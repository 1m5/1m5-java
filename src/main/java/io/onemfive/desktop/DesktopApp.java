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
package io.onemfive.desktop;

import dorkbox.systemTray.SystemTray;
import io.onemfive.core.client.Client;
import io.onemfive.util.AppThread;
import io.onemfive.util.UncaughtExceptionHandler;
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

    private static DesktopTray desktopTray;
    private static SystemTray systemTray;

    private static Runnable shutDownHandler;

    public static double width;
    public static double height;

    private static Stage stage;
    private boolean popupOpened;
    private Scene scene;
    private boolean shutDownRequested;

    public DesktopApp() {
        shutDownHandler = this::stop;
    }

    public static void setDappTray(DesktopTray tray) {
        desktopTray = tray;
        systemTray = desktopTray.systemTray;
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
        HomeView homeView = (HomeView) ViewLoader.load(HomeView.class, true);
        Rectangle maxWindowBounds = new Rectangle();
        try {
            maxWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        } catch (IllegalArgumentException e) {
            // Multi-screen environments may encounter IllegalArgumentException (Window must not be zero)
            // Just ignore the exception and continue, which means the window will use the minimum window size below
            // since we are unable to determine if we can use a larger size
        }
        width = maxWindowBounds.width < INITIAL_WINDOW_WIDTH ?
                Math.max(maxWindowBounds.width, MIN_WINDOW_WIDTH) :
                INITIAL_WINDOW_WIDTH;
        height = maxWindowBounds.height < INITIAL_WINDOW_HEIGHT ?
                Math.max(maxWindowBounds.height, MIN_WINDOW_HEIGHT) :
                INITIAL_WINDOW_HEIGHT;
        scene = new Scene((StackPane)homeView.getRoot(), width, height);

        CssTheme.loadSceneStyles(scene, 0);

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
//        stage.show();
//        show();
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

    public static void show() {
        stage.show();
    }

}
