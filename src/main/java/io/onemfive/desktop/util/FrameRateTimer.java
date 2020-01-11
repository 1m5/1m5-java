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

import java.time.Duration;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * We simulate a global frame rate timer similar to FXTimer to avoid creation of threads for each timer call.
 * Used only in headless apps like the seed node.
 */
public class FrameRateTimer implements Timer, Runnable {
    private final Logger LOG = Logger.getLogger(FrameRateTimer.class.getName());

    private long interval;
    private Runnable runnable;
    private long startTs;
    private boolean isPeriodically;
    private final String uid = UUID.randomUUID().toString();
    private volatile boolean stopped;

    public FrameRateTimer() {
    }

    @Override
    public void run() {
        if (!stopped) {
            try {
                long currentTimeMillis = System.currentTimeMillis();
                if ((currentTimeMillis - startTs) >= interval) {
                    runnable.run();
                    if (isPeriodically)
                        startTs = currentTimeMillis;
                    else
                        stop();
                }
            } catch (Throwable t) {
                LOG.warning(t.getLocalizedMessage());
                stop();
                throw t;
            }
        }
    }

    @Override
    public Timer runLater(Duration delay, Runnable runnable) {
        this.interval = delay.toMillis();
        this.runnable = runnable;
        startTs = System.currentTimeMillis();
        MasterTimer.addListener(this);
        return this;
    }

    @Override
    public Timer runPeriodically(Duration interval, Runnable runnable) {
        this.interval = interval.toMillis();
        isPeriodically = true;
        this.runnable = runnable;
        startTs = System.currentTimeMillis();
        MasterTimer.addListener(this);
        return this;
    }

    @Override
    public void stop() {
        stopped = true;
        MasterTimer.removeListener(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FrameRateTimer)) return false;

        FrameRateTimer that = (FrameRateTimer) o;

        return !(uid != null ? !uid.equals(that.uid) : that.uid != null);

    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }
}
