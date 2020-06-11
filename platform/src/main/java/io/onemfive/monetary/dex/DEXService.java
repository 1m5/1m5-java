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
 * @author objectorange
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
