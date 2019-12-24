package io.onemfive.desktop;

import io.onemfive.core.Config;
import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusObserver;
import io.onemfive.core.admin.AdminService;
import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.client.ClientStatusListener;
import io.onemfive.core.client.SimpleClient;
import io.onemfive.core.util.SystemSettings;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.DLC;
import io.onemfive.network.NetworkService;
import io.onemfive.network.sensors.clearnet.server.ClearnetServerUtil;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Dapp extends Application {

    private static final Logger LOG = Logger.getLogger(Dapp.class.getName());

    public enum Status {Shutdown, Initializing, Initialized, Starting, Running, ShuttingDown, Errored, Exiting}

    private static final Dapp instance = new Dapp();

    private static OneMFiveAppContext oneMFiveAppContext;
    private static ClientAppManager manager;
    private static ClientAppManager.Status clientAppManagerStatus;
    private static Client client;
    private static Properties config;
    private static boolean waiting = true;
    private static boolean running = false;
    private static Scanner scanner;
    private static Status status = Status.Shutdown;
    private static boolean useTray = false;
    private static DAppTray tray;
    private static int uiPort;
    private boolean peerDiscoveryStarted = false;
    private ServiceStatus networkServiceStatus = ServiceStatus.SHUTDOWN;

    public static File rootDir;
    public static File userAppDataDir;
    public static File userAppConfigDir;
    public static File userAppCacheDir;

    private static Consumer<Application> appLaunchedHandler;
    private Stage stage;

    public static void main(String[] args) {
        try {
            init(args);
        } catch (Exception e) {
            System.out.print(e.getLocalizedMessage());
            System.exit(-1);
        }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        appLaunchedHandler.accept(this);
    }

    public static void init(String[] args) throws Exception {
        System.out.println("Welcome to Inkrypt DCDN Dapp. Initializing DCDN Service...");
        status = Status.Initializing;
        Properties p = new Properties();
        String[] parts;
        for(String arg : args) {
            LOG.info("JVM Arg: "+arg);
            parts = arg.split("=");
            p.setProperty(parts[0],parts[1]);
        }

        loadLoggingProperties(p);

        try {
            config = Config.loadFromClasspath("inkrypt-dcdn-dapp.config", p, false);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(-1);
        }

        LOG.info("Inkrypt DCDN Version: "+config.getProperty("inkrypt.version")+ " build "+config.getProperty("inkrypt.version.build"));

        // Launch Tray
        useTray = Boolean.parseBoolean(config.getProperty("tray"));
        if(useTray) {
            tray = new DAppTray();
            tray.start(instance);
            tray.updateStatus(DAppTray.INITIALIZING);
        }

        // UI port
        String clConfig = config.getProperty("1m5.sensors.clearnet.server.config");
        if(clConfig!=null) {
            String[] clConfigParams = clConfig.split(",");
            String uiPortStr = clConfigParams[2];
            uiPort = Integer.parseInt(uiPortStr);
            LOG.info("UI Port: " + uiPortStr);
        }

        // Directories
        String rootDirStr = config.getProperty("inkrypt.dcdn.dir.base");
        if(rootDirStr!=null) {
            rootDir = new File(rootDirStr);
            if (!rootDir.exists() && !rootDir.mkdir()) {
                throw new Exception("Unable to create supplied inkrypt.dcdn.dir.base directory: " + rootDirStr);
            }
        } else {
            try {
                rootDir = SystemSettings.getUserAppHomeDir("inkrypt", "dcdn-dapp", true);
            } catch (IOException e) {
                throw new Exception("Unable to get user app home directory. Exception thrown: \n\t"+e.getLocalizedMessage());
            }
            if (rootDir == null) {
                rootDir = SystemSettings.getSystemApplicationDir("inkrypt", "dcdn-dapp", true);
                if(rootDir==null) {
                    throw new Exception("Unable to get or create system app home directory for inkrypt dcdn-dapp.");
                }
            }
        }

        userAppDataDir = new File(rootDir, "data");
        if(!userAppDataDir.exists() && !userAppDataDir.mkdir()) {
            throw new Exception("Unable to create user app data directory: "+rootDir.getAbsolutePath() + "/data");
        } else {
            config.setProperty("inkrypt.dcdn.dir.userAppData", userAppDataDir.getAbsolutePath());
        }

        userAppConfigDir = new File(rootDir, "config");
        if(!userAppConfigDir.exists() && !userAppConfigDir.mkdir()) {
            throw new Exception("Unable to create user app config directory: "+rootDir.getAbsolutePath() + "/config");
        } else {
            config.setProperty("inkrypt.dcdn.dir.userAppConfig", userAppConfigDir.getAbsolutePath());
        }

        userAppCacheDir = new File(rootDir, "cache");
        if(!userAppCacheDir.exists() && !userAppCacheDir.mkdir()) {
            throw new Exception("Unable to create user app cache directory: "+rootDir.getAbsolutePath() + "/cache");
        } else {
            config.setProperty("inkrypt.dcdn.dir.userAppCache", userAppCacheDir.getAbsolutePath());
        }
        LOG.info("Inkrypt DCDN Dapp Directories: " +
                "\n\tBase: " + rootDir.getAbsolutePath() +
                "\n\tData: " + userAppDataDir.getAbsolutePath() +
                "\n\tConfig: " + userAppConfigDir.getAbsolutePath() +
                "\n\tCache: " + userAppCacheDir.getAbsolutePath());

        status = Status.Initialized;
        instance.start();
    }

    public void start() {
        try {
            status = Status.Starting;
            if(useTray) {
                tray.updateStatus(DAppTray.STARTING);
            }
            instance.launch();
            running = true;
            status = Status.Running;
            // Check periodically to see if 1M5 stopped
            while (instance.clientAppManagerStatus != ClientAppManager.Status.STOPPED && running) {
                instance.waitABit(2 * 1000);
            }
            if(oneMFiveAppContext.getServiceBus().gracefulShutdown()) {
                status = Status.Shutdown;
                if(useTray) {
                    tray.updateStatus(DAppTray.STOPPED);
                }
                System.out.println("Inkrypt DCDN Dapp Stopped.");
            } else {
                status = Status.Errored;
                if(useTray) {
                    tray.updateStatus(DAppTray.ERRORED);
                }
                System.out.println("Inkrypt DCDN Dapp Errored on Shutdown.");
            }
            OneMFiveAppContext.clearGlobalContext(); // Make sure we don't use the old context when restarting
        } catch (Exception e) {
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        }
    }

    @Override
    public void stop() {
//        if (!shutDownRequested) {
//            new Popup<>().headLine(Res.get("popup.shutDownInProgress.headline"))
//                    .backgroundInfo(Res.get("popup.shutDownInProgress.msg"))
//                    .hideCloseButton()
//                    .useAnimation(false)
//                    .show();
//            UserThread.runAfter(() -> {
//                gracefulShutDownHandler.gracefulShutDown(() -> {
//                    log.debug("App shutdown complete");
//                });
//            }, 200, TimeUnit.MILLISECONDS);
//            shutDownRequested = true;
//        }
    }

    public void shutdown() {
        status = Status.ShuttingDown;
        System.out.println("Inkrypt DCDN Dapp Shutting Down...");
        running = false;
    }

    public void exit() {
        System.out.println("Inkrypt DCDN Dapp Exiting...");
        status = Status.Exiting;
        running = false;
        waiting = false;
        System.exit(0);
    }

    public void launchUI() {
        ClearnetServerUtil.launchBrowser("http://127.0.0.1:"+uiPort+"/");
    }

    private void launch() throws Exception {
        // Getting ClientAppManager starts 1M5 Bus
        oneMFiveAppContext = OneMFiveAppContext.getInstance(config);
        manager = oneMFiveAppContext.getClientAppManager(config);
        manager.setShutdownOnLastUnregister(true);
        client = manager.getClient(true);

        ClientStatusListener clientStatusListener = new ClientStatusListener() {
            @Override
            public void clientStatusChanged(ClientAppManager.Status clientStatus) {
                clientAppManagerStatus = clientStatus;
                LOG.info("Client Status changed: "+clientStatus.name());
                switch(clientAppManagerStatus) {
                    case INITIALIZING: {
                        LOG.info("Client initializing...");
                        break;
                    }
                    case READY: {
                        LOG.info("Client ready.");
                        break;
                    }
                    case STOPPING: {
                        LOG.info("Client stopping...");
                        break;
                    }
                    case STOPPED: {
                        LOG.info("Client stopped.");
                        break;
                    }
                }
            }
        };
        client.registerClientStatusListener(clientStatusListener);
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                manager.unregister(client);
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // wait a second to let the bus and internal services start
        waitABit(1000);

        // Initialize and configure 1M5
        Envelope e = Envelope.documentFactory();

        // Setup Service Status Observer
        Map<String, List<ServiceStatusObserver>> serviceStatusObservers = new HashMap<>();
        List<ServiceStatusObserver> networkServiceStatusObservers = new ArrayList<>();
        serviceStatusObservers.put(NetworkService.class.getName(), networkServiceStatusObservers);
        ServiceStatusObserver networkServiceStatusObserver = new ServiceStatusObserver() {
            @Override
            public void statusUpdated(ServiceStatus serviceStatus) {
                if(networkServiceStatus != ServiceStatus.RUNNING && serviceStatus == ServiceStatus.RUNNING) {
                    LOG.info("1M5 Network Service reporting Running. Connecting to 1M5 Network...");
                }
                networkServiceStatus = serviceStatus;
                if(serviceStatus == ServiceStatus.RUNNING) {
                    if(useTray) {
                        tray.updateStatus(DAppTray.CONNECTING);
                    }
                } else if(serviceStatus == ServiceStatus.PARTIALLY_RUNNING) {
                    LOG.info("1M5 Sensor Service reporting Partially Running. Updating status to Reconnecting...");
                    if(useTray) {
                        tray.updateStatus(DAppTray.RECONNECTING);
                    }
                } else if(serviceStatus == ServiceStatus.DEGRADED_RUNNING) {
                    LOG.info("1M5 Sensor Service reporting Degraded Running. Updating status to Blocked.");
                    if(useTray) {
                        tray.updateStatus(DAppTray.BLOCKED);
                    }
                }
            }
        };
        networkServiceStatusObservers.add(networkServiceStatusObserver);

        DLC.addData(ServiceStatusObserver.class, serviceStatusObservers, e);

        // Register Services
        DLC.addRoute(AdminService.class, AdminService.OPERATION_REGISTER_SERVICES,e);
        client.request(e);
    }

    private static void waitABit(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {}
    }

    private static boolean loadLoggingProperties(Properties p) {
        String logPropsPathStr = p.getProperty("java.util.logging.config.file");
        if(logPropsPathStr != null) {
            File logPropsPathFile = new File(logPropsPathStr);
            if(logPropsPathFile.exists()) {
                try {
                    FileInputStream logPropsPath = new FileInputStream(logPropsPathFile);
                    LogManager.getLogManager().readConfiguration(logPropsPath);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
