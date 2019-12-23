package io.onemfive.network.sensors.lifi;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class LiFiSession {

    private static final Logger LOG = Logger.getLogger(LiFiSession.class.getName());

    private Destination localDestination;
    private List<LiFiSessionListener> sessionListeners = new ArrayList<>();

    public Destination getLocalDestination() {
        return localDestination;
    }

    public Destination lookupDestination(String address) {
        Destination dest = null;

        return dest;
    }

    public boolean sendMessage(Destination toDestination, LiFiDatagram datagram, Properties options) {
        LOG.warning("LiFISession.sendMessage() not implemented.");
        return false;
    }

    public byte[] receiveMessage(int msgId) {
        return null;
    }

    public boolean connect() {
        return false;
    }

    public void addSessionListener(LiFiSessionListener listener) {
        sessionListeners.add(listener);
    }

    public void removeSessionListener(LiFiSessionListener listener) {
        sessionListeners.remove(listener);
    }
}
