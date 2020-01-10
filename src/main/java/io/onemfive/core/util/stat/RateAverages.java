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
 * Storage space for computations of various averages.
 */
public class RateAverages {

    /** thread-local temp instance */
    private static final ThreadLocal<RateAverages> TEMP =
            new ThreadLocal<RateAverages>() {
                public RateAverages initialValue() {
                    return new RateAverages();
                }
            };

    /**
     * @return thread-local temp instance.
     */
    public static RateAverages getTemp() {
        return TEMP.get();
    }

    private double average, current, last, totalValues;
    private long totalEventCount;

    void reset() {
        average = 0;
        current = 0;
        last = 0;
        totalEventCount = 0;
        totalValues = 0;
    }

    /**
     * @return one of several things:
     * if there are any events (current or last) =&gt; weighted average
     * otherwise if the useLifetime parameter to Rate.computeAverages was:
     * true =&gt; the lifetime average value
     * false =&gt; zero
     */
    public double getAverage() {
        return average;
    }

    void setAverage(double average) {
        this.average = average;
    }

    /**
     * @return the current average == current value / current event count
     */
    public double getCurrent() {
        return current;
    }

    void setCurrent(double current) {
        this.current = current;
    }

    /**
     * @return the last average == last value / last event count
     */
    public double getLast() {
        return last;
    }

    void setLast(double last) {
        this.last = last;
    }

    /**
     * @return the total event count == current + last event counts
     */
    public long getTotalEventCount() {
        return totalEventCount;
    }

    void setTotalEventCount(long totalEventCount) {
        this.totalEventCount = totalEventCount;
    }

    /**
     * @return the total values == current + last values
     */
    public double getTotalValues() {
        return totalValues;
    }

    void setTotalValues(double totalValues) {
        this.totalValues = totalValues;
    }

}
