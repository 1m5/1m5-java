package io.onemfive.network.sensors.radio.technologies.bluetooth;

import io.onemfive.network.sensors.radio.BaseRadio;
import io.onemfive.network.sensors.radio.RadioPeer;
import io.onemfive.network.sensors.radio.RadioSession;
import io.onemfive.network.sensors.radio.tasks.TaskRunner;

import javax.bluetooth.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Integration with JSR-82 implementation BlueCove (http://www.bluecove.org).
 * Bluecove licensed under GPL.
 */
public class Bluetooth extends BaseRadio {

    private static final Logger LOG = Logger.getLogger(Bluetooth.class.getName());

    private Properties properties;

    private BluetoothPeer localPeer;

    private Map<String, RemoteDevice> devices = new HashMap<>();
    private Map<String, List<String>> deviceServices = new HashMap<>();
    private Map<String, BluetoothPeer> peers = new HashMap<>();

    private DeviceDiscovery deviceDiscovery;
    private ServiceDiscovery serviceDiscovery;
    private PeerDiscovery peerDiscovery;

    @Override
    public RadioSession establishSession(RadioPeer peer, Boolean autoConnect) {
        BluetoothSession session = new BluetoothSession(this);
        if(autoConnect) {
            session.connect(peer);
        }
        return session;
    }

    @Override
    public boolean start(Properties properties) {
        this.properties = properties;

//        try {
//            LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
//        } catch (BluetoothStateException e) {
//            LOG.warning(e.getLocalizedMessage());
//            return false;
//        }

        if(taskRunner==null) {
            taskRunner = new TaskRunner(sensor, properties);
        }

        deviceDiscovery = new DeviceDiscovery(devices, sensor, taskRunner, properties, 60 * 60 * 1000L);
        deviceDiscovery.setLongRunning(true);
        taskRunner.addTask(deviceDiscovery);

        serviceDiscovery = new ServiceDiscovery(devices, deviceServices, peers, sensor, taskRunner, properties, 30 * 1000L);
        serviceDiscovery.setLongRunning(true);
        taskRunner.addTask(serviceDiscovery);

        localPeer = new BluetoothPeer();
        localPeer.setAddress("1234");

        peerDiscovery = new PeerDiscovery( localPeer,this, peers, sensor, taskRunner, properties, 60 * 1000L);
        peerDiscovery.setLongRunning(true);
        taskRunner.addTask(peerDiscovery);

        if(!taskRunner.isAlive()) {
            taskRunner.start();
        }
        return true;
    }

    public static void main(String[] args) {
        Bluetooth bluetooth = new Bluetooth();
        bluetooth.start(null);
        bluetooth.shutdown();
    }
}
