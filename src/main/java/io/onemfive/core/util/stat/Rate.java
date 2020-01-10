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

import io.onemfive.core.util.data.DataHelper;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Simple rate calculator for periodically sampled data points - determining an
 * average value over a period, the number of events in that period, the maximum number
 * of events (using the interval between events), and lifetime data.
 *
 * If value is always a constant, you should be using Frequency instead.
 */
public class Rate {

    private final static Logger LOG = Logger.getLogger(Rate.class.getName());

    private float currentTotalValue;
    // was long, save space
    private int currentEventCount;
    private int currentTotalEventTime;
    private float lastTotalValue;
    // was long, save space
    private int lastEventCount;
    private int lastTotalEventTime;
    private float extremeTotalValue;
    // was long, save space
    private int extremeEventCount;
    private int extremeTotalEventTime;
    private float lifetimeTotalValue;
    private long lifetimeEventCount;
    private long lifetimeTotalEventTime;
    private RateSummaryListener summaryListener;
    private RateStat stat;

    private long lastCoalesceDate;
    private long creationDate;
    // was long, save space
    private int period;

    /** in the current (partial) period, what is the total value acrued through all events? */
    public synchronized double getCurrentTotalValue() {
        return currentTotalValue;
    }

    /** in the current (partial) period, how many events have occurred? */
    public synchronized long getCurrentEventCount() {
        return currentEventCount;
    }

    /** in the current (partial) period, how much of the time has been spent doing the events? */
    public synchronized long getCurrentTotalEventTime() {
        return currentTotalEventTime;
    }

    /** in the last full period, what was the total value acrued through all events? */
    public synchronized double getLastTotalValue() {
        return lastTotalValue;
    }

    /** in the last full period, how many events occurred? */
    public synchronized long getLastEventCount() {
        return lastEventCount;
    }

    /** in the last full period, how much of the time was spent doing the events? */
    public synchronized long getLastTotalEventTime() {
        return lastTotalEventTime;
    }

    /** what was the max total value acrued in any period?  */
    public synchronized double getExtremeTotalValue() {
        return extremeTotalValue;
    }

    /**
     * when the max(totalValue) was achieved, how many events occurred in that period?
     * Note that this is not necesarily the highest event count; that isn't tracked.
     */
    public synchronized long getExtremeEventCount() {
        return extremeEventCount;
    }

    /** when the max(totalValue) was achieved, how much of the time was spent doing the events? */
    public synchronized long getExtremeTotalEventTime() {
        return extremeTotalEventTime;
    }

    /** since rate creation, what was the total value acrued through all events?  */
    public synchronized double getLifetimeTotalValue() {
        return lifetimeTotalValue;
    }

    /** since rate creation, how many events have occurred? */
    public synchronized long getLifetimeEventCount() {
        return lifetimeEventCount;
    }

    /** since rate creation, how much of the time was spent doing the events? */
    public synchronized long getLifetimeTotalEventTime() {
        return lifetimeTotalEventTime;
    }

    /** when was the rate last coalesced? */
    public synchronized long getLastCoalesceDate() {
        return lastCoalesceDate;
    }

    /** when was this rate created? */
    public synchronized long getCreationDate() {
        return creationDate;
    }

    /** how large should this rate's cycle be? */
    public synchronized long getPeriod() {
        return period;
    }

    public RateStat getRateStat() { return stat; }
    public void setRateStat(RateStat rs) { stat = rs; }

    /**
     * A rate with period shorter than Router.COALESCE_TIME = 50*1000 has to
     * be manually coalesced before values are fetched from it.
     * @param period number of milliseconds in the period this rate deals with, min 1, max Integer.MAX_VALUE
     * @throws IllegalArgumentException if the period is invalid
     */
    public Rate(long period) throws IllegalArgumentException {
        if (period <= 0 || period > Integer.MAX_VALUE)
            throw new IllegalArgumentException();

        creationDate = now();
        lastCoalesceDate = creationDate;
        this.period = (int) period;
    }

    /**
     * Create a new rate and load its state from the properties, taking data
     * from the data points underneath the given prefix.  <p>
     * (e.g. prefix = "profile.dbIntroduction.60m", this will load the associated data points such
     * as "profile.dbIntroduction.60m.lifetimeEventCount").  The data can be exported
     * through store(outputStream, "profile.dbIntroduction.60m").
     *
     * @param prefix prefix to the property entries (should NOT end with a period)
     * @param treatAsCurrent if true, we'll treat the loaded data as if no time has
     *                       elapsed since it was written out, but if it is false, we'll
     *                       treat the data with as much freshness (or staleness) as appropriate.
     * @throws IllegalArgumentException if the data was formatted incorrectly
     */
    public Rate(Properties props, String prefix, boolean treatAsCurrent) throws IllegalArgumentException {
        this(1);
        load(props, prefix, treatAsCurrent);
    }

