package io.onemfive.network.sensors.fullspectrum.nets;

import io.onemfive.network.sensors.fullspectrum.SignalSession;

import java.util.List;

public interface Net {
    List<SignalSession> supportedSignals();
}
