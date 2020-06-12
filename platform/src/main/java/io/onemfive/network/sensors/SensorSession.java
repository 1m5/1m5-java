package io.onemfive.network.sensors;

import io.onemfive.network.NetworkPacket;
import io.onemfive.network.ops.NetworkNotifyOp;
import io.onemfive.network.ops.NetworkRequestOp;

import java.util.Properties;

/**
 * Define the means of sending and receiving messages using the radio electromagnetic spectrum
 * over a bidirectional Socket.
 */
public interface SensorSession {

    enum Status {CONNECTING, CONNECTED, DISCONNECTED, STOPPING, STOPPED, ERRORED}

    Integer getId();
    boolean init(Properties properties);
    String getAddress();
    boolean open(String address);
    boolean connect();
    boolean disconnect();
    boolean isConnected();
    boolean close();
    Boolean send(NetworkPacket packet);
    boolean send(NetworkRequestOp requestOp);
    boolean notify(NetworkNotifyOp notifyOp);
    void addSessionListener(SessionListener listener);
    void removeSessionListener(SessionListener listener);
    Status getStatus();
}
