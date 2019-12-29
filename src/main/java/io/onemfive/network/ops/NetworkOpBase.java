package io.onemfive.network.ops;

import io.onemfive.network.sensors.SensorManager;

public abstract class NetworkOpBase implements NetworkOp {

    protected SensorManager sensorManager;

    public NetworkOpBase() {

    }

    public NetworkOpBase (SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }
}
