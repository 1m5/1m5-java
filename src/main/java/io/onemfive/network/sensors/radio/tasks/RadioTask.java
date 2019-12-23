package io.onemfive.network.sensors.radio.tasks;

import io.onemfive.core.util.AppThread;
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
