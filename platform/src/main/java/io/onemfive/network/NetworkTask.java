package io.onemfive.network;

import io.onemfive.data.NetworkNode;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.network.sensors.Sensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.util.tasks.BaseTask;
import io.onemfive.util.tasks.TaskRunner;

/**
 *
 *
 * @author objectorange
 */
public abstract class NetworkTask extends BaseTask {

    protected Sensor sensor;
    protected SensorManager sensorManager;
    protected PeerManager peerManager;
    protected NetworkNode localNode;

    public NetworkTask(String taskName, TaskRunner taskRunner, SensorManager sensorManager) {
        super(taskName, taskRunner);
        this.sensorManager = sensorManager;
        peerManager = sensorManager.getPeerManager();
        localNode = peerManager.getLocalNode();
    }

    public NetworkTask(String taskName, TaskRunner taskRunner, Sensor sensor) {
        super(taskName, taskRunner);
        this.sensor = sensor;
        sensorManager = sensor.getSensorManager();
        peerManager = sensorManager.getPeerManager();
        localNode = peerManager.getLocalNode();
    }
}
