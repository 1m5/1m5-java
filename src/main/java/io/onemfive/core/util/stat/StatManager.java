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

import io.onemfive.core.OneMFiveAppContext;

import java.io.IOException;
import java.io.OutputStream;
import java.text.Collator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinate the management of various frequencies and rates within SC services,
 * both allowing central update and retrieval, as well as distributed creation and
 * use. This does not provide any persistence, but the data structures exposed can be
 * read and updated to manage the complete state.
 *
 */
public class StatManager {

    /**
     *  Comma-separated stats or * for all.
     *  This property must be set at startup, or
     *  logging is disabled.
     */
    public static final String PROP_STAT_FILTER = "stat.logFilters";
    public static final String PROP_STAT_FILE = "stat.logFile";
    public static final String DEFAULT_STAT_FILE = "stats.log";
    /** default false */
    public static final String PROP_STAT_FULL = "stat.full";
    /** every this many minutes for frequencies */
    private static final int FREQ_COALESCE_RATE = 9;

    private final OneMFiveAppContext context;

    /** stat name to FrequencyStat */
    private final ConcurrentHashMap<String, FrequencyStat> frequencyStats;
    /** stat name to RateStat */
    private final ConcurrentHashMap<String, RateStat> rateStats;
    /** may be null */
    private StatLog statLog;

    private int coalesceCounter;

    /**
     * The stat manager should only be constructed and accessed through the
     * application context.  This constructor should only be used by the
     * appropriate application context itself.
     */
    public StatManager(OneMFiveAppContext context) {
        this.context = context;
        frequencyStats = new ConcurrentHashMap<>(8);
        rateStats = new ConcurrentHashMap<>(128);
        String filter = getStatFilter();
        if (filter != null && filter.length() > 0)
            statLog = new BufferedStatLog(context);
    }

    public void shutdown() {
        frequencyStats.clear();
        rateStats.clear();
    }

    public StatLog getStatLog() { return statLog; }
    public void setStatLog(StatLog log) {
        statLog = log;
        for (RateStat rs : rateStats.values()) {
            rs.setStatLog(log);
        }
    }

    /**
     * Create a new statistic to monitor the frequency of some event.
     * The stat is ONLY created if the stat.full property is true or we are not in the router context.
     *
     * @param name unique name of the statistic
     * @param description simple description of the statistic
     * @param group used to group statistics together
     * @param periods array of period lengths (in milliseconds)
     */
    public void createFrequencyStat(String name, String description, String group, long periods[]) {
        if (ignoreStat(name)) return;
        createRequiredFrequencyStat(name, description, group, periods);
    }

    /**
     * Create a new statistic to monitor the frequency of some event.
     * The stat is always created, independent of the stat.full setting or context.
     *
     * @param name unique name of the statistic
     * @param description simple description of the statistic
     * @param group used to group statistics together
     * @param periods array of period lengths (in milliseconds)
     */
    public void createRequiredFrequencyStat(String name, String description, String group, long periods[]) {
        if (frequencyStats.containsKey(name)) return;
        frequencyStats.putIfAbsent(name, new FrequencyStat(name, description, group, periods));
    }

    /**
     * Create a new statistic to monitor the average value and confidence of some action.
     * The stat is ONLY created if the stat.full property is true or we are not in the router context.
     *
     * @param name unique name of the statistic
     * @param description simple description of the statistic
     * @param group used to group statistics together
     * @param periods array of period lengths (in milliseconds)
     */
    public void createRateStat(String name, String description, String group, long periods[]) {
        if (ignoreStat(name)) return;
        createRequiredRateStat(name, description, group, periods);
    }

    /**
     * Create a new statistic to monitor the average value and confidence of some action.
     * The stat is always created, independent of the stat.full setting or context.
     *
     * @param name unique name of the statistic
     * @param description simple description of the statistic
     * @param group used to group statistics together
     * @param periods array of period lengths (in milliseconds)
     */
    public void createRequiredRateStat(String name, String description, String group, long periods[]) {
        if (rateStats.containsKey(name)) return;
        RateStat rs = new RateStat(name, description, group, periods);
        if (statLog != null) rs.setStatLog(statLog);
        rateStats.putIfAbsent(name, rs);
    }

