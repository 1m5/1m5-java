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
import io.onemfive.core.MessageConsumer;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.orchestration.OrchestrationService;
import io.onemfive.util.AppThread;
import io.onemfive.data.Envelope;
import io.onemfive.data.route.Route;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Worker Thread for moving messages from clients to the message channel and then to services and back.
 *
 * @author objectorange
 */
final class WorkerThread extends AppThread {

    private static final Logger LOG = Logger.getLogger(WorkerThread.class.getName());

    private MessageChannel channel;
    private ClientAppManager clientAppManager;
    private Map<String, BaseService> services;

    public WorkerThread(MessageChannel channel, ClientAppManager clientAppManager, Map<String, BaseService> services) {
        super();
        this.channel = channel;
        this.clientAppManager = clientAppManager;
        this.services = services;
    }

    @Override
    public void run() {
        LOG.finer(Thread.currentThread().getName() + ": Channel waiting to receive next message...");
        Envelope e = channel.receive();
        LOG.finer(Thread.currentThread().getName() + ": Channel received message; processing...");
        if (e.replyToClient()) {
            // Service Reply to client
            LOG.finer(Thread.currentThread().getName() + ": Requesting client notify...");
            clientAppManager.notify(e);
        } else {
            MessageConsumer consumer = null;
            Route route = e.getRoute();
            if(route == null || route.getRouted()) {
                consumer = services.get(OrchestrationService.class.getName());
            } else {
                consumer = services.get(route.getService());
                if (consumer == null) {
                    // Service name provided is not registered.
                    LOG.warning(Thread.currentThread().getName() + ": Route found in header; Service not registered; Please register service: "+route.getService()+"\n\tCurrent Registered Services: "+services);
                    return;
                }
            }
            boolean received = false;
            int maxSendAttempts = 3;
            int sendAttempts = 0;
            int waitBetweenMillis = 1000;
            while (!received && sendAttempts < maxSendAttempts) {
                if (consumer.receive(e)) {
                    LOG.finer(Thread.currentThread().getName() + ": Envelope received by service, acknowledging with channel...");
                    channel.ack(e);
                    LOG.finer(Thread.currentThread().getName() + ": Channel Acknowledged.");
                    received = true;
                } else {
                    synchronized (this) {
                        try {
                            this.wait(waitBetweenMillis);
                        } catch (InterruptedException ex) {

                        }
                    }
                }
                sendAttempts++;
            }
            if(!received) {
                // TODO: Need to move the failed Envelope to a log where it can be retried later
                LOG.warning("Failed 3 attempts to send Envelope (id="+e.getId()+") to Service: ");
            }
        }
    }
}
