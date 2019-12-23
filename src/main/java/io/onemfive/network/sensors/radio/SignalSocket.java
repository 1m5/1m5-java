package io.onemfive.network.sensors.radio;

public class SignalSocket {

    private Radio radio;
    private Signal signal;

    public SignalSocket(Radio radio, Signal signal) {
        this.radio = radio;
        this.signal = signal;
    }

    public boolean connect() {

        return false;
    }

    public void close() {

    }
}
