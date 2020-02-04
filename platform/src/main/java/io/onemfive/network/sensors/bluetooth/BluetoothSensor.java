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
import io.onemfive.data.NetworkNode;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.NetworkPacket;
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

    private BluetoothDeviceDiscovery deviceDiscovery;
    private BluetoothServiceDiscovery serviceDiscovery;
    private BluetoothPeerDiscovery peerDiscovery;

    private Map<Integer, BluetoothSession> leased = new HashMap<>();

    public BluetoothSensor() {
        super(Network.Bluetooth);
    }

    public BluetoothSensor(SensorManager sensorManager) {
        super(sensorManager, Network.Bluetooth);
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
        BluetoothSession session = new BluetoothSession(this);
        if(session.open(peer)) {
            if (autoConnect) {
                session.connect();
            }
            sessions.put(session.getId(), session);
        }
        return session;
    }

    /**
     * Sends UTF-8 content to a Bluetooth Peer.
     * @param packet Envelope containing Packet as data.
     * @return boolean was successful
     */
    @Override
    public boolean sendOut(NetworkPacket packet) {
        LOG.info("Sending Packet via Bluetooth...");

        NetworkPeer toPeer = packet.getToPeer();
        if(toPeer == null) {
            LOG.warning("No Peer for Radio found in toDID while sending to Radio.");
            return false;
        }

        if(toPeer.getNetwork() != Network.Bluetooth) {
            LOG.warning("Not a Bluetooth Request.");
            return false;
        }

        if(packet.getEnvelope() == null) {
            LOG.warning("No content found in Envelope while sending to Radio.");
            return false;
        }
        LOG.info("Envelope to send: "+packet.getEnvelope().toString());

        BluetoothSession session = (BluetoothSession) establishSession(packet.getToPeer(), true);
        return session.send(packet);
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return super.sendIn(envelope);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
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
        NetworkNode localNode = sensorManager.getPeerManager().getLocalNode();
        try {
            localPeer = localNode.getNetworkPeer(Network.Bluetooth);
            if(localPeer==null) {
                localPeer = new NetworkPeer(Network.Bluetooth);
                localNode.addNetworkPeer(localPeer);
            }
            String localAddress = LocalDevice.getLocalDevice().getBluetoothAddress();
            localPeer.getDid().setUsername(LocalDevice.getLocalDevice().getFriendlyName());
            localPeer.getDid().getPublicKey().setAddress(localAddress);
            localPeer.getDid().setPassphrase(localNode.getNetworkPeer().getDid().getPassphrase());
            if (!localAddress.equals(localPeer.getDid().getPublicKey().getAddress())
                    || localPeer.getDid().getPublicKey().getAttribute("uuid") == null) {
                // New address or no UUID
//                localPeer.getDid().getPublicKey().addAttribute("uuid", UUID.randomUUID().toString());
                // TODO: Remove hard-coding
                localPeer.getDid().getPublicKey().addAttribute("uuid", "11111111111111111111111111111123");
            }
            sensorManager.getPeerManager().savePeer(localPeer, true);
        } catch (BluetoothStateException e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
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

        // TODO: Increase periodicity once a threshold of known peers is established
        // run every minute
        deviceDiscovery = new BluetoothDeviceDiscovery(this, taskRunner);
        deviceDiscovery.setPeriodicity(60 * 1000L);
        deviceDiscovery.setLongRunning(true);
        taskRunner.addTask(deviceDiscovery);

        // run every minute 20 seconds after device discovery
        serviceDiscovery = new BluetoothServiceDiscovery(sensorManager.getPeerManager(), this, taskRunner);
        serviceDiscovery.setPeriodicity(60 * 1000L);
        serviceDiscovery.setLongRunning(true);
        serviceDiscovery.setDelayed(true);
        serviceDiscovery.setFixedDelay(true);
        serviceDiscovery.setDelayTimeMS(20 * 1000L);
        taskRunner.addTask(serviceDiscovery);

        // run every minute 20 seconds after service discovery
        peerDiscovery = new BluetoothPeerDiscovery(sensorManager.getPeerManager(), this, taskRunner);
        peerDiscovery.setPeriodicity(60 * 1000L);
        peerDiscovery.setLongRunning(true);
        peerDiscovery.setDelayed(true);
        peerDiscovery.setFixedDelay(true);
        peerDiscovery.setDelayTimeMS(40 * 1000L);
        taskRunner.addTask(peerDiscovery);

        Thread taskRunnerThread = new Thread(taskRunner);
        taskRunnerThread.setName("Bluetooth-Sensor-TaskRunner");
        taskRunnerThread.setDaemon(true);
        taskRunnerThread.start();

        LOG.info("Started.");
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

    @Override
    public boolean shutdown() {
        super.shutdown();
        taskRunner.removeTask(peerDiscovery, true);
        taskRunner.removeTask(serviceDiscovery, true);
        taskRunner.removeTask(deviceDiscovery, true);
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        super.gracefulShutdown();
        taskRunner.removeTask(peerDiscovery, false);
        taskRunner.removeTask(serviceDiscovery, false);
        taskRunner.removeTask(deviceDiscovery, false);
        return true;
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