    /**
     * Accrue the data in the current period as an instantaneous event.
     * If value is always a constant, you should be using Frequency instead.
     * If you always use this call, eventDuration is always zero,
     * and the various get*Saturation*() and get*EventTime() methods will return zero.
     */
    public synchronized void addData(long value) {
        currentTotalValue += value;
        currentEventCount++;
        lifetimeTotalValue += value;
        lifetimeEventCount++;
    }

    /**
     * Accrue the data in the current period as if the event took the specified amount of time
     * If value is always a constant, you should be using Frequency instead.
     * If eventDuration is nonzero, then the various get*Saturation*() and get*EventTime()
     * methods will also return nonzero.
     *
     * <pre>
     * There are at least 4 possible strategies for eventDuration:
     *
     *   1) eventDuration is always zero.
     *      The various get*Saturation*() and get*EventTime() methods will return zero.
     *
     *   2) Each eventDuration is relatively small, and reflects processing time.
     *      This is probably the original meaning of "saturation", as it allows you
     *      to track how much time is spent gathering the stats.
     *      get*EventTime() will be close to 0.
     *      get*EventSaturation() will return values close to 0,
     *      get*SaturationLimit() will return adjusted values for the totals.
     *
     *   3) The total of the eventDurations are approximately equal to total elapsed time.
     *      get*EventTime() will be close to the period.
     *      get*EventSaturation() will return values close to 1,
     *      get*SaturationLimit() will return adjusted values for the totals.
     *
     *   4) Each eventDuration is not a duration at all, but someother independent data.
     *      get*EventTime() may be used to retrieve the data.
     *      get*EventSaturation() are probably useless.
     *      get*SaturationLimit() are probably useless.
     * </pre>
     *
     * @param value value to accrue in the current period
     * @param eventDuration how long it took to accrue this data (set to 0 if it was instantaneous)
     */
    public synchronized void addData(long value, long eventDuration) {
        currentTotalValue += value;
        currentEventCount++;
        currentTotalEventTime += eventDuration;

        lifetimeTotalValue += value;
        lifetimeEventCount++;
        lifetimeTotalEventTime += eventDuration;
    }

    /** 2s is plenty of slack to deal with slow coalescing (across many stats) */
    private static final int SLACK = 2000;
    public void coalesce() {
        long now = now();
        double correctedTotalValue; // for summaryListener which divides by rounded EventCount
        synchronized (this) {
            long measuredPeriod = now - lastCoalesceDate;
            if (measuredPeriod < period - SLACK) {
                // no need to coalesce (assuming we only try to do so once per minute)
                //if (_log.shouldLog(Log.DEBUG))
                //    _log.debug("not coalescing, measuredPeriod = " + measuredPeriod + " period = " + _period);
                return;
            }

            // ok ok, lets coalesce

            // how much were we off by?  (so that we can sample down the measured values)
            float periodFactor = measuredPeriod / (float) period;
            lastTotalValue = currentTotalValue / periodFactor;
            lastEventCount = (int) (0.499999 + (currentEventCount / periodFactor));
            lastTotalEventTime = (int) (currentTotalEventTime / periodFactor);
            lastCoalesceDate = now;
            if (currentEventCount == 0)
                correctedTotalValue = 0;
            else
                correctedTotalValue = currentTotalValue *
                        (lastEventCount / (double) currentEventCount);

            if (lastTotalValue >= extremeTotalValue) {  // get the most recent if identical
                extremeTotalValue = lastTotalValue;
                extremeEventCount = lastEventCount;
                extremeTotalEventTime = lastTotalEventTime;
            }

            currentTotalValue = 0.0f;
            currentEventCount = 0;
            currentTotalEventTime = 0;
        }
        if (summaryListener != null)
            summaryListener.add(correctedTotalValue, lastEventCount, lastTotalEventTime, period);
    }

    public void setSummaryListener(RateSummaryListener listener) { summaryListener = listener; }
    public RateSummaryListener getSummaryListener() { return summaryListener; }

