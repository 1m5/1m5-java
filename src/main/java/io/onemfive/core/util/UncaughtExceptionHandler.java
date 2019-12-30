package io.onemfive.core.util;

public interface UncaughtExceptionHandler {
    void handleUncaughtException(Throwable throwable, boolean doShutDown);
}
