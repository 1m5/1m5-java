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
package io.onemfive.util.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Runs Tasks based on Timers.
 *
 * @author objectorange
 */
public class TaskRunner implements Runnable {

    private static final Logger LOG = Logger.getLogger(TaskRunner.class.getName());

    public enum Status {Running, Stopping, Shutdown}

    private ThreadPoolExecutor fixedExecutor;
    private ScheduledThreadPoolExecutor scheduledExecutor;

//    private long periodicity = 1000; // every second check to see if a task needs running
    private long periodicity = 30 * 1000; // every 30 seconds check to see if a task needs running
    private List<Task> tasks = new ArrayList<>();
    private Status status = Status.Shutdown;
    private long runUntil = 0L;

    public TaskRunner() {
        // Default to two new thread pools with 4 threads each
        fixedExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        scheduledExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(4);
    }

    public TaskRunner(int fixedExecutorThreads, int scheduledExecutorThreads) {
        if(fixedExecutorThreads > 0) {
            fixedExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(fixedExecutorThreads);
        }
        if(scheduledExecutorThreads > 0) {
            scheduledExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(scheduledExecutorThreads);
        }
    }

    public TaskRunner(ThreadPoolExecutor fixedExecutor, ScheduledThreadPoolExecutor scheduledExecutor) {
        this.fixedExecutor = fixedExecutor;
        this.scheduledExecutor = scheduledExecutor;
    }

    public void setPeriodicity(Long periodicity) {
        this.periodicity = periodicity;
    }

    public Long getPeriodicity() {
        return periodicity;
    }

    public Status getStatus() {
        return status;
    }

    public void setRunUntil(Long runUntil) {
        this.runUntil = runUntil;
    }

    public void addTask(final Task t) {
        tasks.add(t);
    }

    public void removeTask(Task t, boolean forceStop) {
        if(t.getStatus() == Task.Status.Running) {
            LOG.info("Task asked to remove yet still running...");
            if(forceStop) {
                LOG.info("Attempting to force stop task...");
                t.forceStop();
            } else {
                LOG.info("Attempting to stop task...");
                t.stop();
            }
        }
        LOG.info("Removing task...");
        tasks.remove(t);
    }

    @Override
    public void run() {
        status = Status.Running;
        LOG.info(Thread.currentThread().getName()+" running...");
        while(status == Status.Running) {
            for (final Task t : tasks) {
                if(t.getPeriodicity() == -1) {
                    LOG.info("Flagged to not run, skp...");
                    continue; // Flag to not run
                }
                if(t.getStatus() == Task.Status.Completed) {
                    LOG.info("Completed, remove and skip...");
                    removeTask(t, false);
                    continue;
                }
                if(t.getScheduled()) {
//                    LOG.info("Scheduled, skip...");
                    continue;
                }
                if(t.getDelayed()) {
                    if (scheduledExecutor == null) {
                        scheduledExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(4);
                    }
                    if (t.getPeriodicity() > 0) {
                        if (t.getPeriodicity() < periodicity) {
                            // Ensure time between runs is at least the lowest task periodicity
                            periodicity = t.getPeriodicity();
                        }
                        if (t.getFixedDelay()) {
                            scheduledExecutor.scheduleWithFixedDelay(t, t.getDelayTimeMS(), t.getPeriodicity(), TimeUnit.MILLISECONDS);
                        } else {
                            scheduledExecutor.scheduleAtFixedRate(t, t.getDelayTimeMS(), t.getPeriodicity(), TimeUnit.MILLISECONDS);
                        }
                    } else {
                        scheduledExecutor.schedule(t, t.getDelayTimeMS(), TimeUnit.MILLISECONDS);
                    }
                    t.setScheduled(true);
                } else if(t.getPeriodicity() > 0) {
                    if (t.getPeriodicity() < periodicity) {
                        // Ensure time between runs is at least the lowest task periodicity
                        periodicity = t.getPeriodicity();
                    }
                    if (t.getFixedDelay()) {
                        if(t.getDelayed())
                            scheduledExecutor.scheduleWithFixedDelay(t, t.getDelayTimeMS(), t.getPeriodicity(), TimeUnit.MILLISECONDS);
                        else
                            scheduledExecutor.scheduleWithFixedDelay(t, 0, t.getPeriodicity(), TimeUnit.MILLISECONDS);
                    } else {
                        if(t.getDelayed())
                            scheduledExecutor.scheduleAtFixedRate(t, t.getDelayTimeMS(), t.getPeriodicity(), TimeUnit.MILLISECONDS);
                        else
                            scheduledExecutor.scheduleAtFixedRate(t, 0, t.getPeriodicity(), TimeUnit.MILLISECONDS);
                    }
                    t.setScheduled(true);
                } else if(!t.getScheduled()) {
                    if(t.getLongRunng()) {
                        fixedExecutor.execute(t);
                    } else {
                        t.execute();
                    }
                }
            }
//            LOG.info("Tasks ran.");
            if(runUntil > 0 && runUntil < System.currentTimeMillis()) {
                status = Status.Stopping;
            }
            if(status == Status.Running) {
                try {
                    synchronized (this) {
                        this.wait(periodicity);
                    }
                } catch (InterruptedException ex) {
                }
            }
        }
        LOG.info(Thread.currentThread().getName()+" Stopped.");
        status = Status.Shutdown;
    }

    public void shutdown() {
        LOG.info("Shutting down Task Runner...");
        status = Status.Stopping;
        fixedExecutor.shutdown();
        scheduledExecutor.shutdown();
        status = Status.Shutdown;
        LOG.info("Task Runner shutdown.");
    }

}
