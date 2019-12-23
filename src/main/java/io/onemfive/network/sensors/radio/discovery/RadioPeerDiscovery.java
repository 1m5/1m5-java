package io.onemfive.network.sensors.radio.discovery;

import io.onemfive.network.sensors.radio.RadioPeer;
import io.onemfive.network.sensors.radio.RadioSensor;
import io.onemfive.network.sensors.radio.tasks.RadioTask;
import io.onemfive.network.sensors.radio.tasks.TaskRunner;

import java.util.Properties;
import java.util.logging.Logger;

public class RadioPeerDiscovery extends RadioTask {

    private Logger LOG = Logger.getLogger(RadioPeerDiscovery.class.getName());

    public RadioPeerDiscovery(RadioSensor sensor, TaskRunner taskRunner, Properties properties, long periodicity) {
        super(sensor, taskRunner, properties, periodicity);
    }

    @Override
    public boolean runTask() {
        LOG.info("Starting Radio Peer Discovery...");
        RadioPeer localNode = sensor.getLocalNode();
        if(localNode==null) {
            LOG.info("Local RadioPeer not established yet. Can't run Peer Discovery.");
            return false;
        }
        LOG.info("Completed Radio Peer Discovery.");
        return false;
    }
}
