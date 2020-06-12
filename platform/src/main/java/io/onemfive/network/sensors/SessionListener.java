package io.onemfive.network.sensors;

import io.onemfive.network.sensors.SensorSession;

public interface SessionListener {

    void messageAvailable(SensorSession session, Integer port);
    void connected(SensorSession session);
    void disconnected(SensorSession session);

    void errorOccurred(SensorSession session, String message, Throwable throwable);
}
