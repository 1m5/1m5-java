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

import io.onemfive.data.*;
import io.onemfive.network.*;
import io.onemfive.network.sensors.SensorTask;
import io.onemfive.util.tasks.TaskRunner;
import io.onemfive.util.DLC;
import io.onemfive.network.ops.PingRequestOp;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

public class TorPeerDiscovery extends SensorTask {

    private static final Logger LOG = Logger.getLogger(TorPeerDiscovery.class.getName());

    private NetworkPeer localPeer;
    private Map<String, NetworkPeer> peers;

    public TorPeerDiscovery(NetworkPeer localPeer, Map<String, NetworkPeer> peers, TorSensor sensor, TaskRunner taskRunner) {
        super(TorPeerDiscovery.class.getName(), taskRunner, sensor);
        this.localPeer = localPeer;
        this.peers = peers;
    }

    @Override
    public Boolean execute() {
        started = true;
        if(peers != null && peers.size() > 0) {
            Collection<NetworkPeer> peersList = peers.values();
            for (NetworkPeer peer : peersList) {
                Envelope envelope = Envelope.documentFactory();
                envelope.setManCon(ManCon.MEDIUM);
                DLC.addExternalRoute(NetworkService.class, PingRequestOp.class.getName(), envelope, localPeer, peer);
                Packet packet = new Request();
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
