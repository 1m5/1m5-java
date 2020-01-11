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
