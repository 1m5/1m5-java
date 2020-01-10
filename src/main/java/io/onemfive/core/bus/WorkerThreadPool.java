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
package io.onemfive.core.bus;

import io.onemfive.core.BaseService;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.util.AppThread;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Thread pool for WorkerThreads.
 *
 * TODO: Improve teardown
 * TODO: Improve configuration options
 *
 * @author objectorange
 */
final class WorkerThreadPool extends AppThread {

    private static final Logger LOG = Logger.getLogger(WorkerThreadPool.class.getName());

    public enum Status {Starting, Running, Stopping, Stopped}

    private Status status = Status.Stopped;

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final ClientAppManager clientAppManager;
    private Map<String,BaseService> services;
    private MessageChannel channel;
    private ExecutorService pool;
    private int poolSize = NUMBER_OF_CORES * 2; // default
    private int maxPoolSize = NUMBER_OF_CORES * 2; // default
    private Properties properties;
    private AtomicBoolean spin = new AtomicBoolean(true);

    WorkerThreadPool(ClientAppManager clientAppManager, Map<String, BaseService> services, MessageChannel channel, int poolSize, int maxPoolSize, Properties properties) {
        this.clientAppManager = clientAppManager;
        this.services = services;
        this.channel = channel;
        this.poolSize = poolSize;
        this.maxPoolSize = maxPoolSize;
        this.properties = properties;
    }

    @Override
    public void run() {
        startPool();
        status = Status.Stopped;
    }

    private boolean startPool() {
        int index = 0;
        status = Status.Starting;
        pool = Executors.newFixedThreadPool(maxPoolSize);
        status = Status.Running;
        final long printPeriodMs = 5000; // print * every 5 seconds
        final long waitPeriodMs = 500; // wait half a second
        long currentWait = 0;
        while(spin.get()) {
            synchronized (this){
                try {
                    if(currentWait > printPeriodMs) {
                        LOG.finest("*");
                        currentWait = 0;
                    }
                    int queueSize = channel.getQueue().size();
                    if(queueSize > 0) {
                        LOG.finest("Queue Size = "+queueSize+" : Launching thread...");
                        pool.execute(new WorkerThread(channel, clientAppManager, services));
                    } else {
                        currentWait += waitPeriodMs;
                        this.wait(waitPeriodMs); // wait 500ms
                    }
                } catch (InterruptedException e) {

                }
            }
        }
        return true;
    }

    boolean shutdown() {
        status = Status.Stopping;
        spin.set(false);
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                // pool didn't terminate after the first try
                pool.shutdownNow();
            }


            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                // pool didn't terminate after the second try
            }
        } catch (InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        status = Status.Stopped;
        return true;
    }

    public Status getStatus() {
        return status;
    }
}
