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
import io.onemfive.data.TextMessage;
import io.onemfive.network.NetworkPeer;
import io.onemfive.network.NetworkTask;
import io.onemfive.network.Packet;
import io.onemfive.network.Request;
import io.onemfive.network.sensors.SensorSession;
import io.onemfive.util.tasks.TaskRunner;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class BluetoothPeerDiscovery extends NetworkTask {

    private static final Logger LOG = Logger.getLogger(BluetoothPeerDiscovery.class.getName());

    private NetworkPeer localPeer;
    private BluetoothSensor sensor;
    private Map<String, NetworkPeer> peers;

    public BluetoothPeerDiscovery(NetworkPeer localPeer, Map<String, NetworkPeer> peers, BluetoothSensor sensor, TaskRunner taskRunner, Properties properties, long periodicity) {
        super(BluetoothPeerDiscovery.class.getName(), taskRunner, properties, periodicity);
        this.localPeer = localPeer;
        this.sensor = sensor;
        this.peers = peers;
    }

    @Override
    public Boolean execute() {
        started = true;
        if(peers != null && peers.size() > 0) {
            Collection<NetworkPeer> peersList = peers.values();
            Envelope e;
            for (NetworkPeer peer : peersList) {
                SensorSession session = sensor.establishSession(peer, true);
                Packet packet = new Request();
                packet.setOriginationPeer(localPeer);
                packet.setFromPeer(localPeer);
                packet.setToPeer(peer);
                packet.setDestinationPeer(peer);
                e = Envelope.textFactory();
                ((TextMessage)e.getMessage()).setText("Hola Gaia!-"+System.currentTimeMillis());
                packet.setEnvelope(e);
                session.send(packet);
            }
        }
        lastCompletionTime = System.currentTimeMillis();
        started = false;
        return true;
    }
}
