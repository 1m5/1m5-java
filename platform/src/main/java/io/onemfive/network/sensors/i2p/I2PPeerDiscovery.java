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
import io.onemfive.network.ops.OpsPacket;
import io.onemfive.network.ops.PingRequestOp;
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

        NetworkPeer seedA1M5 = new NetworkPeer();
        seedA1M5.setId("+sKVViuz2FPsl/XQ+Da/ivbNfOI=");
        seedA1M5.getDid().getPublicKey().setAddress("mQENBF43FaEDCACtMtZJu3oSchRgtaUzTmMJRbJmdfSpEaG2nW7U2YinHeMUkIpFCQGu2/OgmCuE4kVEQ4y6kKvqCiMvahtv+OqID0Lk7JEofFpwH8UUUis+p99qnw7RYy1q4IrjBpFSZHLi/nCyZOp4L7jG0CgJEFoZZEd2Uby1vnmePxts7srWkBjlmUWj+e/G89r+ZYpRN7dwdwl69Qk2s3UWTq1xyVyMqg/RuFC9kUgsmkL8vIpO4KYX7DfRKmYT29gfwjrvbVd18oeFECFVU/E6118N4P/8zIj0vhOiuar5hdKiq3oU5ka1hlQqP3IrQz2+feh2Q34+TP/BBEKOvbSv6V/6/6T/ABEBAAG0BUFsaWNliQEuBBMDAgAYBQJeNxWkAhsDBAsJCAcGFQgCCQoLAh4BAAoJEPg2v4r2zXzihH8H/iKc0ZBoWbeP/FykApYjG9m8ze54Pr9noRUw7JDAs6a7Y4IjNuE42NLMMwcxCoekzVmUwMyLrQDW+pLMaZupX2i8yU720F9WMh4f9eC4lXg64IMTnNUZqI4U52wZV22nxiGdGqacHwSSRcG5rHBskdrOJ8BX0QQ7Qt+iw4xyaxMPSPnULiJv3Z+kwLVLbxMQsmtLy7BZW6Pn848oONRNodg9tWn3PA/jTFg4ak+9lzfc1HnAWe/FeQ7O6jZ3h5eAbC4Y9KQqxVI7QzOkwIpRHMbkrVHdEcZMOa36wznC6SCXxpB/uGNrVnCJ0og9RN701QbxOu0XcevMjAOcE5dsC3g=");
        seedA1M5.getDid().getPublicKey().setFingerprint("+sKVViuz2FPsl/XQ+Da/ivbNfOI=");
        seedA1M5.getDid().getPublicKey().setType("RSA2048");
        seedA1M5.getDid().getPublicKey().isIdentityKey(true);
        seedA1M5.getDid().getPublicKey().setBase64Encoded(true);
        if(peerManager.savePeer(seedA1M5, true)) {
            seeds.add(seedA1M5);
        }

        NetworkPeer seedAI2P = new NetworkPeer(Network.I2P);
        // TODO: Change id to fingerprint
        seedAI2P.setId("+sKVViuz2FPsl/XQ+Da/ivbNfOI=");
        seedAI2P.getDid().getPublicKey().setAddress("ygfTZm-Cwhs9FI05gwHC3hr360gpcp103KRUSubJ2xvaEhFXzND8emCKXSAZLrIubFoEct5lmPYjXegykkWZOsjdvt8ZWZR3Wt79rc3Ovk7Ev4WXrgIDHjhpr-cQdBITSFW8Ay1YvArKxuEVpIChF22PlPbDg7nRyHXOqmYmrjo2AcwObs--mtH34VMy4R934PyhfEkpLZTPyN73qO4kgvrBtmpOxdWOGvlDbCQjhSAC3018xpM0qFdFSyQwZkHdJ9sG7Mov5dmG5a6D6wRx~5IEdfufrQi1aR7FEoomtys-vAAF1asUyX1UkxJ2WT2al8eIuCww6Nt6U6XfhN0UbSjptbNjWtK-q4xutcreAu3FU~osZRaznGwCHez5arT4X2jLXNfSEh01ICtT741Ki4aeSrqRFPuIove2tmUHZPt4W6~WMztvf5Oc58jtWOj08HBK6Tc16dzlgo9kpb0Vs3h8cZ4lavpRen4i09K8vVORO1QgD0VH3nIZ5Ql7K43zAAAA");
        seedAI2P.getDid().getPublicKey().setFingerprint("bl4fi-lFyTPQQkKOPuxlF9zPGEdgtAhtKetnyEwj8t0=");
        seedAI2P.getDid().getPublicKey().setType("ElGamal/None/NoPadding");
        seedAI2P.getDid().getPublicKey().isIdentityKey(true);
        seedAI2P.getDid().getPublicKey().setBase64Encoded(true);
        if(peerManager.savePeer(seedAI2P, true)) {
            seeds.add(seedAI2P);
        }
        periodicity = getPeriodicity();
    }

    @Override
    public Long getPeriodicity() {
        for(NetworkPeer sp : seeds) {
            // Do we have at least one reliable I2P Peer?
            if(sp.getNetwork()==Network.I2P && peerManager.isReliable(sp)) {
                return UpdateInterval * 1000L; // wait for UI seconds
            }
        }
        return 5 * 1000L; // Every five seconds until we have a reliable seed.
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
                        LOG.info("Seed not for I2P, skipping.");
                    } else if(seed.getDid().getPublicKey().getAddress().isEmpty()) {
                        LOG.warning("Seed provided does not have an address.");
                    } else if(seed.getDid().getPublicKey().getFingerprint().equals(localNode.getNetworkPeer(Network.I2P).getDid().getPublicKey().getFingerprint())) {
                        LOG.info("Seed is local peer.");
                    } else {
                        LOG.info("Sending Peer Status Request to Seed Peer:\n\t" + seed);
                        Envelope e = Envelope.documentFactory();
                        DLC.addRoute(NetworkService.class, PingRequestOp.class.getName(), e);
                        OpsPacket packet = new OpsPacket();
                        packet.atts.put(OpsPacket.OPS, OpsPacket.PING_REQUEST);
                        packet.atts.put(OpsPacket.FROM_ID, localNode.getNetworkPeer().getId());
                        packet.atts.put(OpsPacket.FROM_ADDRESS, localNode.getNetworkPeer().getDid().getPublicKey().getAddress());
                        packet.atts.put(OpsPacket.FROM_NFINGERPRINT, localNode.getNetworkPeer(Network.I2P).getDid().getPublicKey().getFingerprint());
                        packet.atts.put(OpsPacket.FROM_NADDRESS, localNode.getNetworkPeer(Network.I2P).getDid().getPublicKey().getAddress());
                        packet.atts.put(OpsPacket.TO_ID, seed.getId());
                        packet.atts.put(OpsPacket.TO_NFINGERPRINT, seed.getDid().getPublicKey().getFingerprint());
                        packet.atts.put(OpsPacket.TO_NADDRESS, seed.getDid().getPublicKey().getAddress());
                        if(sensor.sendOut(packet)) {
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
