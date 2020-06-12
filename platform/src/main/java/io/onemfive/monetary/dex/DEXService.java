package io.onemfive.monetary.dex;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.data.Envelope;
import io.onemfive.data.route.Route;
import io.onemfive.monetary.dex.offer.Offer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Service for providing multi-sig escrow
 *
 */
public class DEXService extends BaseService {

    private static final Logger LOG = Logger.getLogger(DEXService.class.getName());

    public static final String OPERATION_PLACE_OFFER = "PLACE_OFFER";
    public static final String OPERATION_GET_OFFERS = "GET_OFFERS";

    public DEXService() {
    }

    public DEXService(MessageProducer producer, ServiceStatusListener listener) {
        super(producer, listener);
    }

    @Override
    public void handleDocument(Envelope e) {
        LOG.warning("Not yet implemented.");
        Route route = e.getRoute();
        String operation = route.getOperation();
        switch(operation) {
            case OPERATION_PLACE_OFFER: { placeOffer(e); break; }
            case OPERATION_GET_OFFERS: { getOffers(e); break; }
		    default: deadLetter(e); // Operation not supported
        }
    }

    private void getOffers(Envelope e) {

    }

    private void placeOffer(Envelope e) {

    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Starting....");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);


        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        LOG.info("Gracefully shutting down...");
        updateStatus(ServiceStatus.GRACEFULLY_SHUTTING_DOWN);


        updateStatus(ServiceStatus.GRACEFULLY_SHUTDOWN);
        LOG.info("Gracefully shutdown.");
        return true;
    }

}
