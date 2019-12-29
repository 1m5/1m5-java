package io.onemfive.network.sensors;

import io.onemfive.data.Envelope;
import io.onemfive.data.Packet;
import io.onemfive.network.NetworkService;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.network.peers.PeerReport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SensorManagerBase implements SensorManager {

    protected final Map<String, Sensor> registeredSensors = new HashMap<>();
    protected final Map<String, Sensor> activeSensors = new HashMap<>();
    protected final Map<String, Sensor> blockedSensors = new HashMap<>();
    protected final Map<String, List<SensorStatusListener>> listeners = new HashMap<>();

    private PeerManager peerManager;

    protected NetworkService networkService;

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    @Override
    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    @Override
    public PeerReport getPeerReport() {
        return (PeerReport)peerManager;
    }

    @Override
    public void registerSensor(Sensor sensor) {
        registeredSensors.put(sensor.getClass().getName(), sensor);
    }

    public Map<String, Sensor> getRegisteredSensors() {
        return registeredSensors;
    }

    public Map<String, Sensor> getActiveSensors() {
        return activeSensors;
    }

    public Map<String, Sensor> getBlockedSensors(){
        return blockedSensors;
    }

    public SensorStatus getSensorStatus(String sensor) {
        Sensor s = activeSensors.get(sensor);
        if(s == null) {
            return SensorStatus.UNREGISTERED;
        } else {
            return s.getStatus();
        }
    }

    public Sensor getRegisteredSensor(String sensorName) {
        return registeredSensors.get(sensorName);
    }

    public boolean isActive(String sensorName) {
        return activeSensors.containsKey(sensorName);
    }

    @Override
    public File getSensorDirectory(String sensorName) {
        return new File(networkService.getSensorsDirectory(), sensorName);
    }

    @Override
    public boolean handleNetworkOpPacket(Packet packet, NetworkOp op) {
        return networkService.handlePacket(packet, op);
    }

    @Override
    public boolean sendToBus(Envelope envelope) {
        return networkService.sendToBus(envelope);
    }

    @Override
    public void suspend(Envelope envelope) {
        networkService.suspend(envelope);
    }

    @Override
    public boolean registerSensorStatusListener(String sensorId, SensorStatusListener listener) {
        listeners.putIfAbsent(sensorId, new ArrayList<>());
        if(!listeners.get(sensorId).contains(listener)) {
            listeners.get(sensorId).add(listener);
        }
        return true;
    }

    @Override
    public boolean unregisterSensorStatusListener(String sensorId, SensorStatusListener listener) {
        if(listeners.get(sensorId)!=null) {
            listeners.get(sensorId).remove(listener);
        }
        return true;
    }
}
