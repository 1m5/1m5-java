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
 * Like {@link OOMHandledThread} but with per-thread OOM listeners,
 * rather than a static router-wide listener list,
 * so that an OOM in an app won't call the sc listener
 * to teardown the whole application.
 *
 * This is preferred for application use.
 * See {@link OOMHandledThread} for features.
 */
public class AppThread extends OOMHandledThread {

    private final Set<OOMEventListener> threadListeners = new CopyOnWriteArraySet<>();

    public AppThread() {
        super();
    }

    public AppThread(String name) {
        super(name);
    }

    public AppThread(Runnable r) {
        super(r);
    }

    public AppThread(Runnable r, String name) {
        super(r, name);
    }

    public AppThread(Runnable r, String name, boolean isDaemon) {
        super(r, name, isDaemon);
    }

    public AppThread(ThreadGroup group, Runnable r, String name) {
        super(group, r, name);
    }

    @Override
    protected void fireOOM(OutOfMemoryError oom) {
        for (OOMEventListener listener : threadListeners)
            listener.outOfMemory(oom);
    }

    /** register a new component that wants notification of OOM events */
    public void addOOMEventThreadListener(OOMEventListener lsnr) {
        threadListeners.add(lsnr);
    }

    /** unregister a component that wants notification of OOM events */
    public void removeOOMEventThreadListener(OOMEventListener lsnr) {
        threadListeners.remove(lsnr);
    }
}
