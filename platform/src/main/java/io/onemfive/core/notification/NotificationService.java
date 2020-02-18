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
package io.onemfive.core.notification;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.util.AppThread;
import io.onemfive.data.*;
import io.onemfive.data.route.Route;
import io.onemfive.util.DLC;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

/**
 * Provides notifications of publishing events for subscribers.
 *
 * TODO: Replace callbacks with service calls to improve scalability and thread contention
 *
 * @author objectorange
 */
public class NotificationService extends BaseService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    /**
     * To subscribe to EventMessages, send a SubscriptionRequest as a DocumentMessage to Service using
     * OPERATION_SUBSCRIBE as operation. SubscriptionRequest must specify EventMessage.Type and optionally a Filter.
     *
     * Filters available for each EventMessage.Type:
     *
     * EMAIL: Internal filtering automatic based on end user's owned DIDs.
     * EXCEPTION: Internal filtering automatically; Client exceptions can be subscribed to by Clients (not yet implemented).
     * ERROR: No filters supported
     * STATUS_SENSOR: String representing full name of Sensor class, e.g. io.onemfive.core.sensors.i2p.I2PSensor
     * STATUS_SERVICE: String representing full name of Service class, e.g. io.onemfive.core.sensors.SensorService
     * STATUS_BUS: No filters supported
     * STATUS_CLIENT: No filters supported
     * STATUS_DID: Identity hash
     * TEXT: Can filter by name if provided. For I2P messages, the name is the sender's base64 encoded key.
     *
     */
    public static final String OPERATION_SUBSCRIBE = "SUBSCRIBE";
    public static final String OPERATION_UNSUBSCRIBE = "UNSUBSCRIBE";
    /**
     * To publish an EventMessage, ensure the Envelope contains one.
     */
    public static final String OPERATION_PUBLISH = "PUBLISH";

    private ExecutorService pool = Executors.newFixedThreadPool(4);

    private Map<String,Map<String,List<Subscription>>> subscriptions;

    public NotificationService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route r = e.getRoute();
        String operation = r.getOperation();
        switch(operation) {
            case OPERATION_SUBSCRIBE:{subscribe(e);break;}
            case OPERATION_UNSUBSCRIBE:{unsubscribe(e);break;}
            default: deadLetter(e);
        }
    }

    @Override
    public void handleEvent(Envelope e) {
        Route r = e.getRoute();
        String operation = r.getOperation();
        switch(operation) {
            case OPERATION_PUBLISH:{publish(e);break;}
            default: deadLetter(e);
        }
    }

    private void subscribe(Envelope e) {
        LOG.fine("Received subscribe request...");
        SubscriptionRequest r = (SubscriptionRequest)DLC.getData(SubscriptionRequest.class,e);
        LOG.fine("Subscription for type: "+r.getType().name());
        Map<String,List<Subscription>> s = subscriptions.get(r.getType().name());
        if(r.getFilter() == null) {
            LOG.fine("With no filters.");
            s.get("|").add(r.getSubscription());
        } else {
            LOG.fine("With filter: "+r.getFilter());
            if(s.get(r.getFilter()) == null)
                s.put(r.getFilter(), new ArrayList<>());
            s.get(r.getFilter()).add(r.getSubscription());
        }
        LOG.fine("Subscription added.");
    }

    private void unsubscribe(Envelope e) {
        LOG.info("Received unsubscribe request...");
        SubscriptionRequest r = (SubscriptionRequest)DLC.getData(SubscriptionRequest.class,e);
        Map<String,List<Subscription>> s = subscriptions.get(r.getType().name());
        if(r.getFilter() == null) {
            s.get("|").remove(r.getSubscription());
        } else {
            s.get(r.getFilter()).remove(r.getSubscription());
        }
        LOG.info("Subscription removed.");
    }

    private void publish(final Envelope e) {
        LOG.fine("Received publish request...");
        EventMessage m = (EventMessage)e.getMessage();
        LOG.fine("For type: "+m.getType());
        Map<String,List<Subscription>> s = subscriptions.get(m.getType());
        if(s == null || s.size() == 0) {
            LOG.fine("No subscriptions for type: "+m.getType());
            return;
        }
        final List<Subscription> subs = s.get("|");
        if(subs == null || subs.size() == 0) {
            LOG.fine("No subscriptions without filters.");
        } else {
            LOG.fine("Notify all "+subs.size()+" unfiltered subscriptions.");
            for(final Subscription sub: subs) {
                pool.execute(() -> sub.notifyOfEvent(e));
            }
        }
//        LOG.info("With name to filter on: " + m.getName());
        final List<Subscription> filteredSubs = s.get(m.getName());
        if(filteredSubs == null || filteredSubs.size() == 0) {
            LOG.fine("No subscriptions for filter: "+m.getName());
        } else {
            LOG.fine("Notify all "+filteredSubs.size()+" filtered subscriptions.");
            for(final Subscription sub: filteredSubs) {
                pool.execute(() -> sub.notifyOfEvent(e));
            }
        }
    }

    private Map<String,List<Subscription>> buildNewMap() {
        List<Subscription> l = new ArrayList<>();
        Map<String,List<Subscription>> m = new HashMap<>();
        m.put("|",l);
        return m;
    }

    @Override
    public boolean start(Properties properties) {
        super.start(properties);
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        subscriptions = new HashMap<>();
        // For each EventMessage.Type, set a HashMap<String,Subscription>
        // and add a null filtered list for Subscriptions with no filters.

        subscriptions.put(EventMessage.Type.JSON.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.HTML.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.EMAIL.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.EXCEPTION.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.ERROR.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.BUS_STATUS.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.CLIENT_STATUS.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.PEER_STATUS.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.NETWORK_STATE_UPDATE.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.SENSOR_STATUS.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.SERVICE_STATUS.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.TEXT.name(), buildNewMap());

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down....");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        // TODO:
        return shutdown();
    }
}
