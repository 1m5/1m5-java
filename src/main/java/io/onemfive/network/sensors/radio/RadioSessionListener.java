package io.onemfive.network.sensors.radio;

public interface RadioSessionListener {

    void messageAvailable(RadioSession session, Integer port);
    void connected(RadioSession session);
    void disconnected(RadioSession session);

    void errorOccurred(RadioSession session, String message, Throwable throwable);
}
