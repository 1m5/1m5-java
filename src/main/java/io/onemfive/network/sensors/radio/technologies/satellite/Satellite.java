package io.onemfive.network.sensors.radio.technologies.satellite;

import io.onemfive.network.sensors.radio.BaseRadio;
import io.onemfive.network.sensors.radio.RadioPeer;
import io.onemfive.network.sensors.radio.RadioSession;

public class Satellite extends BaseRadio {

    @Override
    public RadioSession establishSession(RadioPeer peer, Boolean autoConnect) {
        return null;
    }
}
