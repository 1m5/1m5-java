package io.onemfive;


import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.logging.Logger;

public class DAppTray {

    private Logger LOG = Logger.getLogger(DAppTray.class.getName());

    public static final String INITIALIZING = "Initializing...";
    public static final String STARTING = "Starting...";
    public static final String CONNECTING = "Connecting...";
    public static final String CONNECTED = "Connected";
    public static final String DEGRADED = "Degraded";
    public static final String BLOCKED = "Blocked";
    public static final String RECONNECTING = "Reconnecting...";
    public static final String SHUTTINGDOWN = "Shutting Down...";
    public static final String STOPPED = "Stopped";
    public static final String QUITTING = "Quitting";
    public static final String ERRORED = "Error";

    private String status = INITIALIZING;

    private SystemTray systemTray;

    private MenuItem launchMenuItem;
    private MenuItem quitMenuItem;

    private URL sysTrayWhite;
    private URL sysTrayBlue;
    private URL sysTrayGreen;
    private URL sysTrayOrange;
    private URL sysTrayYellow;
    private URL sysTrayRed;
    private URL sysTrayGray;

    public void start(Dapp dApp) {
        SystemTray.SWING_UI = new DAppUI();
        updateStatus(INITIALIZING);
        systemTray = SystemTray.get();
        if (systemTray == null) {
            throw new RuntimeException("Unable to load SystemTray!");
        }
        sysTrayWhite = this.getClass().getClassLoader().getResource("images/sys_tray_icon_white.png");
        sysTrayBlue = this.getClass().getClassLoader().getResource("images/sys_tray_icon_blue.png");
        sysTrayGreen = this.getClass().getClassLoader().getResource("images/sys_tray_icon_green.png");
        sysTrayOrange = this.getClass().getClassLoader().getResource("images/sys_tray_icon_orange.png");
        sysTrayYellow = this.getClass().getClassLoader().getResource("images/sys_tray_icon_yellow.png");
        sysTrayRed = this.getClass().getClassLoader().getResource("images/sys_tray_icon_red.png");
        sysTrayGray = this.getClass().getClassLoader().getResource("images/sys_tray_icon_gray.png");

        // Setup Menus
        // Launch
        launchMenuItem = new MenuItem("Launch", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new Thread() {
                    @Override
                    public void run() {
                        dApp.launchUI();
                    }
                }.start();
            }
        });
        launchMenuItem.setEnabled(false);
        systemTray.getMenu().add(launchMenuItem).setShortcut('s');

        // Quit
        quitMenuItem = new MenuItem("Quit", new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        systemTray.setImage(sysTrayYellow);
                        updateStatus("Quitting");
                        dApp.shutdown();
                        systemTray.shutdown();
                        dApp.exit();
                    }
                }.start();
            }
        });
        systemTray.getMenu().add(quitMenuItem).setShortcut('q'); // case does not matter
    }

    public void updateStatus(String status) {
        switch(status) {
            case INITIALIZING: {
                systemTray.setImage(sysTrayGray);
                break;
            }
            case STARTING: {
                systemTray.setImage(sysTrayYellow);
                break;
            }
            case CONNECTING: {
                systemTray.setImage(sysTrayOrange);
                break;
            }
            case CONNECTED: {
                launchMenuItem.setEnabled(true);
                systemTray.setImage(sysTrayGreen);
                break;
            }
            case RECONNECTING: {
                systemTray.setImage(sysTrayOrange);
                break;
            }
            case DEGRADED: {
                systemTray.setImage(sysTrayYellow);
                break;
            }
            case BLOCKED: {
                systemTray.setImage(sysTrayBlue);
                break;
            }
            case ERRORED: {
                systemTray.setImage(sysTrayRed);
                break;
            }
            case SHUTTINGDOWN: {
                systemTray.setImage(sysTrayYellow);
                launchMenuItem.setEnabled(false);
                break;
            }
            case STOPPED: {
                systemTray.setImage(sysTrayGray);
                break;
            }
            case QUITTING: {
                launchMenuItem.setEnabled(false);
                quitMenuItem.setEnabled(false);
                systemTray.setImage(sysTrayWhite);
                break;
            }
            default: {
                LOG.warning("Status unknown: "+status);
                return;
            }
        }
        systemTray.setStatus(status);
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
