/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.network.sensors;

import io.onemfive.util.tasks.TaskRunner;
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
