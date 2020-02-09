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
package io.onemfive.network.sensors;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.NetworkConfig;
import io.onemfive.network.NetworkTask;
import io.onemfive.network.ops.PingRequestOp;
import io.onemfive.util.RandomUtil;
import io.onemfive.util.tasks.TaskRunner;

import java.util.logging.Logger;

public class NetworkPeerDiscovery extends NetworkTask  {

    private Logger LOG = Logger.getLogger(NetworkPeerDiscovery.class.getName());

    public NetworkConfig config;

    public final Network network;

    public NetworkPeerDiscovery(TaskRunner taskRunner, Sensor sensor, Network network, NetworkConfig config) {
        super(network.name()+"NetworkPeerDiscovery", taskRunner, sensor);
        this.network = network;
        this.config = config;
        periodicity = getPeriodicity();
    }

    @Override
    public Long getPeriodicity() {
        for(NetworkPeer sp : config.seeds) {
            // Do we have at least one reliable Peer for this Network?
            if(sp.getNetwork()==network && peerManager.isReliable(sp)) {
                return config.UpdateInterval * 1000L; // wait for UI seconds
            }
        }
        return config.UpdateIntervalHyper * 1000L; // Every five seconds until we have a reliable seed.
    }

    @Override
    public Boolean execute() {
        LOG.info("Running Network Peer Discovery...");
        running = true;
        long totalKnown = peerManager.totalPeersByNetwork(localNode.getNetworkPeer().getId(), network);
        if(totalKnown < 2) {
            LOG.info("No Network peers beyond a seed is known. Just use seeds.");
            // Launch Seeds
            for (NetworkPeer seed : config.seeds) {
                if(!seed.getDid().getPublicKey().getFingerprint().equals(localNode.getNetworkPeer(network).getDid().getPublicKey().getFingerprint())
                        && sendPingRequest(seed.getDid().getPublicKey().getAddress())) {
                    LOG.info("Sent Peer Status Request to Seed Peer.");
                } else {
                    LOG.warning("A problem occurred attempting to send out Peer Status Request.");
                }
            }
        } else if(totalKnown < config.MaxPT) {
            LOG.info(totalKnown+" known peers less than Maximum Peers Tracked of "+ config.MaxPT+"; continuing peer discovery...");
            NetworkPeer p = peerManager.getRandomPeer(network);
            LOG.info("Sending Peer Status Request to Known Peer...");
            if(sendPingRequest(p.getDid().getPublicKey().getAddress())) {
                LOG.info("Sent Peer Status Request to Known Peer.");
            } else {
                LOG.warning("A problem occurred attempting to send out Peer Status Request.");
            }
            LOG.info("Sent Peer Status Request to Known Peer.");
        } else {
            LOG.info("Maximum Peers Tracked of "+ config.MaxPT+" reached. No need to look for more.");
        }
        running = false;
        return true;
    }

    protected boolean sendPingRequest(String toAddress) {
        PingRequestOp op = new PingRequestOp();
        op.id = RandomUtil.nextRandomInteger();
        op.fromId = localNode.getNetworkPeer().getId();
        op.fromAddress = localNode.getNetworkPeer().getDid().getPublicKey().getAddress();
        op.fromNetworkFingerprint = localNode.getNetworkPeer(network).getDid().getPublicKey().getFingerprint();
        op.fromNetworkAddress = localNode.getNetworkPeer(network).getDid().getPublicKey().getAddress();
        op.toNetworkAddress = toAddress;
        return sensor.establishSession(null, true).send(op);
    }
}