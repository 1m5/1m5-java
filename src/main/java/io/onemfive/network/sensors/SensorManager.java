package io.onemfive.network.sensors;

import io.onemfive.data.Envelope;
import io.onemfive.data.Packet;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.network.peers.PeerReport;

import java.io.File;
import java.util.Properties;

public interface SensorManager {

    String HTTP_SENSOR_NAME = "io.onemfive.network.sensors.clearnet.client.ClearnetClientSensor";
    String TOR_SENSOR_NAME = "io.onemfive.network.sensors.tor.client.TorClientSensor";
    String I2P_SENSOR_NAME = "io.onemfive.network.sensors.i2p.I2PSensor";
    String RADIO_SENSOR_NAME = "io.onemfive.network.sensors.radio.RadioSensor";
    String LIFI_SENSOR_NAME ="io.onemfive.network.sensors.lifi.LiFiSensor";

    boolean init(Properties properties);
    boolean isActive(String sensorName);
    Sensor selectSensor(Packet packet);
    void registerSensor(Sensor sensor);
    void setPeerManager(PeerManager peerManager);
    PeerReport getPeerReport();
    void updateSensorStatus(final String sensorID, SensorStatus sensorStatus);
    Sensor getRegisteredSensor(String sensorName);
    boolean handleNetworkOpPacket(Packet packet, NetworkOp op);
    boolean sendToBus(Envelope envelope);
    boolean shutdown();
    void suspend(Envelope envelope);
    File getSensorDirectory(String sensorName);
    boolean registerSensorStatusListener(String sensorId, SensorStatusListener listener);
    boolean unregisterSensorStatusListener(String sensorId, SensorStatusListener listener);
}