    /**
     * What was the average value across the events in the last period?
     */
    public synchronized double getAverageValue() {
        int lec = lastEventCount;  // avoid race NPE
        if ((lastTotalValue != 0) && (lec > 0))
            return lastTotalValue / lec;

        return 0.0D;
    }

    /**
     * During the extreme period (i.e. the period with the highest total value),
     * what was the average value?
     */
    public synchronized double getExtremeAverageValue() {
        if ((extremeTotalValue != 0) && (extremeEventCount > 0))
            return extremeTotalValue / extremeEventCount;

        return 0.0D;
    }

    /**
     * What was the average value across the events since the stat was created?
     */
    public synchronized double getLifetimeAverageValue() {
        if ((lifetimeTotalValue != 0) && (lifetimeEventCount > 0))
            return lifetimeTotalValue / lifetimeEventCount;

        return 0.0D;
    }

    /**
     * @return the average or lifetime average depending on last event count
     */
    public synchronized double getAvgOrLifetimeAvg() {
        if (getLastEventCount() > 0)
            return getAverageValue();
        return getLifetimeAverageValue();
    }

    /**
     * During the last period, how much of the time was spent actually processing events in proportion
     * to how many events could have occurred if there were no intervals?
     *
     * @return ratio, or 0 if event times aren't used
     */
    public synchronized double getLastEventSaturation() {
        if ((lastEventCount > 0) && (lastTotalEventTime > 0)) {
            /*double eventTime = (double) _lastTotalEventTime / (double) _lastEventCount;
            double maxEvents = _period / eventTime;
            double saturation = _lastEventCount / maxEvents;
            return saturation;
             */
            return ((double) lastTotalEventTime) / (double) period;
        }

        return 0.0D;
    }

    /**
     * During the extreme period (i.e. the period with the highest total value),
     * how much of the time was spent actually processing events
     * in proportion to how many events could have occurred if there were no intervals?
     *
     * @return ratio, or 0 if the statistic doesn't use event times
     */
    public synchronized double getExtremeEventSaturation() {
        if ((extremeEventCount > 0) && (extremeTotalEventTime > 0)) {
            double eventTime = (double) extremeTotalEventTime / (double) extremeEventCount;
            double maxEvents = period / eventTime;
            return extremeEventCount / maxEvents;
        }
        return 0.0D;
    }

    /**
     * During the lifetime of this stat, how much of the time was spent actually processing events in proportion
     * to how many events could have occurred if there were no intervals?
     *
     * @return ratio, or 0 if event times aren't used
     */
    public synchronized double getLifetimeEventSaturation() {
        if ((lastEventCount > 0) && (lifetimeTotalEventTime > 0)) {
            double eventTime = (double) lifetimeTotalEventTime / (double) lifetimeEventCount;
            double maxEvents = period / eventTime;
            double numPeriods = getLifetimePeriods();
            double avgEventsPerPeriod = lifetimeEventCount / numPeriods;
            return avgEventsPerPeriod / maxEvents;
        }
        return 0.0D;
    }

    /** how many periods have we already completed? */
    public synchronized long getLifetimePeriods() {
        long lifetime = now() - creationDate;
        double periods = lifetime / (double) period;
        return (long) Math.floor(periods);
    }

    /**
     * using the last period's rate, what is the total value that could have been sent
     * if events were constant?
     *
     * @return max total value, or 0 if event times aren't used
     */
    public synchronized double getLastSaturationLimit() {
        if ((lastTotalValue != 0) && (lastEventCount > 0) && (lastTotalEventTime > 0)) {
            double saturation = getLastEventSaturation();
            if (saturation != 0.0D) return lastTotalValue / saturation;

            return 0.0D;
        }
        return 0.0D;
    }

    /**
     * During the extreme period (i.e. the period with the highest total value),
     * what is the total value that could have been
     * sent if events were constant?
     *
     * @return event total at saturation, or 0 if no event times are measured
     */
    public synchronized double getExtremeSaturationLimit() {
        if ((extremeTotalValue != 0) && (extremeEventCount > 0) && (extremeTotalEventTime > 0)) {
            double saturation = getExtremeEventSaturation();
            if (saturation != 0.0d) return extremeTotalValue / saturation;

            return 0.0D;
        }

        return 0.0D;
    }

    /**
     * What was the total value, compared to the total value in
     * the extreme period (i.e. the period with the highest total value),
     * Warning- returns ratio, not percentage (i.e. it is not multiplied by 100 here)
     */
    public synchronized double getPercentageOfExtremeValue() {
        if ((lastTotalValue != 0) && (extremeTotalValue != 0))
            return lastTotalValue / extremeTotalValue;

        return 0.0D;
    }

