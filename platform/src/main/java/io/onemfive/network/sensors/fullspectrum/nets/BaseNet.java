package io.onemfive.network.sensors.fullspectrum.nets;

import io.onemfive.network.sensors.fullspectrum.SignalSession;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseNet implements Net {

    private List<SignalSession> supportedSignals = new ArrayList<>();

    @Override
    public List<SignalSession> supportedSignals() {
        return supportedSignals;
    }
}
