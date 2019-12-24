package io.onemfive.ext.komodo;

import io.onemfive.core.BaseService;
import io.onemfive.data.Envelope;
import io.onemfive.data.Request;
import io.onemfive.data.route.Route;
import io.onemfive.data.util.DLC;

import java.util.logging.Logger;

public class KomodoService extends BaseService {

    private static final Logger LOG = Logger.getLogger(KomodoService.class.getName());

    public static final String OPERATION_SPEND = "SPEND";

    @Override
    public void handleDocument(Envelope e) {
        LOG.warning("Komodo not yet implemented.");
        Route route = e.getRoute();
        String operation = route.getOperation();
        switch(operation) {
            case OPERATION_SPEND: {
                Request request = (Request) DLC.getData(Request.class,e);
//                if(!bitcoin.send(request)) {
//                    LOG.warning("Issue sending BTC to "+request.base58To);
//                }
                break;
            }
            default: deadLetter(e); // Operation not supported
        }
    }
}
