package io.onemfive.ext.bitcoin;

import io.onemfive.data.route.Route;
import io.onemfive.ext.bitcoin.blockchain.BlockChain;
import io.onemfive.ext.bitcoin.blockstore.BlockStore;
import io.onemfive.ext.bitcoin.config.BitcoinConfig;
import io.onemfive.ext.bitcoin.network.*;
import io.onemfive.ext.bitcoin.requests.SendRequest;
import io.onemfive.ext.bitcoin.wallet.Wallet;
import io.onemfive.core.BaseService;
import io.onemfive.core.ServiceStatus;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.DLC;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Service for providing access to the Bitcoin network
 *
 * @author objectorange
 */
public class BitcoinService extends BaseService {

    private static final Logger LOG = Logger.getLogger(BitcoinService.class.getName());

    public static final String OPERATION_SEND = "SEND";

    private BlockChain blockChain;
    private BlockStore blockStore;
    private PeerDiscovery peerDiscovery;
    private Wallet wallet;

    private BitcoinConfig config;

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
        // BitcoinConfig initialization; network property values are: main | test | dev
        if(!p.containsKey("io.onemfive.ext.bitcoin.network")) {
            LOG.severe("io.onemfive.ext.bitcoin.network parameter is required.");
            return false;
        }
        config = BitcoinConfig.getConfig(p.getProperty("io.onemfive.ext.bitcoin.network"));
        if(config==null) {
            LOG.severe("BitcoinConfig not instantiated; start failed.");
            return false;
        }

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
