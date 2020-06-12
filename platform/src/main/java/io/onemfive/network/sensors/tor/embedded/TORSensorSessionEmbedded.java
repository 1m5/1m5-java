package io.onemfive.network.sensors.tor.embedded;

import io.onemfive.network.NetworkPacket;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.sensors.clearnet.ClearnetSession;
import io.onemfive.network.sensors.tor.TORSensor;

import java.util.logging.Logger;


public class TORSensorSessionEmbedded extends ClearnetSession {

    private static final Logger LOG = Logger.getLogger(TORSensorSessionEmbedded.class.getName());

    private TORSensor torSensor;

    public TORSensorSessionEmbedded(TORSensor torSensor) {
        super(torSensor);
        this.torSensor = torSensor;
    }

    @Override
    public Boolean send(NetworkPacket packet) {
        LOG.warning("Not yet implemented.");
        return null;
    }

    @Override
    public boolean open(String address) {
        LOG.warning("Not yet implemented.");
        return false;
    }

    @Override
    public boolean connect() {
        LOG.warning("Not yet implemented.");
        return false;
    }

    @Override
    public boolean disconnect() {
        LOG.warning("Not yet implemented.");
        return false;
    }

    @Override
    public boolean isConnected() {
        LOG.warning("Not yet implemented.");
        return false;
    }

    @Override
    public boolean close() {
        LOG.warning("Not yet implemented.");
        return false;
    }
}
