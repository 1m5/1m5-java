package io.onemfive.data;

public final class ManConStatus {
    // Max Supported ManCon is the highest supported ManCon by Sensor Manager determination by looking at what Sensors were registered and are active.
    // Current default setting of HIGH is based on registering Tor, I2P, and Bluetooth by default.
    public static ManCon MAX_SUPPORTED_MANCON = ManCon.EXTREME;
    // Max Available ManCon is the current level of ManCon that can be supported determined by Sensor Manager
    // changing in real-time based on network connectivity and peer discovery.
    // Initial setting is NONE until sensors come on line, connect, and discover peers. This is managed by the Sensor Manager.
    public static ManCon MAX_AVAILABLE_MANCON = ManCon.NONE;
    // Min Required ManCon is set by end users or system admins for daemons to indicate the minimum ManCon to use for current communications.
    // TODO: Load this from a configuration
    public static ManCon MIN_REQUIRED_MANCON = ManCon.HIGH;

}
