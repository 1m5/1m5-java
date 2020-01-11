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
package io.onemfive.network.sensors.radio.tasks;

import io.onemfive.util.AppThread;
import io.onemfive.network.sensors.radio.RadioSensor;

import java.util.Properties;

/**
 * A task for the Radio Sensor.
 */
public abstract class RadioTask extends AppThread {

    protected RadioSensor sensor;
    protected TaskRunner taskRunner;
    protected Properties properties;
    protected long periodicity = 60 * 60 * 1000; // 1 hour as default
    protected long lastCompletionTime = 0L;
    protected boolean started = false;
    protected boolean completed = false;
    protected boolean longRunning = false;
    protected boolean startRunning = false;
    protected int runs = 0;
    protected int maxRuns = 0;

    public RadioTask(RadioSensor sensor, TaskRunner taskRunner, Properties properties) {
        this.sensor = sensor;
        this.taskRunner = taskRunner;
        this.properties = properties;
        this.lastCompletionTime = System.currentTimeMillis();
    }

    public RadioTask(RadioSensor sensor, TaskRunner taskRunner, Properties properties, long periodicity) {
        this.sensor = sensor;
        this.taskRunner = taskRunner;
        this.properties = properties;
        this.lastCompletionTime = System.currentTimeMillis();
        this.periodicity = periodicity;
    }

    public boolean runTask() {
        runs++;
        return true;
    }

    public int getRuns() {
        return runs;
    }

    public int getMaxRuns() {
        return  maxRuns;
    }

    public void setLongRunning(boolean longRunning) {
        this.longRunning = longRunning;
    }

    public void setStartRunning(boolean startRunning) {
        this.startRunning = startRunning;
    }

    public boolean getLongRunning() {return longRunning;}

    public void setLastCompletionTime(long lastCompletionTime) {
        this.lastCompletionTime = lastCompletionTime;
    }

    public long getLastCompletionTime() { return lastCompletionTime;}

    public long getPeriodicity() {
        return periodicity;
    }
}
