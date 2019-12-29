package io.onemfive.network.ops;

import io.onemfive.core.Notification;
import io.onemfive.data.Packet;
import io.onemfive.network.sensors.SensorManager;

import java.util.logging.Logger;

/**
 * Notify Network Peer of change in reachability
 */
public class NotifyReachabilityOp extends NetworkOpBase implements Notification {

    private static final Logger LOG = Logger.getLogger(NotifyReachabilityOp.class.getName());

    public NotifyReachabilityOp(SensorManager sensorManager) {
        super(sensorManager);
    }

    @Override
    public void notify(Packet packet) {

    }
}
