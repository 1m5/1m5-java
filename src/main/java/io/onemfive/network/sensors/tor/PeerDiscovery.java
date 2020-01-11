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
package io.onemfive.network.sensors.tor;

import io.onemfive.util.tasks.TaskRunner;
import io.onemfive.data.Envelope;
import io.onemfive.data.Packet;
import io.onemfive.data.Sensitivity;
import io.onemfive.util.DLC;
import io.onemfive.network.NetworkService;
import io.onemfive.network.NetworkTask;
import io.onemfive.network.ops.PingRequestOp;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class PeerDiscovery extends NetworkTask {

    private static final Logger LOG = Logger.getLogger(PeerDiscovery.class.getName());

    private TorSensor sensor;
    private TorPeer localPeer;
    private Map<String, TorPeer> peers;

    public PeerDiscovery(TorPeer localPeer, Map<String, TorPeer> peers, TorSensor sensor, TaskRunner taskRunner, Properties properties, long periodicity) {
        super(PeerDiscovery.class.getName(), taskRunner, properties, periodicity);
        this.localPeer = localPeer;
        this.peers = peers;
        this.sensor = sensor;
    }

    @Override
    public Boolean execute() {
        started = true;
        if(peers != null && peers.size() > 0) {
            Collection<TorPeer> peersList = peers.values();
            for (TorPeer peer : peersList) {
                Envelope envelope = Envelope.documentFactory();
                envelope.setSensitivity(Sensitivity.MEDIUM);
                DLC.addExternalRoute(NetworkService.class, PingRequestOp.class.getName(), envelope, localPeer, peer);
                Packet packet = new Packet();
                packet.setOriginationPeer(localPeer);
                packet.setFromPeer(localPeer);
                packet.setDestinationPeer(peer);
                packet.setToPeer(peer);
                packet.setEnvelope(envelope);
                sensor.sendOut(packet);
            }
        }
        lastCompletionTime = System.currentTimeMillis();
        started = false;
        return true;
    }
}
