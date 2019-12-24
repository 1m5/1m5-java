package io.onemfive.network.ops;

import io.onemfive.network.SensorManager;

public abstract class NetworkOpBase implements NetworkOp {

    protected SensorManager sensorManager;

    public NetworkOpBase (SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }
}
