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

import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import io.onemfive.util.AppThread;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    public SystemTray systemTray;

    private MenuItem launchDesktopMenuItem;
    private MenuItem quitMenuItem;

    public void start(DApp dApp) {
        SystemTray.SWING_UI = new DAppUI();
        systemTray = SystemTray.get();
        if (systemTray == null) {
            throw new RuntimeException("Unable to load SystemTray!");
        }
        updateStatus(INITIALIZING);
        // Setup Menus
        // Launch
        launchDesktopMenuItem = new MenuItem("Desktop", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new AppThread(dApp::launchUI).start();
            }
        });
        launchDesktopMenuItem.setEnabled(true);
        systemTray.getMenu().add(launchDesktopMenuItem).setShortcut('s');

        // Quit
        quitMenuItem = new MenuItem("Quit", new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new AppThread() {
                    @Override
                    public void run() {
                        systemTray.setImage(Resources.SYS_TRAY_ICON_YELLOW);
                        updateStatus("Quitting");
                        dApp.shutdown();
                        systemTray.shutdown();
                        dApp.exit();
                    }
                }.start();
            }
        });
        quitMenuItem.setEnabled(true);
        systemTray.getMenu().add(quitMenuItem).setShortcut('q'); // case does not matter
        systemTray.setEnabled(true);
    }

    public void updateStatus(String status) {
        switch(status) {
            case INITIALIZING: {
                systemTray.setImage(Resources.SYS_TRAY_ICON_GRAY);
                break;
            }
            case STARTING: {
                systemTray.setImage(Resources.SYS_TRAY_ICON_YELLOW);
                break;
            }
            case CONNECTING: {
                systemTray.setImage(Resources.SYS_TRAY_ICON_ORANGE);
                break;
            }
            case CONNECTED: {
                launchDesktopMenuItem.setEnabled(true);
                systemTray.setImage(Resources.SYS_TRAY_ICON_GREEN);
                break;
            }
            case RECONNECTING: {
                systemTray.setImage(Resources.SYS_TRAY_ICON_ORANGE);
                break;
            }
            case DEGRADED: {
                systemTray.setImage(Resources.SYS_TRAY_ICON_YELLOW);
                break;
            }
            case BLOCKED: {
                systemTray.setImage(Resources.SYS_TRAY_ICON_BLUE);
                break;
            }
            case ERRORED: {
                systemTray.setImage(Resources.SYS_TRAY_ICON_RED);
                break;
            }
            case SHUTTINGDOWN: {
                systemTray.setImage(Resources.SYS_TRAY_ICON_YELLOW);
                launchDesktopMenuItem.setEnabled(false);
                break;
            }
            case STOPPED: {
                systemTray.setImage(Resources.SYS_TRAY_ICON_GRAY);
                break;
            }
            case QUITTING: {
                launchDesktopMenuItem.setEnabled(false);
                quitMenuItem.setEnabled(false);
                systemTray.setImage(Resources.SYS_TRAY_ICON_WHITE);
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
