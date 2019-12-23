package io.onemfive.api;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.core.bus.ServiceBus;
import io.onemfive.data.Envelope;
import io.onemfive.data.route.Route;

import java.util.logging.Logger;

public class APIService extends BaseService {

    private static final Logger LOG = Logger.getLogger(APIService.class.getName());

    public static final String OPERATION_SEND_MESSAGE = "SEND_MESSAGE";
    public static final String OPERATION_REGISTER_LISTENER = "REGISTER_LISTENER";

    private ServiceBus serviceBus;

    public APIService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
        serviceBus = (ServiceBus)producer;
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        switch(route.getOperation()) {
            case OPERATION_SEND_MESSAGE:{sendMessage(e);break;}
            case OPERATION_REGISTER_LISTENER:{registerListener(e);break;}
            default: deadLetter(e);
        }
    }

    private void sendMessage(Envelope e) {

    }

    private void registerListener(Envelope e) {

    }
}