    // Hope this doesn't cause any problems with unsynchronized accesses like addRateData() ...
    public void removeRateStat(String name) {
        rateStats.remove(name);
    }

    /** update the given frequency statistic, taking note that an event occurred (and recalculating all frequencies) */
    public void updateFrequency(String name) {
        FrequencyStat freq = frequencyStats.get(name);
        if (freq != null) freq.eventOccurred();
    }

    /** update the given rate statistic, taking note that the given data point was received (and recalculating all rates) */
    public void addRateData(String name, long data, long eventDuration) {
        RateStat stat = rateStats.get(name); // unsynchronized
        if (stat != null) stat.addData(data, eventDuration);
    }

    /**
     * Update the given rate statistic, taking note that the given data point was received (and recalculating all rates).
     * Zero duration.
     */
    public void addRateData(String name, long data) {
        RateStat stat = rateStats.get(name); // unsynchronized
        if (stat != null) stat.addData(data);
    }

    public void coalesceStats() {
        if (++coalesceCounter % FREQ_COALESCE_RATE == 0) {
            for (FrequencyStat stat : frequencyStats.values()) {
                if (stat != null) {
                    stat.coalesceStats();
                }
            }
        }
        for (RateStat stat : rateStats.values()) {
            if (stat != null) {
                stat.coalesceStats();
            }
        }
    }

    /**
     *  Misnamed, as it returns a FrequenceyStat, not a Frequency.
     */
    public FrequencyStat getFrequency(String name) {
        return frequencyStats.get(name);
    }

    /**
     *  Misnamed, as it returns a RateStat, not a Rate.
     */
    public RateStat getRate(String name) {
        return rateStats.get(name);
    }

    public Set<String> getFrequencyNames() {
        return new HashSet<String>(frequencyStats.keySet());
    }

    public Set<String> getRateNames() {
        return new HashSet<String>(rateStats.keySet());
    }

    /** is the given stat a monitored rate? */
    public boolean isRate(String statName) {
        return rateStats.containsKey(statName);
    }

    /** is the given stat a monitored frequency? */
    public boolean isFrequency(String statName) {
        return frequencyStats.containsKey(statName);
    }

    /**
     * Group name (untranslated String) to a SortedSet of untranslated stat names.
     * Map is unsorted.
     */
    public Map<String, SortedSet<String>> getStatsByGroup() {
        Map<String, SortedSet<String>> groups = new HashMap<String, SortedSet<String>>(32);
        for (FrequencyStat stat : frequencyStats.values()) {
            String gname = stat.getGroupName();
            SortedSet<String> names = groups.get(gname);
            if (names == null) {
                names = new TreeSet<String>(Collator.getInstance());
                groups.put(gname, names);
            }
            names.add(stat.getName());
        }
        for (RateStat stat : rateStats.values()) {
            String gname = stat.getGroupName();
            SortedSet<String> names = groups.get(gname);
            if (names == null) {
                names = new TreeSet<String>(Collator.getInstance());
                groups.put(gname, names);
            }
            names.add(stat.getName());
        }
        return groups;
    }

    public String getStatFilter() { return context.getProperty(PROP_STAT_FILTER); }
    public String getStatFile() { return context.getProperty(PROP_STAT_FILE, DEFAULT_STAT_FILE); }

    /**
     * Save memory by not creating stats unless they are required for conscious operation.
     * For backward compatibility of any external clients, always returns false if not in router context.
     *
     * @param statName ignored
     * @return true if the stat should be ignored.
     */
    public boolean ignoreStat(String statName) {
        return context.isConsciousContext() && !context.getBooleanProperty(PROP_STAT_FULL);
    }

    /**
     * Serializes all Frequencies and Rates to the provided OutputStream
     * @param out to write to
     * @param prefix to use when serializing
     * @throws IOException if something goes wrong
     */
    public void store(OutputStream out, String prefix) throws IOException {
        for (FrequencyStat fs : frequencyStats.values())
            fs.store(out, prefix);
        for (RateStat rs : rateStats.values())
            rs.store(out,prefix);
    }
}
