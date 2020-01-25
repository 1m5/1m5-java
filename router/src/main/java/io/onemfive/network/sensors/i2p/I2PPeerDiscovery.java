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
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.Request;
import io.onemfive.network.*;
import io.onemfive.network.ops.PingRequestOp;
import io.onemfive.network.peers.P2PRelationship;
import io.onemfive.network.NetworkTask;
import io.onemfive.util.DLC;
import io.onemfive.util.tasks.TaskRunner;

import java.util.*;
import java.util.logging.Logger;

/**
 * Cycle through all known peers randomly to build and maintain known peer database.
 *
 * @author objectorange
 */
public class I2PPeerDiscovery extends NetworkTask {

    private Logger LOG = Logger.getLogger(I2PPeerDiscovery.class.getName());

    // Seeds
    public static List<NetworkPeer> seeds = new ArrayList<>();
    // Banned
    public static List<NetworkPeer> banned = new ArrayList<>();
    // Min Peers Tracked - the point at which Discovery process goes into 'hyper' mode.
    public static int MinPT = 10;
    // Max Peers Tracked - the total number of Peers to attempt to maintain knowledge of
    public static int MaxPT = 100;
    // Max Peers Sent - Maximum number of peers to send in a peer list (the bigger a datagram, the less chance of it getting through).
    public static int MaxPS = 5;
    // Max Acknowledgments Tracked
    public static int MaxAT = 20;
    // Update Interval - seconds between Discovery process
    public static int UpdateInterval = 60;
    // Reliable Peer Min Acks
    public static int MinAckRP = 20;
    // Super Reliable Peer Min Acks
    public static int MinAckSRP = 10000;

    public I2PPeerDiscovery(I2PSensor sensor, TaskRunner taskRunner) {
        super(I2PPeerDiscovery.class.getName(), taskRunner, sensor);

        periodicity = getPeriodicity();

        NetworkPeer seedA = new NetworkPeer(Network.I2P);
        seedA.getDid().getPublicKey().setAddress("6CmEr1T5hbxrdJda4KI1oVF7TN2ifiFNao4EKd9IWQiAh5HLyh0etfIbtNnK7wrW27o1ArjuIDdsdxP1CxDeH2Rm~xHAmgGS59j20DdIeDm0zJX4seQXBjQw044d0Wo76Sb8Ap6iZJ8akiiLztiH2XKY1XXKX7ZopdZj76GdjNKbmJFs1r7vDgk2DL1GKA22JTFz89-F3RXktVGhhSMVoBqbQDQI3T73qGS20gku5nZBbOhid7XYQIxsG~k9Por-YO6OtpaGY2elIgIWhOtc0trdccemojmDCTJok-8ammddvC~FXHb2PDCq5dRRbAZWZywXYdNClm1tgXNd5ux88XpUiWHTK7Hrw9cVClJxz3PBwHlWflhAXcsK6YdBr9FmOXZw475LMEhM3Vy8vKr7v1iuWUwlMhMiJLtZ5uWqqqOae9O7QRloQYzzdr2GFIohICxQ~xtIt4bLVbIawnxpiqSKLAAcKtbWIV8skdsCUePbQeE9aRftEZ5SorK-yW8XAAAA");
        seedA.getDid().getPublicKey().setFingerprint("aXHaBjEuP39ucGhzNuKY9EnJA~KLJrhorqBIzY6liQo=");
        seedA.getDid().getPublicKey().setType("ElGamal/None/NoPadding");
        seedA.getDid().getPublicKey().isIdentityKey(true);
        seedA.getDid().getPublicKey().setBase64Encoded(true);
        seedA.setLocal(true);
        if(peerManager.savePeer(seedA, true)) {
            seedA = peerManager.loadPeer(seedA);
            seeds.add(seedA);
            localNode.addLocalNetworkPeer(seedA);
        }
    }

    @Override
    public Long getPeriodicity() {
        P2PRelationship rel = peerManager.getRelationship(localNode.getLocalNetworkPeer(Network.I2P), seeds.get(0), P2PRelationship.RelType.I2P);
        if(!rel.isReliable())
            return 5 * 1000L; // Every five seconds until we have a reliable seed.
        else
            return UpdateInterval * 1000L; // wait for UI seconds
    }

    @Override
    public Boolean execute() {
        LOG.info("Running I2P Peer Discovery...");
        started = true;
        long totalKnown = peerManager.totalPeersByRelationship(localNode.getLocalNetworkPeer(Network.I2P), P2PRelationship.RelType.I2P);
        if(totalKnown < 2) {
            LOG.info("No I2P peers beyond a seed is known. Just use seeds.");
            if(seeds!=null && seeds.size() > 0) {
                // Launch Seeds
                for (NetworkPeer seed : seeds) {
                    if(seed.getNetwork()!= Network.I2P) {
                        LOG.warning("Seed provided is not for I2P.");
                    } else if(seed.getDid().getPublicKey().getAddress().isEmpty()) {
                        LOG.warning("Seed provided does not have an address.");
                    } else if(seed.getDid().getPublicKey().getAddress().equals(localNode.getLocalNetworkPeer(Network.I2P).getDid().getPublicKey().getAddress())) {
                        LOG.info("Seed is local peer.");
                    } else {
                        LOG.info("Sending Peer Status Request to Seed Peer:\n\t" + seed);
                        Envelope e = Envelope.documentFactory();
                        DLC.addRoute(NetworkService.class, PingRequestOp.class.getName(), e);
                        Request request = new Request();
                        request.setOriginationPeer(localNode.getLocalNetworkPeer(Network.I2P));
                        request.setFromPeer(localNode.getLocalNetworkPeer(Network.I2P));
                        request.setDestinationPeer(seed);
                        request.setToPeer(seed);
                        request.setEnvelope(e);
                        if(sensor.sendOut(request)) {
                            LOG.info("Sent Peer Status Request to Seed Peer.");
                        } else {
                            LOG.warning("A problem occurred attempting to send out Peer Status Request.");
                        }
                    }
                }
            } else {
                LOG.info("No seeds provided.");
                return false;
            }
        } else if(totalKnown < MaxPT) {
            LOG.info(totalKnown+" known peers less than Maximum Peers Tracked of "+ MaxPT+"; continuing peer discovery...");
            NetworkPeer p = peerManager.getRandomKnownPeer(localNode.getLocalNetworkPeer(Network.I2P));
            if(p != null) {
                LOG.info("Sending Peer Status Request to Known Peer...");
                Envelope e = Envelope.documentFactory();
                DLC.addRoute(NetworkService.class, PingRequestOp.class.getName(), e);
                Request request = new Request();
                request.setOriginationPeer(localNode.getLocalNetworkPeer(Network.I2P));
                request.setFromPeer(localNode.getLocalNetworkPeer(Network.I2P));
                request.setDestinationPeer(p);
                request.setEnvelope(e);
                sensor.sendOut(request);
                LOG.info("Sent Peer Status Request to Known Peer.");
            }
        } else {
            LOG.info("Maximum Peers Tracked of "+ MaxPT+" reached. No need to look for more.");
        }
        started = false;
        return true;
    }

}
