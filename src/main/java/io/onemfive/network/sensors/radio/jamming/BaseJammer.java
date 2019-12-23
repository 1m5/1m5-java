package io.onemfive.network.sensors.radio.jamming;

public abstract class BaseJammer implements Jammer {

    protected JammerStatus status;

    public JammerStatus getStatus() {
        return status;
    }
}
