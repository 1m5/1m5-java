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

import io.onemfive.desktop.util.FrameRateTimer;
import io.onemfive.desktop.util.Timer;
import io.onemfive.desktop.views.BaseView;
import io.onemfive.desktop.views.View;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MVC {

    private static final Logger LOG = Logger.getLogger(MVC.class.getName());

    private static Class<? extends Timer> timerClass;
    private static Executor executor;

    static {
        executor = Executors.newFixedThreadPool(8);
        timerClass = FrameRateTimer.class;
    }

    public static final Navigation navigation = new Navigation();

    private static final HashMap<String, BaseView> viewCache = new HashMap<>();

    public static Executor getExecutor() {
        return executor;
    }

    public static void setExecutor(Executor executor) {
        MVC.executor = executor;
    }

    public static View loadView(Class<? extends View> viewClass) {
        // Caching on by default
        return loadView(viewClass, true);
    }

    public synchronized static View loadView(Class<? extends View> viewClass, boolean useCache) {
        BaseView view = null;
        if (viewCache.containsKey(viewClass.getName()) && useCache) {
            view = viewCache.get(viewClass.getName());
        } else {
            URL loc = viewClass.getResource(viewClass.getSimpleName()+".fxml");
            FXMLLoader loader = new FXMLLoader(loc);
            try {
                loader.load();
                view = loader.getController();
                if(useCache)
                    viewCache.put(viewClass.getName(), view);
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        return view;
    }

    public static void execute(Runnable command) {
        MVC.executor.execute(command);
    }

    // Prefer FxTimer if a delay is needed in a JavaFx class (gui module)
    public static Timer runAfterRandomDelay(Runnable runnable, long minDelayInSec, long maxDelayInSec) {
        return MVC.runAfterRandomDelay(runnable, minDelayInSec, maxDelayInSec, TimeUnit.SECONDS);
    }

    @SuppressWarnings("WeakerAccess")
    public static Timer runAfterRandomDelay(Runnable runnable, long minDelay, long maxDelay, TimeUnit timeUnit) {
        return MVC.runAfter(runnable, new Random().nextInt((int) (maxDelay - minDelay)) + minDelay, timeUnit);
    }

    public static Timer runAfter(Runnable runnable, long delayInSec) {
        return MVC.runAfter(runnable, delayInSec, TimeUnit.SECONDS);
    }

    public static Timer runAfter(Runnable runnable, long delay, TimeUnit timeUnit) {
        return getTimer().runLater(Duration.ofMillis(timeUnit.toMillis(delay)), runnable);
    }

    public static Timer runPeriodically(Runnable runnable, long intervalInSec) {
        return MVC.runPeriodically(runnable, intervalInSec, TimeUnit.SECONDS);
    }

    public static Timer runPeriodically(Runnable runnable, long interval, TimeUnit timeUnit) {
        return getTimer().runPeriodically(Duration.ofMillis(timeUnit.toMillis(interval)), runnable);
    }

    private static Timer getTimer() {
        try {
            return timerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            String message = "Could not instantiate timer bsTimerClass=" + timerClass;
            LOG.warning(message);
            throw new RuntimeException(message);
        }
    }

}
