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
package io.onemfive.desktop.util;

import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Runs all listener objects periodically in a short interval.
public class MasterTimer {

    private final static Logger LOG = LoggerFactory.getLogger(MasterTimer.class);

    private static final java.util.Timer timer = new java.util.Timer();
    // frame rate of 60 fps is about 16 ms but we  don't need such a short interval, 100 ms should be good enough
    public static final long FRAME_INTERVAL_MS = 100;

    static {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                UserThread.execute(() -> listeners.stream().forEach(Runnable::run));
            }
        }, FRAME_INTERVAL_MS, FRAME_INTERVAL_MS);
    }

    private static final Set<Runnable> listeners = new CopyOnWriteArraySet<>();

    public static void addListener(Runnable runnable) {
        listeners.add(runnable);
    }

    public static void removeListener(Runnable runnable) {
        listeners.remove(runnable);
    }
}
