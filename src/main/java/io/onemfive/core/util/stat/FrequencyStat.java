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

/** coordinate an event frequency over various periods */
public class FrequencyStat {
    /** unique name of the statistic */
    private final String statName;
    /** grouping under which the stat is kept */
    private final String groupName;
    /** describe the stat */
    private final String description;
    /** actual frequency objects for this statistic */
    private final Frequency frequencies[];

    public FrequencyStat(String name, String description, String group, long periods[]) {
        statName = name;
        this.description = description;
        groupName = group;
        frequencies = new Frequency[periods.length];
        for (int i = 0; i < periods.length; i++)
            frequencies[i] = new Frequency(periods[i]);
    }

    /** update all of the frequencies for the various periods */
    public void eventOccurred() {
        for (int i = 0; i < frequencies.length; i++)
            frequencies[i].eventOccurred();
    }

    /**
     * coalesce all the stats
     */
    public void coalesceStats() {
        for (int i = 0; i < frequencies.length; i++)
            frequencies[i].recalculate();
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
        long rv[] = new long[frequencies.length];
        for (int i = 0; i < frequencies.length; i++)
            rv[i] = frequencies[i].getPeriod();
        return rv;
    }

    public Frequency getFrequency(long period) {
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i].getPeriod() == period) return frequencies[i];
        }
        return null;
    }

    /**
     * @return lifetime event count
     */
    public long getEventCount() {
        if ( (frequencies == null) || (frequencies.length <= 0) ) return 0;
        return frequencies[0].getEventCount();
    }

    /**
     * @return lifetime average frequency in millisedonds, i.e. the average time between events, or Long.MAX_VALUE if no events ever
     */
    public long getFrequency() {
        if ( (frequencies == null) || (frequencies.length <= 0) ) return Long.MAX_VALUE;
        double d = frequencies[0].getStrictAverageInterval();
        if (d > frequencies[0].getPeriod())
            return Long.MAX_VALUE;
        return Math.round(d);
    }

    @Override
    public int hashCode() {
        return statName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof FrequencyStat)) return false;
        return statName.equals(((FrequencyStat)obj).statName);
    }

    private final static String NL = System.getProperty("line.separator");

    /**
     * Serializes this FrequencyStat to the provided OutputStream
     * @param out to write to
     * @param prefix to prepend to the stat
     * @throws IOException if something goes wrong
     */
    public void store(OutputStream out, String prefix) throws IOException {
        StringBuilder buf = new StringBuilder(1024);
        buf.append(NL);
        buf.append("################################################################################").append(NL);
        buf.append("# Frequency: ").append(groupName).append(": ").append(statName).append(NL);
        buf.append("# ").append(description).append(NL);
        buf.append("# ").append(NL).append(NL);
        out.write(buf.toString().getBytes("UTF-8"));
        buf.setLength(0);
        for (Frequency r: frequencies){
            buf.append("#######").append(NL);
            buf.append("# Period : ").append(DataHelper.formatDuration(r.getPeriod())).append(" for rate ")
                    .append(groupName).append(" - ").append(statName).append(NL);
            buf.append(NL);
            r.store(buf);
            out.write(buf.toString().getBytes("UTF-8"));
            buf.setLength(0);
        }
    }

}
