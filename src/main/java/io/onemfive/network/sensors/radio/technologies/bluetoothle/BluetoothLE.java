package io.onemfive.network.sensors.radio.technologies.bluetoothle;

import io.onemfive.network.sensors.radio.BaseRadio;
import io.onemfive.network.sensors.radio.RadioPeer;
import io.onemfive.network.sensors.radio.RadioSession;

public class BluetoothLE extends BaseRadio {

    @Override
    public RadioSession establishSession(RadioPeer peer, Boolean autoConnect) {
        return null;
    }
}
