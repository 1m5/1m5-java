package io.onemfive.network.sensors.radio.technologies.bluetooth;

import io.onemfive.network.sensors.radio.RadioPeer;

public class BluetoothPeer extends RadioPeer {

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
