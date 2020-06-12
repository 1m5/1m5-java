package io.onemfive.network.sensors.i2p;

import io.onemfive.core.LifeCycle;
import io.onemfive.network.NetworkPacket;

public interface I2P extends LifeCycle {
    boolean sendOut(NetworkPacket packet);
}
