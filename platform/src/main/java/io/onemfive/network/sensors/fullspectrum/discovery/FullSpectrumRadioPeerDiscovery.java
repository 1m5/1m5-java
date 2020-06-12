package io.onemfive.network.sensors.fullspectrum.discovery;

import io.onemfive.network.sensors.Sensor;
import io.onemfive.network.NetworkTask;
import io.onemfive.util.tasks.TaskRunner;

import java.util.logging.Logger;

public class FullSpectrumRadioPeerDiscovery extends NetworkTask {

    private Logger LOG = Logger.getLogger(FullSpectrumRadioPeerDiscovery.class.getName());

    public FullSpectrumRadioPeerDiscovery(TaskRunner taskRunner, Sensor sensor) {
        super(FullSpectrumRadioPeerDiscovery.class.getName(), taskRunner, sensor);
    }

    @Override
    public Boolean execute() {
        return null;
    }
}
