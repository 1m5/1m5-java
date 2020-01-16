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
package io.onemfive.network.sensors.radio.technologies.bluetooth;

import io.onemfive.network.Network;
import io.onemfive.network.NetworkPeer;
import io.onemfive.network.sensors.radio.BaseRadio;
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

    private NetworkPeer localPeer;

    private Map<String, RemoteDevice> devices = new HashMap<>();
    private Map<String, List<String>> deviceServices = new HashMap<>();
    private Map<String, NetworkPeer> peers = new HashMap<>();

    private DeviceDiscovery deviceDiscovery;
    private ServiceDiscovery serviceDiscovery;
    private PeerDiscovery peerDiscovery;

    @Override
    public RadioSession establishSession(NetworkPeer peer, Boolean autoConnect) {
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

        localPeer = new NetworkPeer(Network.RADIO_BLUETOOTH);
        localPeer.getDid().getPublicKey().setAddress("1234");

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
