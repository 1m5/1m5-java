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

import io.onemfive.data.Envelope;
import io.onemfive.data.TextMessage;
import io.onemfive.network.NetworkPeer;
import io.onemfive.network.Packet;
import io.onemfive.network.sensors.radio.RadioSensor;
import io.onemfive.network.sensors.radio.RadioSession;
import io.onemfive.network.sensors.radio.tasks.RadioTask;
import io.onemfive.network.sensors.radio.tasks.TaskRunner;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class PeerDiscovery extends RadioTask {

    private static final Logger LOG = Logger.getLogger(PeerDiscovery.class.getName());

    private NetworkPeer localPeer;
    private Bluetooth radio;
    private Map<String, NetworkPeer> peers;

    public PeerDiscovery(NetworkPeer localPeer, Bluetooth radio, Map<String, NetworkPeer> peers, RadioSensor sensor, TaskRunner taskRunner, Properties properties, long periodicity) {
        super(sensor, taskRunner, properties, periodicity);
        this.localPeer = localPeer;
        this.radio = radio;
        this.peers = peers;
        startRunning = false;
    }

    @Override
    public boolean runTask() {
        if(super.runTask()) {
            started = true;
            if(peers != null && peers.size() > 0) {
                Collection<NetworkPeer> peersList = peers.values();
                Envelope e;
                for (NetworkPeer peer : peersList) {
                    RadioSession session = radio.establishSession(peer, true);
                    Packet packet = new Packet();
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
        } else {
            return false;
        }
    }
}
