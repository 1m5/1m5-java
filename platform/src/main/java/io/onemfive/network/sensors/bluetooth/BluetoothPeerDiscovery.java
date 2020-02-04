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

import io.onemfive.data.*;
import io.onemfive.network.NetworkTask;
import io.onemfive.network.NetworkPacket;
import io.onemfive.network.Request;
import io.onemfive.network.ops.OpsPacket;
import io.onemfive.network.peers.P2PRelationship;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.network.sensors.SensorSession;
import io.onemfive.util.DLC;
import io.onemfive.util.tasks.TaskRunner;

import java.util.logging.Logger;

public class BluetoothPeerDiscovery extends NetworkTask {

    private static final Logger LOG = Logger.getLogger(BluetoothPeerDiscovery.class.getName());

    private SensorSession session;
    private PeerManager peerManager;

    public BluetoothPeerDiscovery(PeerManager peerManager, BluetoothSensor sensor, TaskRunner taskRunner) {
        super(BluetoothPeerDiscovery.class.getName(), taskRunner, sensor);
        this.peerManager = peerManager;
    }

    @Override
    public Boolean execute() {
        running = true;
        NetworkPeer peer = peerManager.getRandomPeer(Network.Bluetooth);
        if(peer==null) {
            LOG.info("No Bluetooth Peers yet for Peer Discovery. Waiting on Device Discovery results...");
        } else {
            if (session == null)
                session = sensor.establishSession(peer, true);
            OpsPacket packet = new OpsPacket();
//            packet.atts.put("url", session.getURL());
            packet.atts.put(OpsPacket.FROM_ADDRESS, peerManager.getLocalNode().getNetworkPeer(Network.Bluetooth).getDid().getPublicKey().getAddress());
            packet.atts.put(OpsPacket.FROM_ID, peerManager.getLocalNode().getNetworkPeer(Network.Bluetooth).getId());
            packet.atts.put(OpsPacket.TO_ADDRESS, peer.getDid().getPublicKey().getAddress());
            packet.atts.put(OpsPacket.TO_ID, peer.getId());
            // TODO: Replace payload with status information
            session.send(packet);
        }
        lastCompletionTime = System.currentTimeMillis();
        running = false;
        return true;
    }

}
