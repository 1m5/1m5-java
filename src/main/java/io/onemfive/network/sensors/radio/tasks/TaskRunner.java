package io.onemfive.network.sensors.radio.tasks;

import io.onemfive.core.util.AppThread;
import io.onemfive.network.sensors.radio.RadioSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Runs Radio Tasks.
 *
 * @author objectorange
 */
public class TaskRunner extends AppThread {

    private static final Logger LOG = Logger.getLogger(TaskRunner.class.getName());

    public enum Status {Running, Stopping, Shutdown}

    private long timeBetweenRunsMs = 1000; // every second check to see if a task needs running
    private List<RadioTask> tasks = new ArrayList<>();
    private Status status = Status.Shutdown;
    private RadioSensor sensor;

    private Properties properties;

    public TaskRunner(RadioSensor sensor, Properties properties) {
        this.sensor = sensor;
        this.properties = properties;
    }

    // Run Task immediately but track it
    public void executeTask(RadioTask t) {
        if(t.longRunning) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    t.runTask();
                }
            };
            new Thread(r).start();
        } else {
            if(t.runTask())
                t.setLastCompletionTime(System.currentTimeMillis());
            else
                LOG.warning("Task exited as failure.");
        }
    }

    public void addTask(RadioTask t) {
        // Ensure time between runs is at least the lowest task periodicity
        if(t.getPeriodicity() > 0 && t.getPeriodicity() < timeBetweenRunsMs)
            timeBetweenRunsMs = t.getPeriodicity();
        tasks.add(t);
    }

    public void removeTask(RadioTask t) {
        tasks.remove(t);
        long def = 2 * 60 * 1000;
        for(RadioTask task : tasks) {
            if(task.getPeriodicity() < def) {
                def = task.getPeriodicity();
            }
        }
        if(timeBetweenRunsMs != def) {
            timeBetweenRunsMs = def;
            LOG.info("Changed TaskRunner.timeBetweenRuns in ms to: "+timeBetweenRunsMs);
        }
    }

    @Override
    public void run() {
        status = Status.Running;
        LOG.info("Radio Sensor: Task Runner running...");
        while(status == Status.Running) {
            try {
                LOG.fine("Radio Sensor: Sleeping for "+(timeBetweenRunsMs/(60*1000))+" minutes..");
                synchronized (this) {
                    this.wait(timeBetweenRunsMs);
                }
            } catch (InterruptedException ex) {
            }
            LOG.finer("Radio Sensor: Awoke, determine if tasks ("+tasks.size()+") need ran...");
            for (RadioTask t : tasks) {
                if (t.getPeriodicity() == -1) {
                    continue; // Flag to not run
                }
                if(t.started) {
                    LOG.finer("Task in progress.");
                } else if(t.maxRuns > 0 && t.runs > t.maxRuns) {
                    LOG.info("Max runs reached.");
                    t.completed = true;
                } else if(t.startRunning || (System.currentTimeMillis() - t.getLastCompletionTime()) > t.getPeriodicity()) {
                    t.startRunning = false; // Ensure we don't run this again without verifying periodicity
                    if(t.longRunning) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                t.runTask();
                            }
                        };
                        new Thread(r).start();
                    } else {
                        if(t.runTask())
                            t.setLastCompletionTime(System.currentTimeMillis());
                        else
                            LOG.warning("Radio Sensor: Task exited as incomplete.");
                    }
                } else {
                    LOG.finer("Radio Sensor: Either startRunning is false or it's not yet time to start.");
                }
                if(t.completed)
                    removeTask(t);
            }
        }
        LOG.info("Radio Sensor: Task Runner Stopped.");
        status = Status.Shutdown;
    }

    public void shutdown() {
        status = Status.Stopping;
        LOG.info("Radio Sensor: Signaled Task Runner to shutdown after all tasks complete...");
    }

}
