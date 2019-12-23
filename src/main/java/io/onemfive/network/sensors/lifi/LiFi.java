package io.onemfive.network.sensors.lifi;

/**
 * Interface to use for all LiFi calls.
 */
public interface LiFi {

    int sendMessage(LiFiDatagram message);
}