    /**
     * How large was the last period's value as compared to the lifetime average value?
     * Warning- returns ratio, not percentage (i.e. it is not multiplied by 100 here)
     */
    public synchronized double getPercentageOfLifetimeValue() {
        if ((lastTotalValue != 0) && (lifetimeTotalValue != 0)) {
            double lifetimePeriodValue = period * (lifetimeTotalValue / (now() - creationDate));
            return lastTotalValue / lifetimePeriodValue;
        }

        return 0.0D;
    }

    /**
     * @return a thread-local temp object containing computed averages.
     */
    public RateAverages computeAverages() {
        return computeAverages(RateAverages.getTemp(),false);
    }

    /**
     * @param out where to store the computed averages.
     * @param useLifetime whether the lifetime average should be used if
     * there are no events.
     * @return the same RateAverages object for chaining
     */
    public synchronized RateAverages computeAverages(RateAverages out, boolean useLifetime) {
        out.reset();

        final long total = currentEventCount + lastEventCount;
        out.setTotalEventCount(total);

        if (total <= 0) {
            final double avg = useLifetime ? getLifetimeAverageValue() : getAverageValue();
            out.setAverage(avg);
        } else {

            if (currentEventCount > 0)
                out.setCurrent( getCurrentTotalValue() / currentEventCount);
            if (lastEventCount > 0)
                out.setLast( getLastTotalValue() / lastEventCount);

            out.setTotalValues(getCurrentTotalValue() + getLastTotalValue());
            out.setAverage( out.getTotalValues()  / total );
        }
        return out;
    }

    public synchronized void store(String prefix, StringBuilder buf) throws IOException {
        PersistenceHelper.addTime(buf, prefix, ".period", "Length of the period:", period);
        PersistenceHelper.addDate(buf, prefix, ".creationDate",
                "When was this rate created?", creationDate);
        PersistenceHelper.addDate(buf, prefix, ".lastCoalesceDate",
                "When did we last coalesce this rate?",
                lastCoalesceDate);
        PersistenceHelper.addDate(buf, prefix, ".currentDate",
                "When was this data written?", now());
        PersistenceHelper.add(buf, prefix, ".currentTotalValue",
                "Total value of data points in the current (uncoalesced) period", currentTotalValue);
        PersistenceHelper.add(buf, prefix, ".currentEventCount",
                "How many events have occurred in the current (uncoalesced) period?", currentEventCount);
        PersistenceHelper.addTime(buf, prefix, ".currentTotalEventTime",
                "How much time have the events in the current (uncoalesced) period consumed?",
                currentTotalEventTime);
        PersistenceHelper.add(buf, prefix, ".lastTotalValue",
                "Total value of data points in the most recent (coalesced) period", lastTotalValue);
        PersistenceHelper.add(buf, prefix, ".lastEventCount",
                "How many events have occurred in the most recent (coalesced) period?", lastEventCount);
        PersistenceHelper.addTime(buf, prefix, ".lastTotalEventTime",
                "How much time have the events in the most recent (coalesced) period consumed?",
                lastTotalEventTime);
        PersistenceHelper.add(buf, prefix, ".extremeTotalValue",
                "Total value of data points in the most extreme period", extremeTotalValue);
        PersistenceHelper.add(buf, prefix, ".extremeEventCount",
                "How many events have occurred in the most extreme period?", extremeEventCount);
        PersistenceHelper.addTime(buf, prefix, ".extremeTotalEventTime",
                "How much time have the events in the most extreme period consumed?",
                extremeTotalEventTime);
        PersistenceHelper.add(buf, prefix, ".lifetimeTotalValue",
                "Total value of data points since this stat was created", lifetimeTotalValue);
        PersistenceHelper.add(buf, prefix, ".lifetimeEventCount",
                "How many events have occurred since this stat was created?", lifetimeEventCount);
        PersistenceHelper.addTime(buf, prefix, ".lifetimeTotalEventTime",
                "How much total time was consumed by the events since this stat was created?",
                lifetimeTotalEventTime);
    }

