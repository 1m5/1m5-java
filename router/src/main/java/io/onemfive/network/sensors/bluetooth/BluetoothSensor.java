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
package io.onemfive.network.sensors.bluetooth;

import io.onemfive.data.Envelope;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.Packet;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorSession;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.util.tasks.TaskRunner;

import javax.bluetooth.*;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Integration with JSR-82 implementation BlueCove (http://www.bluecove.org).
 * Bluecove licensed under GPL.
 */
public class BluetoothSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(BluetoothSensor.class.getName());

    private String bluetoothBaseDir;
    private File bluetoothDir;

    private Map<String, RemoteDevice> devices = new HashMap<>();
    private Map<String, List<String>> deviceServices = new HashMap<>();
    private Map<String, NetworkPeer> peers = new HashMap<>();

    private BluetoothDeviceDiscovery deviceDiscovery;
    private ServiceDiscovery serviceDiscovery;
    private BluetoothPeerDiscovery peerDiscovery;

    public BluetoothSensor() {
        super(new NetworkPeer(Network.RADIO_BLUETOOTH));
    }

    public BluetoothSensor(SensorManager sensorManager) {
        super(sensorManager, new NetworkPeer(Network.RADIO_BLUETOOTH));
    }

    @Override
    public String[] getOperationEndsWith() {
        return new String[]{".bt"};
    }

    @Override
    public String[] getURLBeginsWith() {
        return new String[]{"bt"};
    }

    @Override
    public String[] getURLEndsWith() {
        return new String[]{".bt"};
    }

    @Override
    public SensorSession establishSession(NetworkPeer peer, Boolean autoConnect) {
        BluetoothSession session = new BluetoothSession();
        if(autoConnect) {
            session.connect();
        }
        return session;
    }

    /**
     * Sends UTF-8 content to a Radio Peer using Software Defined Radio (SDR).
     * @param packet Envelope containing SensorRequest as data.
     *                 To DID must contain base64 encoded Radio destination key.
     * @return boolean was successful
     */
    @Override
    public boolean sendOut(Packet packet) {
        LOG.info("Sending Radio Message...");
//        Envelope envelope = packet.getEnvelope();
//        NetworkRequest request = (NetworkRequest) DLC.getData(NetworkRequest.class,envelope);
//        if(request == null){
//            LOG.warning("No SensorRequest in Envelope.");
//            request.statusCode = ServiceMessage.REQUEST_REQUIRED;
//            return false;
//        }
//        NetworkPeer toPeer = request.destination.getPeer(Network.SDR.name());
//        if(toPeer == null) {
//            LOG.warning("No Peer for Radio found in toDID while sending to Radio.");
//            request.statusCode = NetworkRequest.DESTINATION_PEER_REQUIRED;
//            return false;
//        }
//        if(!Network.SDR.name().equals((toPeer.getNetwork()))) {
//            LOG.warning("Radio requires an SDR Peer.");
//            request.statusCode = NetworkRequest.DESTINATION_PEER_WRONG_NETWORK;
//            return false;
//        }
//        NetworkPeer fromPeer = request.origination.getPeer(Network.SDR.name());
//        LOG.info("Content to send: "+request.content);
//        if(request.content == null) {
//            LOG.warning("No content found in Envelope while sending to Radio.");
//            request.statusCode = NetworkRequest.NO_CONTENT;
//            return false;
//        }

//        Radio radio = RadioSelector.determineBestRadio(toRPeer);
//        if(radio==null) {
//            LOG.warning("Unhandled issue #1 here.");
//            return false;
//        }
//        RadioSession session = radio.establishSession(toRPeer, true);
//        if(session==null) {
//            LOG.warning("Unhandled issue #2 here.");
//            return false;
//        }
//        RadioDatagram datagram = session.toRadioDatagram(request);
//        Properties options = new Properties();
//        if(session.sendDatagram(datagram)) {
//            LOG.info("Radio Message sent.");
//            return true;
//        } else {
//            LOG.warning("Radio Message sending failed.");
//            request.statusCode = NetworkRequest.SENDING_FAILED;
//            return false;
//        }
        return true;
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return super.sendIn(envelope);
    }

    /**
     * Will be called only if you register via addSessionListener().
     *
     * After this is called, the client should call receiveMessage(msgId).
     * There is currently no method for the client to reject the message.
     * If the client does not call receiveMessage() within a timeout period
     * (currently 30 seconds), the session will delete the message and
     * log an error.
     *
     * @param session session to notify
     */
    public void messageAvailable(BluetoothSession session) {
//        RadioDatagram d = session.receiveDatagram(port);
//        LOG.info("Received Radio Message:\n\tFrom: " + d.from.getSDRAddress());
//        Envelope e = Envelope.eventFactory(EventMessage.Type.TEXT);
//        DID did = new DID();
//        did.addPeer(d.from);
//        e.setDID(did);
//        EventMessage m = (EventMessage) e.getMessage();
//        m.setName(d.from.getSDRFingerprint());
//        m.setMessage(d);
//        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
//        LOG.info("Sending Event Message to Notification Service...");
//        sendIn(e);
    }



    @Override
    public boolean start(Properties properties) {
        this.properties = properties;
        updateStatus(SensorStatus.STARTING);
        bluetoothBaseDir = properties.getProperty("1m5.dir.sensors")+"/bluetooth";
        bluetoothDir = new File(bluetoothBaseDir);
        if (!bluetoothDir.exists()) {
            if (!bluetoothDir.mkdir()) {
                LOG.severe("Unable to create Bluetooth base directory: " + bluetoothBaseDir + "; exiting...");
                return false;
            }
        }
        properties.setProperty("bluetooth.dir.base", bluetoothBaseDir);
        properties.setProperty("1m5.dir.sensors.bluetooth", bluetoothBaseDir);
        // Config Directory
        String configDir = bluetoothDir + "/config";
        File configFolder = new File(configDir);
        if(!configFolder.exists())
            if(!configFolder.mkdir())
                LOG.warning("Unable to create Bluetooth config directory: " +configDir);
        if(configFolder.exists()) {
            System.setProperty("bluetooth.dir.config",configDir);
            properties.setProperty("bluetooth.dir.config",configDir);
        }
        // Router Directory
        String routerDir = bluetoothDir + "/router";
        File routerFolder = new File(routerDir);
        if(!routerFolder.exists())
            if(!routerFolder.mkdir())
                LOG.warning("Unable to create Bluetooth router directory: "+routerDir);
        if(routerFolder.exists()) {
            System.setProperty("bluetooth.dir.router",routerDir);
            properties.setProperty("bluetooth.dir.router",routerDir);
        }
        // PID Directory
        String pidDir = bluetoothDir + "/pid";
        File pidFolder = new File(pidDir);
        if(!pidFolder.exists())
            if(!pidFolder.mkdir())
                LOG.warning("Unable to create Bluetooth PID directory: "+pidDir);
        if(pidFolder.exists()) {
            System.setProperty("bluetooth.dir.pid",pidDir);
            properties.setProperty("bluetooth.dir.pid",pidDir);
        }
        // Log Directory
        String logDir = bluetoothDir + "/log";
        File logFolder = new File(logDir);
        if(!logFolder.exists())
            if(!logFolder.mkdir())
                LOG.warning("Unable to create Bluetooth log directory: "+logDir);
        if(logFolder.exists()) {
            System.setProperty("bluetooth.dir.log",logDir);
            properties.setProperty("bluetooth.dir.log",logDir);
        }

        try {
            localPeer.getDid().getPublicKey().setAddress(LocalDevice.getLocalDevice().getBluetoothAddress());
            localPeer.getDid().setUsername(LocalDevice.getLocalDevice().getFriendlyName());
        } catch (BluetoothStateException e) {
            LOG.warning(e.getLocalizedMessage());
            return true;
        }
//        try {
//            LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
//        } catch (BluetoothStateException e) {
//            LOG.warning(e.getLocalizedMessage());
//            return false;
//        }

        if(taskRunner==null) {
            taskRunner = new TaskRunner(4,4);
        }

        // run every 2 minutes
        deviceDiscovery = new BluetoothDeviceDiscovery(devices, this, taskRunner);
        deviceDiscovery.setPeriodicity(2 * 60 * 1000L);
        deviceDiscovery.setLongRunning(true);
        taskRunner.addTask(deviceDiscovery);

        // run every 60 seconds
        peerDiscovery = new BluetoothPeerDiscovery(localPeer, peers, this, taskRunner);
        peerDiscovery.setPeriodicity(60 * 1000L);
        peerDiscovery.setLongRunning(true);
        taskRunner.addTask(peerDiscovery);

        // run every 30 seconds
        serviceDiscovery = new ServiceDiscovery(devices, deviceServices, peers, this, taskRunner);
        serviceDiscovery.setPeriodicity(30 * 1000L);
        serviceDiscovery.setLongRunning(true);
        taskRunner.addTask(serviceDiscovery);

//        new Thread(taskRunner, BluetoothSensor.class.getSimpleName()+"TaskRunnerThread").start();

        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

//    public static void main(String[] args) {
//        NetworkPeer np = new NetworkPeer(Network.RADIO_BLUETOOTH);
//        BluetoothSensor s = new BluetoothSensor(np);
//        s.start(null);
//        SensorSession sess = s.establishSession(np, true);
        // Discovery here

        // Get dest peer here and build request
//        Request request = new Request();
//        request.setOriginationPeer(np);
//        request.setDestinationPeer(destPeer);
//        sess.send();
//        s.shutdown();
//    }
}
