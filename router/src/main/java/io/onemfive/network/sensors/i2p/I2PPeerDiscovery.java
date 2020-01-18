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
package io.onemfive.network.sensors.i2p;

import io.onemfive.data.Envelope;
import io.onemfive.network.*;
import io.onemfive.network.ops.PingRequestOp;
import io.onemfive.network.peers.P2PRelationship;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.network.sensors.SensorTask;
import io.onemfive.util.DLC;
import io.onemfive.util.tasks.TaskRunner;

import java.util.List;
import java.util.logging.Logger;

/**
 * Cycle through all known peers randomly to build and maintain known peer database.
 *
 * @author objectorange
 */
public class I2PPeerDiscovery extends SensorTask {

    private Logger LOG = Logger.getLogger(I2PPeerDiscovery.class.getName());

    private NetworkPeer localPeer;
    private PeerManager peerManager;

    public I2PPeerDiscovery(I2PSensor sensor, TaskRunner taskRunner, NetworkPeer localPeer, PeerManager peerManager) {
        super(I2PPeerDiscovery.class.getName(), taskRunner, sensor);
        this.peerManager = peerManager;
        this.localPeer = localPeer;
        periodicity = getPeriodicity();
    }

    @Override
    public Long getPeriodicity() {
            return NetworkConfig.UI * 1000L; // wait for UI seconds
    }

    @Override
    public Boolean execute() {
        LOG.info("Running I2P Peer Discovery...");
        long totalKnown = peerManager.totalPeersByRelationship(localPeer, P2PRelationship.RelType.Known);
        if(totalKnown < 1) {
            LOG.info("No I2P peers known.");
            if(NetworkConfig.seeds!=null && NetworkConfig.seeds.size() > 0) {
                // Launch Seeds
                List<NetworkPeer> seeds = NetworkConfig.seeds.get(NetworkConfig.env);
                for (NetworkPeer seed : seeds) {
                    if(seed.getNetwork()==Network.I2P) {
                        LOG.info("Sending Peer Status Request to Seed Peer:\n\t" + seed);
                        Envelope e = Envelope.documentFactory();
                        DLC.addRoute(NetworkService.class, PingRequestOp.class.getName(), e);
                        Request request = new Request();
                        request.setOriginationPeer(localPeer);
                        request.setFromPeer(localPeer);
                        request.setDestinationPeer(seed);
                        request.setEnvelope(e);
                        sensor.sendOut(request);
                        LOG.info("Sent Peer Status Request to Seed Peer.");
                    }
                }
            } else {
                LOG.warning("No seeds available! Please provide at least one seed!");
                return false;
            }
        } else if(totalKnown < NetworkConfig.MaxPT) {
            LOG.info(totalKnown+" known peers less than Maximum Peers Tracked of "+ NetworkConfig.MaxPT+"; continuing peer discovery...");
            NetworkPeer p = peerManager.getRandomKnownPeer(localPeer);
            if(p != null) {
                LOG.info("Sending Peer Status Request to Known Peer...");
                Envelope e = Envelope.documentFactory();
                DLC.addRoute(NetworkService.class, PingRequestOp.class.getName(), e);
                Request request = new Request();
                request.setOriginationPeer(localPeer);
                request.setFromPeer(localPeer);
                request.setDestinationPeer(p);
                request.setEnvelope(e);
                sensor.sendOut(request);
                LOG.info("Sent Peer Status Request to Known Peer.");
            }
        } else {
            LOG.info("Maximum Peers Tracked of "+ NetworkConfig.MaxPT+" reached. No need to look for more.");
        }
        return true;
    }

}
