package io.onemfive.network.sensors.radio;

import io.onemfive.core.LifeCycle;
import io.onemfive.network.peers.PeerReport;

/**
 * Interface to use for all Radio calls.
 */
public interface Radio extends LifeCycle {
    void setPeerReport(PeerReport peerReport);
    RadioSession establishSession(RadioPeer peer, Boolean autoConnect);
    RadioSession getSession(Integer sessionId);
    Boolean closeSession(Integer sessionId);
    Boolean disconnected();
}
