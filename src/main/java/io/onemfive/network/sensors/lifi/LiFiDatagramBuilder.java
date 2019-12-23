package io.onemfive.network.sensors.lifi;

public class LiFiDatagramBuilder {

    public static final int DATAGRAM_MAX_SIZE = 32768;
    private LiFiSession session;

    public LiFiDatagramBuilder(LiFiSession session) {
        this.session = session;
    }

    public LiFiDatagram makeLiFIDatagram(byte[] payload) {
        return null;
    }
}
