package io.onemfive.monetary.btc;

import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.data.route.Route;
import io.onemfive.monetary.btc.blockchain.BlockChain;
import io.onemfive.monetary.btc.blockstore.BlockStore;
import io.onemfive.monetary.btc.config.BitcoinConfig;
import io.onemfive.monetary.btc.network.*;
import io.onemfive.monetary.btc.requests.SendRequest;
import io.onemfive.monetary.btc.wallet.Wallet;
import io.onemfive.core.BaseService;
import io.onemfive.core.ServiceStatus;
import io.onemfive.data.Envelope;
import io.onemfive.util.DLC;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Service for providing access to the Bitcoin network
 *
 */
public class BitcoinService extends BaseService {

    private static final Logger LOG = Logger.getLogger(BitcoinService.class.getName());

    public static final String OPERATION_SEND = "SEND";

    private BlockChain blockChain;
    private BlockStore blockStore;
    private BitcoinPeerDiscovery peerDiscovery;
    private Wallet wallet;

    private BitcoinConfig config;

    public BitcoinService() {
    }

    public BitcoinService(MessageProducer producer, ServiceStatusListener listener) {
        super(producer, listener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        String operation = route.getOperation();
        switch(operation) {
            case OPERATION_SEND: {
                SendRequest request = (SendRequest)DLC.getData(SendRequest.class,e);
//                if(!bitcoin.send(request)) {
//                    LOG.warning("Issue sending BTC to "+request.base58To);
//                }
                break;
            }
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
        BitcoinService service = new BitcoinService();
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
