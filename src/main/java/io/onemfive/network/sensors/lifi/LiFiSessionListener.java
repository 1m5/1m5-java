package io.onemfive.network.sensors.lifi;

public interface LiFiSessionListener {

    void messageAvailable(LiFiSession session, int var2, long var3);

    void disconnected(LiFiSession session);

    void errorOccurred(LiFiSession session, String message, Throwable throwable);

}
