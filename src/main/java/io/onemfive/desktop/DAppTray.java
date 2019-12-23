package io.onemfive.desktop;


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
    private URL icon;
    private URL iconBlue;
    private URL iconGreen;
    private URL iconOrange;
    private URL iconYellow;
    private URL iconRed;

    public void start(Dapp dApp) {
        SystemTray.SWING_UI = new DAppUI();

        systemTray = SystemTray.get();
        if (systemTray == null) {
            throw new RuntimeException("Unable to load SystemTray!");
        }
        icon = this.getClass().getClassLoader().getResource("Original-Crop.png");
        iconBlue = this.getClass().getClassLoader().getResource("Blue-Crop.png");
        iconGreen = this.getClass().getClassLoader().getResource("Green-Crop.png");
        iconOrange = this.getClass().getClassLoader().getResource("Orange-Crop.png");
        iconYellow = this.getClass().getClassLoader().getResource("Yellow-Crop.png");
        iconRed = this.getClass().getClassLoader().getResource("Red-Crop.png");

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
                        systemTray.setImage(iconYellow);
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
                systemTray.setImage(icon);
                break;
            }
            case STARTING: {
                systemTray.setImage(iconYellow);
                break;
            }
            case CONNECTING: {
                systemTray.setImage(iconOrange);
                break;
            }
            case CONNECTED: {
                launchMenuItem.setEnabled(true);
                systemTray.setImage(iconGreen);
                break;
            }
            case RECONNECTING: {
                systemTray.setImage(iconYellow);
                break;
            }
            case BLOCKED: {
                systemTray.setImage(iconBlue);
                break;
            }
            case ERRORED: {
                systemTray.setImage(iconRed);
                break;
            }
            case SHUTTINGDOWN: {
                systemTray.setImage(iconYellow);
                launchMenuItem.setEnabled(false);
                break;
            }
            case STOPPED: {
                systemTray.setImage(icon);
                break;
            }
            case QUITTING: {
                launchMenuItem.setEnabled(false);
                quitMenuItem.setEnabled(false);
                systemTray.setImage(icon);
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
