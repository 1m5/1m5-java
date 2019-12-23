package io.onemfive.network.sensors.radio.jamming;

/**
 * Jam a particular Net contact.
 */
public interface Jammer {
    JammerStatus getStatus();
}
