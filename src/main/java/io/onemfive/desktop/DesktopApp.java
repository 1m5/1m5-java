package io.onemfive.desktop;

import dorkbox.systemTray.SystemTray;
import io.onemfive.DAppTray;
import io.onemfive.core.client.Client;
import io.onemfive.core.util.AppThread;
import io.onemfive.core.util.UncaughtExceptionHandler;
import io.onemfive.desktop.util.CssTheme;
import io.onemfive.desktop.util.ImageUtil;
import io.onemfive.desktop.views.ViewLoader;
import io.onemfive.desktop.views.home.HomeView;
import javafx.application.Application;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.util.logging.Logger;

import static io.onemfive.desktop.util.Layout.*;

public class DesktopApp extends Application implements UncaughtExceptionHandler {

    private static final Logger LOG = Logger.getLogger(DesktopApp.class.getName());

    private static Client client;
    private static DAppTray dAppTray;
    private static SystemTray systemTray;
    private static AppThread routerThread;

    private static Runnable shutDownHandler;

    private Stage stage;
    private boolean popupOpened;
    private Scene scene;
    private boolean shutDownRequested;

    public DesktopApp() {
        shutDownHandler = this::stop;
    }

    public static void setClient(Client c) {
        client = c;
    }

    public static void setDappTray(DAppTray tray) {
        dAppTray = tray;
        systemTray = dAppTray.systemTray;
    }

    public static void setRouterThread(AppThread thread) {
        routerThread = thread;
    }

    @Override
    public void init() throws Exception {
        LOG.info("DesktopApp initializing...\n\tThread name: " + Thread.currentThread().getName());
    }

    @Override
    public void start(Stage stage) {
        LOG.info("DesktopApp starting...\n\tThread name: " + Thread.currentThread().getName());
        this.stage = stage;
        stage.setTitle("1M5");
        HomeView homeView = (HomeView) ViewLoader.load(HomeView.class);
        Rectangle maxWindowBounds = new Rectangle();
        try {
            maxWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        } catch (IllegalArgumentException e) {
            // Multi-screen environments may encounter IllegalArgumentException (Window must not be zero)
            // Just ignore the exception and continue, which means the window will use the minimum window size below
            // since we are unable to determine if we can use a larger size
        }
        scene = new Scene(homeView.getRoot(),
                maxWindowBounds.width < INITIAL_WINDOW_WIDTH ?
                        Math.max(maxWindowBounds.width, MIN_WINDOW_WIDTH) :
                        INITIAL_WINDOW_WIDTH,
                maxWindowBounds.height < INITIAL_WINDOW_HEIGHT ?
                        Math.max(maxWindowBounds.height, MIN_WINDOW_HEIGHT) :
                        INITIAL_WINDOW_HEIGHT);

        CssTheme.loadSceneStyles(scene, 1);

        // configure the system tray
//        SystemTray.create(stage, shutDownHandler);

        stage.setOnCloseRequest(Event::consume);

        // configure the primary stage
        stage.setTitle("1M5");
        stage.setScene(scene);
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.getIcons().add(ImageUtil.getApplicationIconImage());

        // make the UI visible
        stage.show();
    }

    @Override
    public void stop() {
        if (!shutDownRequested) {
//            new Popup().headLine(Res.get("popup.shutDownInProgress.headline"))
//                    .backgroundInfo(Res.get("popup.shutDownInProgress.msg"))
//                    .hideCloseButton()
//                    .useAnimation(false)
//                    .show();
//            UserThread.runAfter(() -> {
//                gracefulShutDownHandler.gracefulShutDown(() -> {
//                    log.debug("App shutdown complete");
//                });
//            }, 200, TimeUnit.MILLISECONDS);
            shutDownRequested = true;
        }
    }

    @Override
    public void handleUncaughtException(Throwable throwable, boolean doShutDown) {
        if (!shutDownRequested) {
            if (scene == null) {
                LOG.warning("Scene not available yet, we create a new scene. The bug might be caused by an exception in a constructor or by a circular dependency in Guice. throwable=" + throwable.toString());
                scene = new Scene(new StackPane(), 1000, 650);
                CssTheme.loadSceneStyles(scene, CssTheme.CSS_THEME_LIGHT);
                stage.setScene(scene);
                stage.show();
            }
            try {
                if (doShutDown)
                    stop();
            } catch (Throwable throwable2) {
                // If printStackTrace cause a further exception we don't pass the throwable to the Popup.
                LOG.severe(throwable2.getLocalizedMessage());
                stop();
            }
        }
    }

//    public static void main(String[] args) {
//        launch(DesktopApp.class);
//    }

}
