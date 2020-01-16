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
package io.onemfive;

import io.onemfive.core.Config;
import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusObserver;
import io.onemfive.core.admin.AdminService;
import io.onemfive.core.client.Client;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.client.ClientStatusListener;
import io.onemfive.data.ServiceCallback;
import io.onemfive.desktop.DesktopTray;
import io.onemfive.data.Envelope;
import io.onemfive.util.DLC;
import io.onemfive.desktop.DesktopApp;
import io.onemfive.network.NetworkService;
import javafx.application.Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Daemon {

    private static final Logger LOG = Logger.getLogger(Daemon.class.getName());

    public enum Status {Shutdown, Initializing, Initialized, Starting, Running, ShuttingDown, Errored, Exiting}

    private static final Daemon instance = new Daemon();

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
    private static DesktopTray tray;
    private static int uiPort;
    private boolean peerDiscoveryStarted = false;
    private ServiceStatus networkServiceStatus = ServiceStatus.SHUTDOWN;

    public static File rootDir;
    public static File userAppDataDir;
    public static File userAppConfigDir;
    public static File userAppCacheDir;

    private static Thread routerThread;

    public static void main(String[] args) {
        LOG.info("1M5 initializing...\n\tThread name: " + Thread.currentThread().getName());
        // Start GUI
        new Thread(new Runnable() {
            @Override
            public void run() {
                Application.launch(DesktopApp.class);
            }
        }).start();
        Thread.currentThread().setName("1M5-Router-Thread");
        // Start bus in separate thread
        routerThread = Thread.currentThread();
        try {
            LOG.info("1M5 Router initializing...\n\tThread name: " + Thread.currentThread().getName());
            init(args);
        } catch (Exception e) {
            System.out.print(e.getLocalizedMessage());
            System.exit(-1);
        }
    }

    public static void init(String[] args) throws Exception {
        LOG.info("Welcome to 1M5. Initializing...");
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
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        }

        LOG.info("1M5 Version: "+config.getProperty("1m5.version")+"."+config.getProperty("1m5.version.build"));

        // Launch Tray
        tray = new DesktopTray();
        tray.start(instance);
        DesktopApp.setDappTray(tray);
        status = Status.Initialized;
        instance.start();
    }

    public void start() {
        try {
            status = Status.Starting;
            tray.updateStatus(DesktopTray.STARTING);
            // launch router
            instance.launch();
            running = true;
            status = Status.Running;
            // Check periodically to see if 1M5 stopped
            while (instance.clientAppManagerStatus != ClientAppManager.Status.STOPPED && running) {
                instance.waitABit(2 * 1000);
            }
            if(oneMFiveAppContext.getServiceBus().gracefulShutdown()) {
                status = Status.Shutdown;
                tray.updateStatus(DesktopTray.STOPPED);
                LOG.info("1M5 Dapp Stopped.");
            } else {
                status = Status.Errored;
                tray.updateStatus(DesktopTray.ERRORED);
                LOG.severe("1M5 Dapp Errored on Shutdown.");
            }
            OneMFiveAppContext.clearGlobalContext(); // Make sure we don't use the old context when restarting
        } catch (Exception e) {
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        }
    }

    public void shutdown() {
        status = Status.ShuttingDown;
        LOG.info("1M5 Dapp Shutting Down...");
        running = false;
    }

    public void exit() {
        LOG.info("1M5 Dapp Exiting...");
        status = Status.Exiting;
        running = false;
        waiting = false;
        System.exit(0);
    }

    private void launch() throws Exception {
        oneMFiveAppContext = OneMFiveAppContext.getInstance(config);
        // Getting ClientAppManager starts 1M5 Bus
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
                        LOG.info("Router starting...");
                        tray.updateStatus(DesktopTray.STARTING);
                        break;
                    }
                    case READY: {
                        LOG.info("Router connected.");
                        tray.updateStatus(DesktopTray.CONNECTED);
                        break;
                    }
                    case STOPPING: {
                        LOG.info("Router stopping...");
                        tray.updateStatus(DesktopTray.SHUTTINGDOWN);
                        break;
                    }
                    case STOPPED: {
                        LOG.info("Router stopped.");
                        tray.updateStatus(DesktopTray.STOPPED);
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
                    tray.updateStatus(DesktopTray.CONNECTED);
                } else if(serviceStatus == ServiceStatus.PARTIALLY_RUNNING) {
                    LOG.info("1M5 Network Service reporting Partially Running. Updating status to Reconnecting...");
                    tray.updateStatus(DesktopTray.RECONNECTING);
                } else if(serviceStatus == ServiceStatus.DEGRADED_RUNNING) {
                    LOG.info("1M5 Network Service reporting Degraded Running. Updating status to Reconnecting...");
                    tray.updateStatus(DesktopTray.DEGRADED);
                } else if(serviceStatus == ServiceStatus.BLOCKED) {
                    LOG.info("1M5 Network Service reporting Degraded Running. Updating status to Blocked.");
                    tray.updateStatus(DesktopTray.BLOCKED);
                }
            }
        };
        networkServiceStatusObservers.add(networkServiceStatusObserver);

        DLC.addData(ServiceStatusObserver.class, serviceStatusObservers, e);

        // Register Services
        DLC.addRoute(AdminService.class, AdminService.OPERATION_REGISTER_SERVICES,e);
        client.request(e);
    }

    public static void sendRequest(Envelope e) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(client==null) {
                    waitABit(1000);
                }
                client.request(e);
            }
        },"1M5-Send-To-Bus-Thread");
        t.setDaemon(true);
        t.start();
    }

    public static void sendRequest(Envelope e, ServiceCallback cb) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(client==null) {
                    waitABit(1000);
                }
                client.request(e, cb);
            }
        },"1M5-Send-To-Bus-With-Callback-Thread");
        t.setDaemon(true);
        t.start();
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
                    LOG.warning(e.getLocalizedMessage());
                }
            }
        }
        return false;
    }
}