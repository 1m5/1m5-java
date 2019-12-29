package io.onemfive.desktop;

import dorkbox.systemTray.*;
import dorkbox.util.CacheUtil;
import dorkbox.util.Desktop;
import dorkbox.util.JavaFX;
import dorkbox.util.OS;
import io.onemfive.DAppTray;
import io.onemfive.Resources;
import io.onemfive.core.client.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Random;

public class DesktopApp extends Application {

    private static Client client;
    private static DAppTray dAppTray;
    private static SystemTray systemTray;
    private static Runnable shutDownHandler;

    private ActionListener callbackGray;
    private Stage stage;
    private boolean popupOpened;
    private Scene scene;

   public static void init(Client c, DAppTray tray) {
       client = c;
       dAppTray = tray;
       systemTray = dAppTray.systemTray;
       shutDownHandler = new Runnable() {
           @Override
           public void run() {

           }
       };
   }

    @Override
    public void start(Stage stage) throws Exception {
       this.stage = stage;
       stage.setTitle("1M5");
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        stage.setScene(new Scene(root, 300, 250));
        stage.show();

        SystemTray.DEBUG = true; // for test apps, we always want to run in debug mode
        CacheUtil.clear(); // for test apps, make sure the cache is always reset. You should never do this in production.

//        SystemTray.APP_NAME = "1M5";

        // SwingUtil.setLookAndFeel(null); // set Native L&F (this is the System L&F instead of CrossPlatform L&F)
        // SystemTray.SWING_UI = new CustomSwingUI();

        systemTray = SystemTray.get();
        if (systemTray == null) {
            throw new RuntimeException("Unable to load SystemTray!");
        }

        systemTray.setTooltip("Mail Checker");
        systemTray.setImage(Resources.SYS_TRAY_ICON_GRAY);
        systemTray.setStatus("No Mail");

        callbackGray = new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                final MenuItem entry = (MenuItem) e.getSource();
                systemTray.setStatus(null);
                systemTray.setImage(Resources.SYS_TRAY_ICON_WHITE);

                entry.setCallback(null);
//                systemTray.setStatus("Mail Empty");
                systemTray.getMenu().remove(entry);
                System.err.println("POW");
            }
        };


        Menu mainMenu = systemTray.getMenu();

        MenuItem greenEntry = new MenuItem("Green Mail", new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                final MenuItem entry = (MenuItem) e.getSource();
                systemTray.setStatus("Some Mail!");
                systemTray.setImage(Resources.SYS_TRAY_ICON_GREEN);

                entry.setCallback(callbackGray);
                entry.setImage(Resources.SYS_TRAY_ICON_WHITE);
                entry.setText("Delete Mail");
                entry.setTooltip(null); // remove the tooltip
//                systemTray.remove(menuEntry);
            }
        });
        greenEntry.setImage(Resources.SYS_TRAY_ICON_GREEN);
        // case does not matter
        greenEntry.setShortcut('G');
        greenEntry.setTooltip("This means you have green mail!");
        mainMenu.add(greenEntry);


        Checkbox checkbox = new Checkbox("Euro € Mail", new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                System.err.println("Am i checked? " + ((Checkbox) e.getSource()).getChecked());
            }
        });
        checkbox.setShortcut('€');
        mainMenu.add(checkbox);

        MenuItem removeTest = new MenuItem("This should not be here", new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                try {
                    Desktop.browseURL("https://1m5.io");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        mainMenu.add(removeTest);
        mainMenu.remove(removeTest);

        mainMenu.add(new Separator());

        mainMenu.add(new MenuItem("About", new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                try {
                    Desktop.browseURL("https://git.dorkbox.com/dorkbox/SystemTray");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }));

        mainMenu.add(new MenuItem("Temp Directory", new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                try {
                    Desktop.browseDirectory(OS.TEMP_DIR.getAbsolutePath());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }));

        Menu submenu = new Menu("Options", Resources.SYS_TRAY_ICON_BLUE);
        submenu.setShortcut('t');


        MenuItem disableMenu = new MenuItem("Disable menu", Resources.SYS_TRAY_ICON_WHITE, new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                MenuItem source = (MenuItem) e.getSource();
                source.getParent().setEnabled(false);
            }
        });
        submenu.add(disableMenu);


        submenu.add(new MenuItem("Hide tray", Resources.SYS_TRAY_ICON_GRAY, new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                systemTray.setEnabled(false);
            }
        }));
        submenu.add(new MenuItem("Remove menu", Resources.SYS_TRAY_ICON_WHITE, new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                MenuItem source = (MenuItem) e.getSource();
                source.getParent().remove();
            }
        }));
        submenu.add(new MenuItem("Add new entry to tray", new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                systemTray.getMenu().add(new MenuItem("Random " + new Random().nextInt(10)));
            }
        }));
        mainMenu.add(submenu);

        MenuItem entry = new MenuItem("Type: ");
        entry.setEnabled(false);
        systemTray.getMenu().add(entry);

        systemTray.getMenu().add(new MenuItem("Quit", new ActionListener() {
            @Override
            public
            void actionPerformed(final java.awt.event.ActionEvent e) {
                systemTray.shutdown();

                if (!JavaFX.isEventThread()) {
                    JavaFX.dispatch(new Runnable() {
                        @Override
                        public
                        void run() {
                            stage.hide(); // must do this BEFORE Platform.exit() otherwise odd errors show up
                            Platform.exit();  // necessary to close javaFx
                        }
                    });
                } else {
                    stage.hide(); // must do this BEFORE Platform.exit() otherwise odd errors show up
                    Platform.exit();  // necessary to close javaFx
                }

                //System.exit(0);  not necessary if all non-daemon threads have stopped.
            }
        })).setShortcut('q'); // case does not matter
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

}
