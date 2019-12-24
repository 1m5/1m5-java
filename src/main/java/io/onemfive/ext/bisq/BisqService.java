package io.onemfive.ext.bisq;

import io.onemfive.core.BaseService;
import io.onemfive.core.ServiceStatus;
import io.onemfive.data.Envelope;
import io.onemfive.data.route.Route;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Service for providing access to the Bisq network
 *
 * @author objectorange
 */
public class BisqService extends BaseService {

    private static final Logger LOG = Logger.getLogger(BisqService.class.getName());

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        String operation = route.getOperation();
        switch(operation) {
		default: deadLetter(e); // Operation not supported
        }
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

    public static void main(String[] args) {
        BisqService service = new BisqService();
        Properties props = new Properties();
        for(String arg : args) {
            String[] nvp = arg.split("=");
            props.put(nvp[0],nvp[1]);
        }
        if(service.start(props)) {
            while(service.getServiceStatus() != ServiceStatus.SHUTDOWN) {
                try {
                    synchronized (service) {
                        service.wait(2 * 1000);
                    }
                } catch (InterruptedException e) {
                    System.exit(0);
                }
            }
        } else {
            System.exit(-1);
        }
    }
}
