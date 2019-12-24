package io.onemfive.network.sensors.radio;

import io.onemfive.data.Request;

/**
 * Define the means of sending and receiving messages using the radio electromagnetic spectrum
 * over a bidirectional Socket.
 */
public interface RadioSession {

    enum Status {CONNECTING, CONNECTED, DISCONNECTED, STOPPING, STOPPED, ERRORED}

    Integer getId();
    Radio getRadio();
    boolean connect(RadioPeer peer);
    boolean disconnect();
    boolean isConnected();
    boolean close();
    RadioDatagram toRadioDatagram(Request request);
    Boolean sendDatagram(RadioDatagram datagram);
    RadioDatagram receiveDatagram(Integer port);
    void addSessionListener(RadioSessionListener listener);
    void removeSessionListener(RadioSessionListener listener);
    Status getStatus();
}
