package onemfive;

import ra.bluetooth.BluetoothService;
import ra.common.Status;
import ra.did.DIDService;
import ra.http.HTTPService;
import ra.i2p.I2PService;
import ra.keyring.KeyRingService;
import ra.maildrop.MailDropService;
import ra.networkmanager.NetworkManagerService;
import ra.pressfreedomindex.PFIScraperService;
import ra.servicebus.ServiceBus;
import ra.tor.TORClientService;
import ra.util.Config;
import ra.util.SecureFile;
import ra.util.SystemSettings;
import ra.util.Wait;

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

        // Register Services
        try {
            bus.registerService(MailDropService.class.getName(), config);
            bus.registerService(KeyRingService.class.getName(), config);
            bus.registerService(DIDService.class.getName(), config);
            config.put("ra.http.server.configs", "1m5-api,API,2015,ra.http.EnvelopeJSONDataHandler");
            bus.registerService(HTTPService.class.getName(), config);
            bus.registerService(TORClientService.class.getName(), config);
            bus.registerService(I2PService.class.getName(), config);
            bus.registerService(BluetoothService.class.getName(), config);
            bus.registerService(NetworkManagerService.class.getName(), CRNetworkManagerService.class.getName(), config);
            bus.registerService(PFIScraperService.class.getName(), config);
        } catch (Exception e) {
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        }
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
