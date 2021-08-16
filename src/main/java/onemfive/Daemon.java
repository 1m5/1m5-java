package onemfive;

//import ra.bisq.BisqClientService;
import ra.bluetooth.BluetoothService;
import ra.btc.BitcoinService;
import ra.btc.RPCCommand;
import ra.btc.rpc.RPCResponse;
import ra.btc.rpc.blockchain.GetBlockCount;
import ra.btc.rpc.blockchain.GetBlockchainInfo;
import ra.btc.rpc.control.Uptime;
import ra.btc.rpc.mining.GetNetworkHashPS;
import ra.btc.rpc.network.GetNetworkInfo;
import ra.btc.rpc.network.GetPeerInfo;
import ra.btc.rpc.util.EstimateSmartFee;
import ra.btc.rpc.wallet.*;
import ra.common.Envelope;
import ra.common.Status;
import ra.common.service.ServiceNotAccessibleException;
import ra.common.service.ServiceNotSupportedException;
import ra.dex.DEXService;
import ra.did.DIDService;
//import ra.gnuradio.GNURadioService;
import ra.http.HTTPService;
import ra.i2p.I2PService;
import ra.keyring.KeyRingService;
//import ra.lifi.LiFiService;
//import ra.maildrop.MailDropService;
import ra.maildrop.MailDropService;
import ra.networkmanager.NetworkManagerService;
import ra.notification.NotificationService;
//import ra.pressfreedomindex.PFIScraperService;
import ra.servicebus.ServiceBus;
import ra.tor.TORClientService;
import ra.util.Config;
import ra.util.SecureFile;
import ra.util.SystemSettings;
import ra.util.Wait;
//import ra.wifidirect.WiFiDirectNetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Daemon {

    private static final Logger LOG = Logger.getLogger(Daemon.class.getName());

    private static final Daemon instance = new Daemon();

    private File baseDir;
    private File configDir;
    private File libDir;
    private File pidDir;
    private File logDir;
    private File dataDir;
    private File cacheDir;
    private volatile File tmpDir;
    private final Random tmpDirRand = new Random();
    private final static Object lockA = new Object();
    private boolean initialize = false;
    private boolean configured = false;
    private Locale locale;
    private String version = null;
    // split up big lock on this to avoid deadlocks
    private final Object lock1 = new Object();

    private ServiceBus bus;
    private Properties config;
    private Status status = Status.Stopped;

    public static void main(String[] args) {
        instance.start(args);
    }

    public void start(String[] args) {
        LOG.info("Welcome to 1M5. Initializing...");
        Thread.currentThread().setName("1M5-Thread");
        LOG.info("Thread name: " + Thread.currentThread().getName());

        status = Status.Starting;

        try {
            config = Config.loadFromMainArgsAndClasspath(args, "1m5.config",false);
        } catch (Exception e) {
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        }

        String logPropsPathStr = config.getProperty("java.util.logging.config.file");
        if(logPropsPathStr != null) {
            File logPropsPathFile = new File(logPropsPathStr);
            if(logPropsPathFile.exists()) {
                try {
                    FileInputStream logPropsPath = new FileInputStream(logPropsPathFile);
                    LogManager.getLogManager().readConfiguration(logPropsPath);
                } catch (IOException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                }
            }
        }

        version = config.getProperty("1m5.version")+"."+config.getProperty("1m5.version.build");
        LOG.info("1M5 Version: "+version);
        System.setProperty("1m5.version", version);

        System.setProperty("1m5.userTimeZone", TimeZone.getDefault().getID());

        String systemTimeZone = config.getProperty("1m5.systemTimeZone");
        LOG.info("1M5 System Time Zone: "+systemTimeZone);
        TimeZone.setDefault(TimeZone.getTimeZone(systemTimeZone));
        System.setProperty("1m5.systemTimeZone",systemTimeZone);

        String baseStr = null;
        try {
            baseDir = SystemSettings.getUserAppHomeDir(".1m5","platform",true);
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
            return;
        }
        if(baseDir!=null) {
            config.put("1m5.dir.base", baseDir.getAbsolutePath());
        } else {
            baseDir = SystemSettings.getSystemApplicationDir(".1m5", "platform", true);
            if (baseDir == null) {
                LOG.severe("Unable to create base system directory for 1M5 platform.");
                return;
            } else {
                baseStr = baseDir.getAbsolutePath();
                config.put("1m5.dir.base", baseStr);
            }
        }

        configDir = new SecureFile(baseDir, "config");
        if(!configDir.exists() && !configDir.mkdir()) {
            LOG.severe("Unable to create config directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.config",configDir.getAbsolutePath());
        }

        libDir = new SecureFile(baseDir, "lib");
        if(!libDir.exists() && !libDir.mkdir()) {
            LOG.severe("Unable to create lib directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.lib",libDir.getAbsolutePath());
        }

        dataDir = new SecureFile(baseDir, "data");
        if(!dataDir.exists() && !dataDir.mkdir()) {
            LOG.severe("Unable to create data directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.data",dataDir.getAbsolutePath());
        }

        cacheDir = new SecureFile(baseDir, "cache");
        if(!cacheDir.exists() && !cacheDir.mkdir()) {
            LOG.severe("Unable to create cache directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.cache",cacheDir.getAbsolutePath());
        }

        pidDir = new SecureFile(baseDir, "pid");
        if (!pidDir.exists() && !pidDir.mkdir()) {
            LOG.severe("Unable to create pid directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.pid",pidDir.getAbsolutePath());
        }

        logDir = new SecureFile(baseDir, "logs");
        if (!logDir.exists() && !logDir.mkdir()) {
            LOG.severe("Unable to create logs directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.log",logDir.getAbsolutePath());
        }

        tmpDir = new SecureFile(baseDir, "tmp");
        if (!tmpDir.exists() && !tmpDir.mkdir()) {
            LOG.severe("Unable to create tmp directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.temp",tmpDir.getAbsolutePath());
        }

        LOG.info("1M5 Directories: " +
                "\n\tBase: "+baseDir.getAbsolutePath()+
                "\n\tConfig: "+configDir.getAbsolutePath()+
                "\n\tData: "+dataDir.getAbsolutePath()+
                "\n\tCache: "+cacheDir.getAbsolutePath()+
                "\n\tPID: "+pidDir.getAbsolutePath()+
                "\n\tLogs: "+logDir.getAbsolutePath()+
                "\n\tTemp: "+tmpDir.getAbsolutePath());

        bus = new ServiceBus(config);
        bus.start(config);

        // Configure HTTP API
//        config.put("","");

        // Register supported services
        try {
            // Core Services
            bus.registerService(MailDropService.class.getName(), config);
            bus.registerService(NotificationService.class.getName(), config);
            bus.registerService(DIDService.class.getName(), config);
            bus.registerService(KeyRingService.class.getName(), config);
            // Networking Services
            bus.registerService(NetworkManagerService.class.getName(), config);
            // TODO: Upgrade to CR Network Manager Service
//            bus.registerService(NetworkManagerService.class.getName(), CRNetworkManagerService.class.getName(), config);
            bus.registerService(HTTPService.class.getName(), config);
            bus.registerService(TORClientService.class.getName(), config);
            bus.registerService(I2PService.class.getName(), config);
            bus.registerService(BluetoothService.class.getName(), config);
//            bus.registerService(WiFiDirectNetwork.class.getName(), config);
//            bus.registerService(GNURadioService.class.getName(), config);
//            bus.registerService(LiFiService.class.getName(), config);
            // Additional Services
//            bus.registerService(PFIScraperService.class.getName(), config);
            bus.registerService(BitcoinService.class.getName(), config);
            bus.registerService(DEXService.class.getName(), config);
        } catch (ServiceNotAccessibleException e) {
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        } catch (ServiceNotSupportedException e) {
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        }

        // Start infrastructure services
        bus.startService(MailDropService.class.getName());
        bus.startService(NotificationService.class.getName());
        bus.startService(DIDService.class.getName());
        bus.startService(KeyRingService.class.getName());
        bus.startService(NetworkManagerService.class.getName());
        bus.startService(HTTPService.class.getName()); // for localhost
//        bus.startService(TORClientService.class.getName());
//        bus.startService(I2PService.class.getName());
//        bus.startService(BluetoothService.class.getName());

        // Start available services
        Wait.aSec(3);
        bus.startService(BitcoinService.class.getName());
        bus.startService(DEXService.class.getName());

//        Envelope e = Envelope.documentFactory();
//        e.addRoute(BitcoinService.class.getName(), BitcoinService.OPERATION_RPC_REQUEST);
        // Send to establish initial info
//        e.addNVP(RPCCommand.NAME, new GetBlockchainInfo());
//        e.addNVP(RPCCommand.NAME, new Uptime());
//        e.addNVP(RPCCommand.NAME, new GetNetworkHashPS());
//        e.addNVP(RPCCommand.NAME, new ListWallets());
//        e.addNVP(RPCCommand.NAME, new EstimateSmartFee(3));
//        e.addNVP(RPCCommand.NAME, new GetBlockCount());
//        e.addNVP(RPCCommand.NAME, new GetNewAddress());
//        e.addNVP(RPCCommand.NAME, new CreateWallet("Personal","1234"));
//        e.addNVP(RPCCommand.NAME, new LoadWallet("Personal"));
//        e.addNVP(RPCCommand.NAME, new UnloadWallet("Personal"));
//        e.addNVP(RPCCommand.NAME, new GetWalletInfo());
//        e.addNVP(RPCCommand.NAME, new GetBalance());

//        e.addNVP(RPCCommand.NAME, new GetPeerInfo());
//        e.addNVP(RPCCommand.NAME, new GetNetworkInfo());

//        bus.send(e);

//        LOG.info(((RPCResponse)e.getValue(RPCCommand.RESPONSE)).toJSON());

        status = Status.Running;

        // Check periodically to see if 1M5 stopped
        while (status == Status.Running) {
            Wait.aSec(2);
        }

        shutdown();

        System.exit(0);
    }

    public void shutdown() {
        status = Status.Stopping;
        LOG.info("1M5 Shutting Down...");
        if(!bus.gracefulShutdown()) {
            LOG.warning("Service Bus Graceful Shutdown failed.");
        }
        status = Status.Stopped;
    }

}
