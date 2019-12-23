package io.onemfive.network.sensors.radio;

public interface Signal {
    String getId();
    String getName();
    Integer getPort();
    String getGoverningBody();
    String getDescription();
    Integer getScore();
    Boolean getActive();
    Long getFloorFrequencyHz();
    Long getCeilingFrequencyHz();
    Long getCurrentFrequencyHz();
}
