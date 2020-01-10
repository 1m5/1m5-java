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
import io.onemfive.core.util.OOMHandledThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Note - if no filter is defined in stat.logFilters at startup, this class will not
 * be instantiated - see StatManager.
 */
public class BufferedStatLog implements StatLog {

    private static Logger LOG = Logger.getLogger(BufferedStatLog.class.getName());
    private static final int BUFFER_SIZE = 1024;
    private static final boolean DISABLE_LOGGING = false;

    private final OneMFiveAppContext context;
    private final StatEvent events[];
    private int eventNext;
    private int lastWrite;
    /** flush stat events to disk after this many events (or 30s)*/
    private int flushFrequency;
    private final List<String> statFilters;
    private String lastFilters;
    private BufferedWriter out;
    private String outFile;
    /** short circuit for adding data, set to true if some filters are set, false if its empty (so we can skip the sync) */
    private volatile boolean filtersSpecified;

    public BufferedStatLog(OneMFiveAppContext ctx) {
        context = ctx;
        events = new StatEvent[BUFFER_SIZE];
        if (DISABLE_LOGGING) return;
        for (int i = 0; i < BUFFER_SIZE; i++)
            events[i] = new StatEvent();
        eventNext = 0;
        lastWrite = events.length-1;
        statFilters = new ArrayList<String>(10);
        flushFrequency = 500;
        updateFilters();
        OOMHandledThread writer = new OOMHandledThread(new StatLogWriter(), "StatLogWriter");
        writer.setDaemon(true);
        writer.start();
    }

    public void addData(String scope, String stat, long value, long duration) {
        if (DISABLE_LOGGING) return;
        if (!shouldLog(stat)) return;
        synchronized (events) {
            events[eventNext].init(scope, stat, value, duration);
            eventNext = (eventNext + 1) % events.length;

            if (eventNext == lastWrite)
                lastWrite = (lastWrite + 1) % events.length; // drop an event

            LOG.info("AddData next=" + eventNext + " lastWrite=" + lastWrite);

            if (eventNext > lastWrite) {
                if (eventNext - lastWrite >= flushFrequency)
                    events.notifyAll();
            } else {
                if (events.length - 1 - lastWrite + eventNext >= flushFrequency)
                    events.notifyAll();
            }
        }
    }

    private boolean shouldLog(String stat) {
        if (!filtersSpecified) return false;
        synchronized (statFilters) {
            return statFilters.contains(stat) || statFilters.contains("*");
        }
    }

    private void updateFilters() {
        String val = context.getProperty(StatManager.PROP_STAT_FILTER);
        if (val != null) {
            if ( (lastFilters != null) && (lastFilters.equals(val)) ) {
                // noop
            } else {
                StringTokenizer tok = new StringTokenizer(val, ",");
                synchronized (statFilters) {
                    statFilters.clear();
                    while (tok.hasMoreTokens())
                        statFilters.add(tok.nextToken().trim());
                    filtersSpecified = !statFilters.isEmpty();
                }
            }
            lastFilters = val;
        } else {
            synchronized (statFilters) {
                statFilters.clear();
                filtersSpecified = false;
            }
        }

        String filename = context.getProperty(StatManager.PROP_STAT_FILE, StatManager.DEFAULT_STAT_FILE);
        File foo = new File(filename);
        if (!foo.isAbsolute())
            filename = (new File(context.getBaseDir(), filename)).getAbsolutePath();
        if ( (outFile != null) && (outFile.equals(filename)) ) {
            // noop
        } else {
            if (out != null) try { out.close(); } catch (IOException ioe) {}
            outFile = filename;
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile, true), "UTF-8"), 32*1024);
            } catch (IOException ioe) { ioe.printStackTrace(); }
        }
    }

    private class StatLogWriter implements Runnable {
        private final SimpleDateFormat _fmt = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        public void run() {
            int writeStart = -1;
            int writeEnd = -1;
            while (true) {
                try {
                    synchronized (events) {
                        if (eventNext > lastWrite) {
                            if (eventNext - lastWrite < flushFrequency)
                                events.wait(30*1000);
                        } else {
                            if (events.length - 1 - lastWrite + eventNext < flushFrequency)
                                events.wait(30*1000);
                        }
                        writeStart = (lastWrite + 1) % events.length;
                        writeEnd = eventNext;
                        lastWrite = (writeEnd == 0 ? events.length-1 : writeEnd - 1);
                    }
                    if (writeStart != writeEnd) {
                        try {
                            LOG.info("writing " + writeStart +"->"+ writeEnd);
                            writeEvents(writeStart, writeEnd);
                        } catch (RuntimeException e) {
                            LOG.warning("error writing " + writeStart +"->"+ writeEnd+": "+ e.getLocalizedMessage());
                        }
                    }
                } catch (InterruptedException ie) {}
            }
        }

        private void writeEvents(int start, int end) {
            try {
                updateFilters();
                int cur = start;
                while (cur != end) {
                    //if (shouldLog(_events[cur].getStat())) {
                    String when = null;
                    synchronized (_fmt) {
                        when = _fmt.format(new Date(events[cur].getTime()));
                    }
                    out.write(when);
                    out.write(" ");
                    if (events[cur].getScope() == null)
                        out.write("noScope");
                    else
                        out.write(events[cur].getScope());
                    out.write(" ");
                    out.write(events[cur].getStat());
                    out.write(" ");
                    out.write(Long.toString(events[cur].getValue()));
                    out.write(" ");
                    out.write(Long.toString(events[cur].getDuration()));
                    out.write("\n");
                    //}
                    cur = (cur + 1) % events.length;
                }
                out.flush();
            } catch (IOException ioe) {
                LOG.warning("Error writing out: "+ioe.getLocalizedMessage());
            }
        }
    }

    private class StatEvent {
        private long _time;
        private String _scope;
        private String _stat;
        private long _value;
        private long _duration;

        public long getTime() { return _time; }
        public String getScope() { return _scope; }
        public String getStat() { return _stat; }
        public long getValue() { return _value; }
        public long getDuration() { return _duration; }

        public void init(String scope, String stat, long value, long duration) {
            _scope = scope;
            _stat = stat;
            _value = value;
            _duration = duration;
            _time = new Date().getTime();
        }
    }
}
