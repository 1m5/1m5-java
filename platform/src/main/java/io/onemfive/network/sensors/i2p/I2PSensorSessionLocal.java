package io.onemfive.network.sensors.i2p;

import io.onemfive.network.NetworkPacket;
import io.onemfive.network.ops.NetworkNotifyOp;
import io.onemfive.network.ops.NetworkRequestOp;
import io.onemfive.network.sensors.BaseSession;

public class I2PSensorSessionLocal extends BaseSession {

    public I2PSensorSessionLocal(I2PSensor sensor) {
        super(sensor);
    }

    @Override
    public boolean open(String address) {
        return false;
    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public Boolean send(NetworkPacket packet) {
        return null;
    }

    @Override
    public boolean send(NetworkRequestOp requestOp) {
        return false;
    }

    @Override
    public boolean notify(NetworkNotifyOp notifyOp) {
        return false;
    }
}
