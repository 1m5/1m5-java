package io.onemfive.network.sensors.lifi;

public class LiFiDatagramExtractor {

    private byte[] payload;
    private Destination sender;

    void extractLiFiDatagram(byte[] datagram) {
        // Extract payload
        this.payload = null;
        // Extract sender
        this.sender = null;
    }

    byte[] getPayload() {
        return payload;
    }

    Destination getSender() {
        return sender;
    }
}
