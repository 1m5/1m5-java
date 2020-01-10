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
import java.io.OutputStream;
import static java.util.Arrays.*;
import java.util.Properties;

/** coordinate a moving rate over various periods */
public class RateStat {
    /** unique name of the statistic */
    private final String statName;
    /** grouping under which the stat is kept */
    private final String groupName;
    /** describe the stat */
    private final String description;
    /** actual rate objects for this statistic */
    protected final Rate[] rates;
    /** component we tell about events as they occur */
    private StatLog statLog;

    public RateStat(String name, String description, String group, long periods[]) {
        statName = name;
        this.description = description;
        groupName = group;
        if (periods.length == 0)
            throw new IllegalArgumentException();

        long [] periodsCopy = new long[periods.length];
        System.arraycopy(periods, 0, periodsCopy, 0, periods.length);
        sort(periodsCopy);

        rates = new Rate[periodsCopy.length];
        for (int i = 0; i < periodsCopy.length; i++) {
            Rate rate = new Rate(periodsCopy[i]);
            rate.setRateStat(this);
            rates[i] = rate;
        }
    }
    public void setStatLog(StatLog sl) { statLog = sl; }

    /**
     * update all of the rates for the various periods with the given value.
     */
    public void addData(long value, long eventDuration) {
        if (statLog != null) statLog.addData(groupName, statName, value, eventDuration);
        for (Rate r: rates)
            r.addData(value, eventDuration);
    }

    /**
     * Update all of the rates for the various periods with the given value.
     * Zero duration.
     */
    public void addData(long value) {
        if (statLog != null) statLog.addData(groupName, statName, value, 0);
        for (Rate r: rates)
            r.addData(value);
    }

    /** coalesce all the stats */
    public void coalesceStats() {
        for (Rate r: rates)
            r.coalesce();
    }

    public String getName() {
        return statName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public long[] getPeriods() {
        long [] rv = new long[rates.length];
        for (int i = 0; i < rates.length; i++)
            rv[i] = rates[i].getPeriod();
        return rv;
    }

    public double getLifetimeAverageValue() {
        return rates[0].getLifetimeAverageValue();
    }
    public long getLifetimeEventCount() {
        return rates[0].getLifetimeEventCount();
    }

    /**
     * Returns rate with requested period if it exists,
     * otherwise null
     * @param period ms
     * @return the Rate
     */
    public Rate getRate(long period) {
        for (Rate r : rates) {
            if (r.getPeriod() == period)
                return r;
        }

        return null;
    }

    /**
     * Adds a new rate with the requested period, provided that
     * a rate with that period does not already exist.
     * @param period ms
     */
    @Deprecated
    public void addRate(long period) {
        throw new UnsupportedOperationException();
    }

    /**
     * If a rate with the provided period exists, remove it.
     * @param period ms
     */
    @Deprecated
    public void removeRate(long period) {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests if a rate with the provided period exists within this RateStat.
     * @param period ms
     * @return true if exists
     */
    public boolean containsRate(long period) {
        return getRate(period) != null;
    }

    @Override
    public int hashCode() {
        return statName.hashCode();
    }

    private final static String NL = System.getProperty("line.separator");

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(4096);
        buf.append(getGroupName()).append('.').append(getName()).append(": ").append(getDescription()).append('\n');
        long periods[] = getPeriods();
        sort(periods);
        for (int i = 0; i < periods.length; i++) {
            buf.append('\t').append(periods[i]).append(':');
            Rate curRate = getRate(periods[i]);
            buf.append(curRate.toString());
            buf.append(NL);
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof RateStat)) return false;
        if (obj == this)
            return true;
        RateStat rs = (RateStat) obj;
        if (nameGroupDescEquals(rs))
            return deepEquals(this.rates, rs.rates);

        return false;
    }

    boolean nameGroupDescEquals(RateStat rs) {
        return DataHelper.eq(getGroupName(), rs.getGroupName()) && DataHelper.eq(getDescription(), rs.getDescription())
                && DataHelper.eq(getName(), rs.getName());
    }

    public void store(OutputStream out, String prefix) throws IOException {
        StringBuilder buf = new StringBuilder(1024);
        buf.append(NL);
        buf.append("################################################################################").append(NL);
        buf.append("# Rate: ").append(groupName).append(": ").append(statName).append(NL);
        buf.append("# ").append(description).append(NL);
        buf.append("# ").append(NL).append(NL);
        out.write(buf.toString().getBytes("UTF-8"));
        buf.setLength(0);
        for (Rate r: rates){
            buf.append("#######").append(NL);
            buf.append("# Period : ").append(DataHelper.formatDuration(r.getPeriod())).append(" for rate ")
                    .append(groupName).append(" - ").append(statName).append(NL);
            buf.append(NL);
            String curPrefix = prefix + "." + DataHelper.formatDuration(r.getPeriod());
            r.store(curPrefix, buf);
            out.write(buf.toString().getBytes("UTF-8"));
            buf.setLength(0);
        }
    }

    /**
     * Load this rate stat from the properties, populating all of the rates contained
     * underneath it.  The comes from the given prefix (e.g. if we are given the prefix
     * "profile.dbIntroduction", a series of rates may be found underneath
     * "profile.dbIntroduction.60s", "profile.dbIntroduction.60m", and "profile.dbIntroduction.24h").
     * This RateStat must already be created, with the specified rate entries constructued - this
     * merely loads them with data.
     *
     * @param prefix prefix to the property entries (should NOT end with a period)
     * @param treatAsCurrent if true, we'll treat the loaded data as if no time has
     *                       elapsed since it was written out, but if it is false, we'll
     *                       treat the data with as much freshness (or staleness) as appropriate.
     * @throws IllegalArgumentException if the data was formatted incorrectly
     */
    public void load(Properties props, String prefix, boolean treatAsCurrent) throws IllegalArgumentException {
        for (Rate r : rates) {
            long period = r.getPeriod();
            String curPrefix = prefix + "." + DataHelper.formatDuration(period);
            r.load(props, curPrefix, treatAsCurrent);
        }
    }

}
