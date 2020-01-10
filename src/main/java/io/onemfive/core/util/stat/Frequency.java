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
package io.onemfive.core.util.stat;

/**
 * Manage the calculation of a moving average event frequency over a certain period.
 *
 * This provides lifetime, and rolling average, frequency counts.
 * Unlike Rate, it does not support "bucketed" averages.
 * There is no tracking of the event frequency in the current or last bucket.
 * There are no buckets at all.
 *
 * Depending on what you want, a rolling average might be better than buckets.
 * Or not.
 */
public class Frequency {
    private double avgInterval;
    private double minAverageInterval;
    private final long period;
    private long lastEvent;
    private final long start = now();
    private long count;

    /** @param period ms */
    public Frequency(long period) {
        this.period = period;
        avgInterval = period + 1;
        minAverageInterval = avgInterval;
    }

    /** how long is this frequency averaged over? (ms) */
    public long getPeriod() {
        return period;
    }

    /**
     * when did the last event occur?
     * @deprecated unused
     */
    @Deprecated
    public synchronized long getLastEvent() {
        return lastEvent;
    }

    /**
     * on average over the last $period, after how many milliseconds are events coming in,
     * as calculated during the last event occurrence?
     * @return milliseconds; returns period + 1 if no events in previous period
     */
    public synchronized double getAverageInterval() {
        return avgInterval;
    }

    /**
     * what is the lowest average interval (aka most frequent) we have seen? (ms)
     * @return milliseconds; returns period + 1 if no events in previous period
     * @deprecated unused
     */
    @Deprecated
    public synchronized double getMinAverageInterval() {
        return minAverageInterval;
    }

    /**
     * Calculate how many events would occur in a period given the current (rolling) average.
     * Use getStrictAverageInterval() for the real lifetime average.
     */
    public synchronized double getAverageEventsPerPeriod() {
        if (avgInterval > 0) return period / avgInterval;

        return 0;
    }

    /**
     * Calculate how many events would occur in a period given the maximum rolling average.
     * Use getStrictAverageEventsPerPeriod() for the real lifetime average.
     */
    public synchronized double getMaxAverageEventsPerPeriod() {
        if (minAverageInterval > 0 && minAverageInterval <= period) return period / minAverageInterval;

        return 0;
    }

    /**
     * Over the lifetime of this stat, without any decay or weighting, what was the average interval between events? (ms)
     * @return milliseconds; returns Double.MAX_VALUE if no events ever
     */
    public synchronized double getStrictAverageInterval() {
        long duration = now() - start;
        if ((duration <= 0) || (count <= 0)) return Double.MAX_VALUE;
        return duration / (double) count;
    }

    /** using the strict average interval, how many events occur within an average period? */
    public synchronized double getStrictAverageEventsPerPeriod() {
        double avgInterval = getStrictAverageInterval();
        if (avgInterval > 0) return period / avgInterval;
        return 0;
    }

    /** how many events have occurred within the lifetime of this stat? */
    public synchronized long getEventCount() {
        return count;
    }

    /**
     * Take note that a new event occurred, recalculating all the averages and frequencies
     *
     */
    public void eventOccurred() {
        recalculate(true);
    }

    /**
     * Recalculate the averages
     *
     */
    public void recalculate() {
        recalculate(false);
    }

    /**
     * Recalculate, but only update the lastEvent if eventOccurred
     */
    private void recalculate(boolean eventOccurred) {
        synchronized (this) {
            // This calculates something of a rolling average interval.
            long now = now();
            long interval = now - lastEvent;
            if (interval > period)
                interval = period;
            else if (interval <= 0) interval = 1;

            if (interval >= period && !eventOccurred) {
                // ensure getAverageEventsPerPeriod() will return 0
                avgInterval = period + 1;
            } else {
                double oldWeight = 1 - (interval / (float) period);
                double newWeight = (interval / (float) period);
                double oldInterval = avgInterval * oldWeight;
                double newInterval = interval * newWeight;
                avgInterval = oldInterval + newInterval;
            }

            if ((avgInterval < minAverageInterval) || (minAverageInterval <= 0)) minAverageInterval = avgInterval;

            if (eventOccurred) {
                lastEvent = now;
                count++;
            }
        }
    }

    private final static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Appends the data of this frequency to the specified StringBuilder
     * @param dest to append data to
     */
    synchronized void store(StringBuilder dest) {
        dest.append("avgInterval:").append(avgInterval).append(',');
        dest.append("minAverageInterval").append(minAverageInterval).append(',');
        dest.append("lastEvent").append(lastEvent).append(",");
        dest.append("count").append(count);
    }
}
