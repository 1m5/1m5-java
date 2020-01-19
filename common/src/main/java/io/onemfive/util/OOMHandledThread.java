/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.util;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Preferred over {@link Thread} for all uses.
 * For applications, {@link AppThread} is preferred.
 * <p>
 * Provides the following features:
 * <ul>
 * <li>Logging to wrapper log on unexpected termination in {@link #run()}.
 * <li>Notification of OOM to registered listener (the router),
 *     which will cause logging to the wrapper log and a router restart
 * <li>Catching and logging "OOM" caused by thread limit in {@link #start()}
 *     with distinct message, and does not call the OOM listener.
 * <li>Initialization to NORM_PRIORITY
 *     (not the priority of the creating thread).
 * </ul>
 */
public class OOMHandledThread extends Thread {

    private boolean shutdown = false;
    private static final Set<OOMEventListener> _listeners = new CopyOnWriteArraySet<OOMEventListener>();

    public OOMHandledThread() {
        super();
        setPriority(NORM_PRIORITY);
    }

    public OOMHandledThread(String name) {
        super(name);
        setPriority(NORM_PRIORITY);
    }

    public OOMHandledThread(Runnable r) {
        super(r);
        setPriority(NORM_PRIORITY);
    }

    public OOMHandledThread(Runnable r, String name) {
        super(r, name);
        setPriority(NORM_PRIORITY);
    }

    public OOMHandledThread(Runnable r, String name, boolean isDaemon) {
        super(r, name);
        setDaemon(isDaemon);
        setPriority(NORM_PRIORITY);
    }

    public OOMHandledThread(ThreadGroup g, Runnable r) {
        super(g, r);
        setPriority(NORM_PRIORITY);
    }

    public OOMHandledThread(ThreadGroup group, Runnable r, String name) {
        super(group, r, name);
        setPriority(NORM_PRIORITY);
    }

    /**
     *  Overridden to provide useful info to users on OOM, and to prevent
     *  shutting down the whole JVM for what is most likely not a heap issue.
     *  If the calling thread is an OOMHandledThread an OOM would shut down the JVM.
     *  Telling the user to increase the heap size may make the problem worse.
     *  We may be able to continue without this thread, particularly in app context.
     */
    @Override
    public void start() {
        try {
            super.start();
        } catch (OutOfMemoryError oom) {
            System.out.println("ERROR: Thread could not be started: " + getName());
            if (!(SystemVersion.isWindows() || SystemVersion.isAndroid())) {
                System.out.println("Check ulimit -u, /etc/security/limits.conf, or /proc/sys/kernel/threads-max");
            }
            oom.printStackTrace();
            if (!(SystemVersion.isWindows() || SystemVersion.isAndroid()))
                throw new RuntimeException("Thread could not be started, " +
                        "Check ulimit -u, /etc/security/limits.conf, or /proc/sys/kernel/threads-max", oom);
            throw new RuntimeException("Thread could not be started", oom);
        }
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (Throwable t) {
            if (t instanceof OutOfMemoryError) {
                fireOOM((OutOfMemoryError)t);
            } else {
                System.out.println ("Thread terminated unexpectedly: " + getName());
                t.printStackTrace();
            }
        }
    }

    protected void fireOOM(OutOfMemoryError oom) {
        for (OOMEventListener listener : _listeners)
            listener.outOfMemory(oom);
    }

    /** register a new component that wants notification of OOM events */
    public static void addOOMEventListener(OOMEventListener lsnr) {
        _listeners.add(lsnr);
    }

    /** unregister a component that wants notification of OOM events */
    public static void removeOOMEventListener(OOMEventListener lsnr) {
        _listeners.remove(lsnr);
    }

    public interface OOMEventListener {
        void outOfMemory(OutOfMemoryError err);
    }

/****
 public static void main(String args[]) {
 OOMHandledThread t = new OOMHandledThread(new Runnable() {
 public void run() {
 throw new NullPointerException("blah");
 }
 });
 t.init();
 try {
 Thread.sleep(10000);
 } catch (Throwable tt) { // nop
 }
 }
 ****/
}
