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

        NetworkPeer seedA = new NetworkPeer(Network.I2P);
        // TODO: Change id to fingerprint
        seedA.setId("1234567890");
        seedA.getDid().getPublicKey().setAddress("ygfTZm-Cwhs9FI05gwHC3hr360gpcp103KRUSubJ2xvaEhFXzND8emCKXSAZLrIubFoEct5lmPYjXegykkWZOsjdvt8ZWZR3Wt79rc3Ovk7Ev4WXrgIDHjhpr-cQdBITSFW8Ay1YvArKxuEVpIChF22PlPbDg7nRyHXOqmYmrjo2AcwObs--mtH34VMy4R934PyhfEkpLZTPyN73qO4kgvrBtmpOxdWOGvlDbCQjhSAC3018xpM0qFdFSyQwZkHdJ9sG7Mov5dmG5a6D6wRx~5IEdfufrQi1aR7FEoomtys-vAAF1asUyX1UkxJ2WT2al8eIuCww6Nt6U6XfhN0UbSjptbNjWtK-q4xutcreAu3FU~osZRaznGwCHez5arT4X2jLXNfSEh01ICtT741Ki4aeSrqRFPuIove2tmUHZPt4W6~WMztvf5Oc58jtWOj08HBK6Tc16dzlgo9kpb0Vs3h8cZ4lavpRen4i09K8vVORO1QgD0VH3nIZ5Ql7K43zAAAA");
        seedA.getDid().getPublicKey().setFingerprint("bl4fi-lFyTPQQkKOPuxlF9zPGEdgtAhtKetnyEwj8t0=");
        seedA.getDid().getPublicKey().setType("ElGamal/None/NoPadding");
        seedA.getDid().getPublicKey().isIdentityKey(true);
        seedA.getDid().getPublicKey().setBase64Encoded(true);
        if(peerManager.savePeer(seedA, true)) {
            seeds.add(seedA);
        }
        periodicity = getPeriodicity();
    }

    @Override
    public Long getPeriodicity() {
        if(!peerManager.isReliable(seeds.get(0)))
            return 5 * 1000L; // Every five seconds until we have a reliable seed.
        else
            return UpdateInterval * 1000L; // wait for UI seconds
    }

    @Override
    public Boolean execute() {
        LOG.info("Running I2P Peer Discovery...");
        running = true;
        long totalKnown = peerManager.totalPeersByNetwork(localNode.getNetworkPeer().getId(), Network.I2P);
        if(totalKnown < 2) {
            LOG.info("No I2P peers beyond a seed is known. Just use seeds.");
            if(seeds!=null && seeds.size() > 0) {
                // Launch Seeds
                for (NetworkPeer seed : seeds) {
                    if(seed.getNetwork()!= Network.I2P) {
                        LOG.warning("Seed provided is not for I2P.");
                    } else if(seed.getDid().getPublicKey().getAddress().isEmpty()) {
                        LOG.warning("Seed provided does not have an address.");
                    } else if(seed.getDid().getPublicKey().getAddress().equals(localNode.getNetworkPeer(Network.I2P).getDid().getPublicKey().getAddress())) {
                        LOG.info("Seed is local peer.");
                    } else {
                        LOG.info("Sending Peer Status Request to Seed Peer:\n\t" + seed);
                        Envelope e = Envelope.documentFactory();
                        DLC.addRoute(NetworkService.class, PingRequestOp.class.getName(), e);
                        Request request = new Request();
                        request.setOriginationPeer(localNode.getNetworkPeer(Network.I2P));
                        request.setFromPeer(localNode.getNetworkPeer(Network.I2P));
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
            NetworkPeer p = peerManager.getRandomPeer(Network.I2P);
            if(p != null) {
                LOG.info("Sending Peer Status Request to Known Peer...");
                Envelope e = Envelope.documentFactory();
                DLC.addRoute(NetworkService.class, PingRequestOp.class.getName(), e);
                Request request = new Request();
                request.setOriginationPeer(localNode.getNetworkPeer(Network.I2P));
                request.setFromPeer(localNode.getNetworkPeer(Network.I2P));
                request.setDestinationPeer(p);
                request.setEnvelope(e);
                sensor.sendOut(request);
                LOG.info("Sent Peer Status Request to Known Peer.");
            }
        } else {
            LOG.info("Maximum Peers Tracked of "+ MaxPT+" reached. No need to look for more.");
        }
        running = false;
        return true;
    }

}