    /**
     * Load this rate from the properties, taking data from the data points
     * underneath the given prefix.
     *
     * @param prefix prefix to the property entries (should NOT end with a period)
     * @param treatAsCurrent if true, we'll treat the loaded data as if no time has
     *                       elapsed since it was written out, but if it is false, we'll
     *                       treat the data with as much freshness (or staleness) as appropriate.
     * @throws IllegalArgumentException if the data was formatted incorrectly
     */
    public synchronized void load(Properties props, String prefix, boolean treatAsCurrent) throws IllegalArgumentException {
        period = PersistenceHelper.getInt(props, prefix, ".period");
        creationDate = PersistenceHelper.getLong(props, prefix, ".creationDate");
        lastCoalesceDate = PersistenceHelper.getLong(props, prefix, ".lastCoalesceDate");
        currentTotalValue = (float)PersistenceHelper.getDouble(props, prefix, ".currentTotalValue");
        currentEventCount = PersistenceHelper.getInt(props, prefix, ".currentEventCount");
        currentTotalEventTime = (int)PersistenceHelper.getLong(props, prefix, ".currentTotalEventTime");
        lastTotalValue = (float)PersistenceHelper.getDouble(props, prefix, ".lastTotalValue");
        lastEventCount = PersistenceHelper.getInt(props, prefix, ".lastEventCount");
        lastTotalEventTime = (int)PersistenceHelper.getLong(props, prefix, ".lastTotalEventTime");
        extremeTotalValue = (float)PersistenceHelper.getDouble(props, prefix, ".extremeTotalValue");
        extremeEventCount = PersistenceHelper.getInt(props, prefix, ".extremeEventCount");
        extremeTotalEventTime = (int)PersistenceHelper.getLong(props, prefix, ".extremeTotalEventTime");
        lifetimeTotalValue = (float)PersistenceHelper.getDouble(props, prefix, ".lifetimeTotalValue");
        lifetimeEventCount = PersistenceHelper.getLong(props, prefix, ".lifetimeEventCount");
        lifetimeTotalEventTime = PersistenceHelper.getLong(props, prefix, ".lifetimeTotalEventTime");

        if (treatAsCurrent) lastCoalesceDate = now();

        if (period <= 0) throw new IllegalArgumentException("Period for " + prefix + " is invalid");
        coalesce();
    }

    /**
     * This is used in StatSummarizer and SummaryListener.
     * We base it on the stat we are tracking, not the stored data.
     */
    @Override
    public synchronized boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof Rate)) return false;
        if (obj == this) return true;
        Rate r = (Rate) obj;
        if (period != r.getPeriod() || creationDate != r.getCreationDate())
            return false;
        if (stat == null && r.stat == null)
            return true;
        if (stat != null && r.stat != null)
            return stat.nameGroupDescEquals(r.stat);
        return false;
    }

    /**
     * It doesn't appear that Rates are ever stored in a Set or Map
     * (RateStat stores in an array) so let's make this easy.
     */
    @Override
    public synchronized int hashCode() {
        return DataHelper.hashCode(stat) ^ period ^ ((int) creationDate);
    }

    @Override
    public synchronized String toString() {
        StringBuilder buf = new StringBuilder(2048);
        buf.append("\n\t total value: ").append(getLastTotalValue());
        buf.append("\n\t highest total value: ").append(getExtremeTotalValue());
        buf.append("\n\t lifetime total value: ").append(getLifetimeTotalValue());
        buf.append("\n\t # periods: ").append(getLifetimePeriods());
        buf.append("\n\t average value: ").append(getAverageValue());
        buf.append("\n\t highest average value: ").append(getExtremeAverageValue());
        buf.append("\n\t lifetime average value: ").append(getLifetimeAverageValue());
        buf.append("\n\t % of lifetime rate: ").append(100.0d * getPercentageOfLifetimeValue());
        buf.append("\n\t % of highest rate: ").append(100.0d * getPercentageOfExtremeValue());
        buf.append("\n\t # events: ").append(getLastEventCount());
        buf.append("\n\t lifetime events: ").append(getLifetimeEventCount());
        if (getLifetimeTotalEventTime() > 0) {
            // we have some actual event durations
            buf.append("\n\t % of time spent processing events: ").append(100.0d * getLastEventSaturation());
            buf.append("\n\t total value if we were always processing events: ").append(getLastSaturationLimit());
            buf.append("\n\t max % of time spent processing events: ").append(100.0d * getExtremeEventSaturation());
            buf.append("\n\t max total value if we were always processing events: ")
                    .append(getExtremeSaturationLimit());
        }
        return buf.toString();
    }

    private static long now() {
        // "event time" is in the stat log (and uses Clock).
        // we just want sequential and stable time here, so use the OS time, since it doesn't
        // skew periodically
        return System.currentTimeMillis(); //Clock.getInstance().now();
    }
}

