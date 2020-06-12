package io.onemfive.network.sensors.fullspectrum;

import io.onemfive.network.sensors.SensorSession;

public interface SignalSession extends SensorSession {
    String getName();
    String getFullName();
    Integer getPort();
    String getGoverningBody();
    String getDescription();
    Integer getScore();
    Boolean getActive();
    Long getFloorFrequencyHz();
    Long getCeilingFrequencyHz();
    Long getCurrentFrequencyHz();
}
