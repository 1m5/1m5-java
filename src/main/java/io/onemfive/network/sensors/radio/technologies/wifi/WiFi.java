package io.onemfive.network.sensors.radio.technologies.wifi;

import io.onemfive.network.sensors.radio.BaseRadio;
import io.onemfive.network.sensors.radio.RadioPeer;
import io.onemfive.network.sensors.radio.RadioSession;

public class WiFi extends BaseRadio {

    @Override
    public RadioSession establishSession(RadioPeer peer, Boolean autoConnect) {
        return null;
    }
}
