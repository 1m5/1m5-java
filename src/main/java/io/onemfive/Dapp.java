package io.onemfive;

import dorkbox.util.OS;
import io.onemfive.core.Config;
import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusObserver;
import io.onemfive.core.admin.AdminService;
import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.client.ClientStatusListener;
import io.onemfive.core.util.SystemSettings;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.DLC;
import io.onemfive.desktop.DesktopApp;
import io.onemfive.network.NetworkService;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Dapp {

    private static final Logger LOG = Logger.getLogger(Dapp.class.getName());

    public enum Status {Shutdown, Initializing, Initialized, Starting, Running, ShuttingDown, Errored, Exiting}

    private static final Dapp instance = new Dapp();

    private static OneMFiveAppContext oneMFiveAppContext;
    private static ClientAppManager manager;
    private static ClientAppManager.Status clientAppManagerStatus;
    private static Client client;
    private static DesktopApp desktopApp;
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

    public static void main(String[] args) {
        try {
            init(args);
        } catch (Exception e) {
            System.out.print(e.getLocalizedMessage());
            System.exit(-1);
        }
    }

    public static void init(String[] args) throws Exception {
        System.out.println("Welcome to 1M5. Initializing...");
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
            config = Config.loadFromClasspath("1m5.config", p, false);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(-1);
        }

        LOG.info("1M5 Version: "+config.getProperty("1m5.version")+"."+config.getProperty("1m5.version.build"));

        // Launch Tray
        tray = new DAppTray();
        tray.start(instance);
        status = Status.Initialized;
        instance.start();
    }

    public void start() {
        try {
            status = Status.Starting;
            tray.updateStatus(DAppTray.STARTING);
            instance.launch();
            running = true;
            status = Status.Running;
            // Check periodically to see if 1M5 stopped
            while (instance.clientAppManagerStatus != ClientAppManager.Status.STOPPED && running) {
                instance.waitABit(2 * 1000);
            }
            if(oneMFiveAppContext.getServiceBus().gracefulShutdown()) {
                status = Status.Shutdown;
                tray.updateStatus(DAppTray.STOPPED);
                System.out.println("1M5 Dapp Stopped.");
            } else {
                status = Status.Errored;
                tray.updateStatus(DAppTray.ERRORED);
                System.out.println("1M5 Dapp Errored on Shutdown.");
            }
            OneMFiveAppContext.clearGlobalContext(); // Make sure we don't use the old context when restarting
        } catch (Exception e) {
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        }
    }

    public void shutdown() {
        status = Status.ShuttingDown;
        System.out.println("1M5 Dapp Shutting Down...");
        running = false;
    }

    public void exit() {
        System.out.println("1M5 Dapp Exiting...");
        status = Status.Exiting;
        running = false;
        waiting = false;
        System.exit(0);
    }

    public void launchUI() {
//        ClearnetServerUtil.launchBrowser("http://127.0.0.1:"+uiPort+"/");
        if (OS.isMacOsX() && OS.javaVersion <= 7) {
            System.setProperty("javafx.macosx.embedded", "true");
            java.awt.Toolkit.getDefaultToolkit();
        }
        DesktopApp.init(client, tray);
        DesktopApp.launch(DesktopApp.class);
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
                        LOG.info("Dapp starting...");
                        tray.updateStatus(DAppTray.STARTING);
                        break;
                    }
                    case READY: {
                        LOG.info("Dapp connected.");
                        tray.updateStatus(DAppTray.CONNECTED);
                        break;
                    }
                    case STOPPING: {
                        LOG.info("Dapp stopping...");
                        tray.updateStatus(DAppTray.SHUTTINGDOWN);
                        break;
                    }
                    case STOPPED: {
                        LOG.info("Dapp stopped.");
                        tray.updateStatus(DAppTray.STOPPED);
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
                    tray.updateStatus(DAppTray.CONNECTED);
                } else if(serviceStatus == ServiceStatus.PARTIALLY_RUNNING) {
                    LOG.info("1M5 Sensor Service reporting Partially Running. Updating status to Reconnecting...");
                    tray.updateStatus(DAppTray.RECONNECTING);
                } else if(serviceStatus == ServiceStatus.DEGRADED_RUNNING) {
                    LOG.info("1M5 Sensor Service reporting Degraded Running. Updating status to Reconnecting...");
                    tray.updateStatus(DAppTray.DEGRADED);
                } else if(serviceStatus == ServiceStatus.BLOCKED) {
                    LOG.info("1M5 Sensor Service reporting Degraded Running. Updating status to Blocked.");
                    tray.updateStatus(DAppTray.BLOCKED);
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
