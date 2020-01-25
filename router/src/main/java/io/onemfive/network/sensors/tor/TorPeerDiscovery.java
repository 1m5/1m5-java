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
import io.onemfive.network.peers.P2PRelationship;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.network.NetworkTask;
import io.onemfive.util.tasks.TaskRunner;
import io.onemfive.util.DLC;
import io.onemfive.network.ops.PingRequestOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TorPeerDiscovery extends NetworkTask {

    private static final Logger LOG = Logger.getLogger(TorPeerDiscovery.class.getName());

    // Hidden Services
    public static List<NetworkPeer> hiddenServices = new ArrayList<>();
    // Banned
    public static List<NetworkPeer> banned = new ArrayList<>();

    public TorPeerDiscovery(TorSensor sensor, TaskRunner taskRunner) {
        super(TorPeerDiscovery.class.getName(), taskRunner, sensor);

    }

    @Override
    public Boolean execute() {
        LOG.info("Running Tor Peer Discovery...");
        started = true;
        long totalKnown = peerManager.totalPeersByRelationship(localNode.getLocalNetworkPeer(Network.TOR), P2PRelationship.RelType.TOR);
        LOG.info(totalKnown+" Tor peers known.");
        if(hiddenServices!=null && hiddenServices.size() > 0) {
            // Verify Tor Hidden Services Status
            for (NetworkPeer hiddenService : hiddenServices) {
                if(hiddenService.getNetwork()!= Network.TOR) {
                    LOG.warning("HiddenService provided is not for TOR.");
                } else if(hiddenService.getDid().getPublicKey().getAddress().isEmpty()) {
                    LOG.warning("HiddenService provided does not have an address.");
                } else if(hiddenService.getDid().getPublicKey().getAddress().equals(localNode.getLocalNetworkPeer(Network.TOR).getDid().getPublicKey().getAddress())) {
                    LOG.info("HiddenService is local peer.");
                    hiddenServices.remove(hiddenService);
                } else {
                    LOG.info("Sending Peer Status Request to HiddenService Peer:\n\t" + hiddenService);
                    Envelope e = Envelope.documentFactory();
                    DLC.addRoute(NetworkService.class, PingRequestOp.class.getName(), e);
                    Request request = new Request();
                    request.setOriginationPeer(localNode.getLocalNetworkPeer(Network.TOR));
                    request.setFromPeer(localNode.getLocalNetworkPeer(Network.TOR));
                    request.setDestinationPeer(hiddenService);
                    request.setToPeer(hiddenService);
                    request.setEnvelope(e);
                    if(sensor.sendOut(request)) {
                        LOG.info("Sent Peer Status Request to HiddenService.");
                    } else {
                        LOG.warning("A problem occurred attempting to send out Peer Status Request.");
                    }
                }
            }
        } else {
            LOG.info("No hidden services provided.");
            return false;
        }
        started = false;
        return true;
    }
}
