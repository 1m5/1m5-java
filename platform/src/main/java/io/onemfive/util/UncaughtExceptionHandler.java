package io.onemfive.util;

public interface UncaughtExceptionHandler {
    void handleUncaughtException(Throwable throwable, boolean doShutDown);
}
