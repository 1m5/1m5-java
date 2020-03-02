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
package io.onemfive.monetary.xmr;

import io.onemfive.core.*;
import io.onemfive.data.Envelope;
import io.onemfive.data.route.Route;
import io.onemfive.util.DLC;
//import monero.daemon.MoneroDaemon;
//import monero.daemon.MoneroDaemonRpc;
//import monero.wallet.MoneroWalletRpc;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Integration with Monero using:
 * https://github.com/monero-ecosystem/monero-java
 */
public class MoneroService extends BaseService {

    private static final Logger LOG = Logger.getLogger(MoneroService.class.getName());

    public static final String OPERATION_SIGNIN_WALLET = "SIGNIN_WALLET";

    public static final String OPERATION_SET_MINING_ADDRESS = "SET_MINING_ADDRESS";
    public static final String OPERATION_START_MINER = "START_MINER";
    public static final String OPERATION_STOP_MINER = "STOP_MINER";

    private Properties properties;

//    private MoneroDaemon daemon;
//    private MoneroWalletRpc walletRpc;
    private String miningAddress;
    private static long NUM_MINING_THREADS = Runtime.getRuntime().availableProcessors();

    public MoneroService() {
    }

    public MoneroService(MessageProducer producer, ServiceStatusListener listener) {
        super(producer, listener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route r = e.getRoute();
        switch(r.getOperation()) {
            case OPERATION_SIGNIN_WALLET: {

                break;
            }
            case OPERATION_SET_MINING_ADDRESS: {
                String miningAddr = (String)DLC.getValue("miningAddress", e);
                if(miningAddr!=null) {
                    miningAddress = miningAddr;
                }
                break;
            }
            case OPERATION_START_MINER: {
                if(miningAddress==null) {
                    DLC.addNVP("monero-error", "Mining address not yet set.", e);
                    break;
                }
//                daemon.startMining(miningAddress, NUM_MINING_THREADS, true, false);
                break;
            }
            case OPERATION_STOP_MINER: {
//                daemon.stopMining();
                break;
            }
            default: deadLetter(e);
        }
    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Starting....");
        updateStatus(ServiceStatus.STARTING);
//        daemon = new MoneroDaemonRpc("http://localhost:38081");
//        LOG.info("Height: "+daemon.getHeight());
//        LOG.info("Fee Est: "+daemon.getFeeEstimate());
        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);
        if(super.shutdown()) {
//            if(daemon!=null) {
//                daemon.stop();
//            }
        }
        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        LOG.info("Gracefully shutting down...");
        updateStatus(ServiceStatus.GRACEFULLY_SHUTTING_DOWN);
        if(super.gracefulShutdown()) {
//            if(daemon!=null) {
//                daemon.stop();
//            }
        }
        updateStatus(ServiceStatus.GRACEFULLY_SHUTDOWN);
        LOG.info("Gracefully shutdown.");
        return true;
    }

}
