package io.onemfive.network;

import io.onemfive.core.util.tasks.TaskRunner;
import io.onemfive.data.Envelope;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.data.Sensitivity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A Base for common data and operations across all Sensors to provide a basic framework for them.
 *
 * @author objectorange
 */
public abstract class BaseSensor implements Sensor {

    protected Network network;
    protected SensorManager sensorManager;
    private SensorStatus sensorStatus = SensorStatus.NOT_INITIALIZED;
    protected Integer restartAttempts = 0;
    private Sensitivity sensitivity;
    private Integer priority;
    protected Map<String,NetworkPeer> peers = new HashMap<>();
    protected TaskRunner taskRunner;
    protected String directory;

    protected void updateStatus(SensorStatus sensorStatus) {
        this.sensorStatus = sensorStatus;
        // Might be null during localized testing
        if(sensorManager != null) {
            sensorManager.updateSensorStatus(this.getClass().getName(), sensorStatus);
        }
    }

    public BaseSensor() {}

    public BaseSensor(SensorManager sensorManager, Sensitivity sensitivity, Integer priority) {
        this.sensorManager = sensorManager;
        this.sensitivity = sensitivity;
        this.priority = priority;
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return sensorManager.sendToBus(envelope);
    }

    @Override
    public boolean replyIn(Envelope envelope) {
        return sensorManager.sendToBus(envelope);
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    @Override
    public void setSensitivity(Sensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }

    @Override
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public SensorStatus getStatus() {
        return sensorStatus;
    }

    @Override
    public Sensitivity getSensitivity() {
        return sensitivity;
    }

    @Override
    public Integer getPriority() {
        return priority;
    }

    @Override
    public Integer getRestartAttempts() {
        return restartAttempts;
    }

    @Override
    public File getDirectory() {
        return sensorManager.getSensorDirectory(this.getClass().getName());
    }
}
