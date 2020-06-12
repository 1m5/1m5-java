package io.onemfive.network.sensors.lifi;

import io.onemfive.network.NetworkTask;
import io.onemfive.util.tasks.TaskRunner;

public class LiFiPeerDiscovery extends NetworkTask {

    public LiFiPeerDiscovery(TaskRunner taskRunner, LiFiSensor sensor) {
        super(LiFiPeerDiscovery.class.getName(), taskRunner, sensor);
    }

    @Override
    public Boolean execute() {
        return null;
    }
}
