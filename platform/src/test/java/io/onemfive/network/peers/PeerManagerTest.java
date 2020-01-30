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
package io.onemfive.network.peers;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

@Ignore
public class PeerManagerTest {

    private static Logger LOG = Logger.getLogger(PeerManagerTest.class.getName());

    @Test
    public void relationships() {
        /**
         * For example, Peer A wishes to send a message to Peer C at lowest latency path:
         *
         * Using only I2P:
         *
         *     Peer A to Peer C avg latency with I2P is 10 seconds
         *     Peer A to Peer B avg latency with I2P is 2 seconds
         *     Peer B to Peer C avg latency with I2P is 4 seconds
         *
         * In this case Peer A will use Peer B to get to Peer C with a likely latency result of 6 seconds.
         *
         * But if Tor was used:
         *
         *     Peer A to Peer C avg latency with Tor is 5 seconds
         *     Peer A to Peer B avg latency with Tor is 4 seconds
         *     Peer B to Peer C avg latency with Tor is 6 seconds
         *
         * In this case Peer A will send directly to Peer C with Tor at a likely latency of 5 seconds
         *
         * And Using Bluetooth:
         *
         *     Peer A to Peer C avg latency with Bluetooth is 1/2 second (they are physically next to each other)
         *     Peer A to Peer B avg latency with Bluetooth is 30 seconds (many hops)
         *     Peer B to Peer C avg latency with Bluetooth is 30 seconds
         *
         * Peer A easily sends directly to Peer C with Bluetooth at a likely latency of 1/2 second
         *
         * If we use all networks to determine, Bluetooth will be selected using path A -> C.
         *
         * If then Peer C turns off Bluetooth and all networks are evaluated, Tor will be selected A -> C (5 seconds).
         *
         * But say Peer C's Tor access gets blocked, then I2P with path A -> B -> C will be selected.
         *
         * But say Peer B shows up near Peer A and turns on their Bluetooth with a result in avg latency
         * with Bluetooth A -> B of 1/2 second, now the path to C will be A -> B using Bluetooth and B -> C using I2P
         * with an expected latency of 4.5 seconds.
         */
        Properties p = new Properties();
        p.setProperty("1m5.network.peers.dir","/home/objectorange/Projects/1m5/1m5/src/test/resources");
        PeerManager mgr = new PeerManager();
        mgr.init(p);

        // Node A
        NetworkPeer pA = mgr.localNode.getNetworkPeer();
        pA.setLocal(true);
        pA.getDid().setUsername("Alice");
        pA.getDid().getPublicKey().setAddress("1m5-A");
        mgr.savePeer(pA, true);
        NetworkPeer pAI2P = new NetworkPeer(Network.I2P);
        pAI2P.getDid().getPublicKey().setAddress("i2p-A");
        mgr.savePeer(pAI2P, true);
        NetworkPeer pATor = new NetworkPeer(Network.TOR);
        pATor.getDid().getPublicKey().setAddress("tor-A");
        mgr.savePeer(pATor, true);
        NetworkPeer pABT = new NetworkPeer(Network.Bluetooth);
        pABT.getDid().getPublicKey().setAddress("bt-A");
        mgr.savePeer(pABT, true);

        // Node B
        NetworkPeer pB = new NetworkPeer();
        pB.getDid().setUsername("Bob");
        pB.getDid().getPublicKey().setAddress("1m5-B");
        mgr.savePeer(pB, true);
        NetworkPeer pBI2P = new NetworkPeer(Network.I2P);
        pBI2P.getDid().getPublicKey().setAddress("i2p-B");
        mgr.savePeer(pBI2P, true);
        NetworkPeer pBTor = new NetworkPeer(Network.TOR);
        pBTor.getDid().getPublicKey().setAddress("tor-B");
        mgr.savePeer(pBTor, true);
        NetworkPeer pBBT = new NetworkPeer(Network.Bluetooth);
        pBBT.getDid().getPublicKey().setAddress("bt-B");
        mgr.savePeer(pBBT, true);

        // Node C
        NetworkPeer pC = new NetworkPeer();
        pC.getDid().setUsername("Charlie");
        pC.getDid().getPublicKey().setAddress("1m5-C");
        mgr.savePeer(pC, true);
        NetworkPeer pCI2P = new NetworkPeer(Network.I2P);
        pCI2P.getDid().getPublicKey().setAddress("i2p-C");
        mgr.savePeer(pCI2P, true);
        NetworkPeer pCTor = new NetworkPeer(Network.TOR);
        pCTor.getDid().getPublicKey().setAddress("tor-C");
        mgr.savePeer(pCTor, true);
        NetworkPeer pCBT = new NetworkPeer(Network.Bluetooth);
        pCBT.getDid().getPublicKey().setAddress("bt-C");
        mgr.savePeer(pCBT, true);

        long numPeers = mgr.totalPeersByRelationship(mgr.localNode.getNetworkPeer(), P2PRelationship.networkToRelationship(mgr.localNode.getNetworkPeer().getNetwork()));
        LOG.info("num peers: "+numPeers);

        long sent = 10 *60*1000;
        long ack;

        // I2P latencies
        // A -> C
        for(int i=0; i<2; i++) {
            ack = sent + 10000;
            mgr.savePeerStatusTimes(pAI2P, pCI2P, sent, ack);
            sent = ack;
        }
        // A -> B
        for(int i=0; i<2; i++) {
            ack = sent + 2000;
            mgr.savePeerStatusTimes(pAI2P, pBI2P, sent, ack);
            sent = ack;
        }
        // B -> C
        for(int i=0; i<2; i++) {
            ack = sent + 40000;
            mgr.savePeerStatusTimes(pBI2P, pCI2P, sent, ack);
            sent = ack;
        }

        LOG.info("Lowest Latency Path on I2P A -> C: ");
        List<NetworkPeer> llPath = mgr.findLowestLatencyPath(pAI2P, pCI2P);
        for(NetworkPeer np : llPath) {
            LOG.info(np.toString());
        }

        // Tor latencies
        // A -> C
        for(int i=0; i<2; i++) {
            ack = sent + 500;
            mgr.savePeerStatusTimes(pATor, pCTor, sent, ack);
            sent = ack;
        }
        // A -> B
        for(int i=0; i<2; i++) {
            ack = sent + 30000;
            mgr.savePeerStatusTimes(pATor, pBTor, sent, ack);
            sent = ack;
        }
        // B -> C
        for(int i=0; i<2; i++) {
            ack = sent + 30000;
            mgr.savePeerStatusTimes(pBTor, pCTor, sent, ack);
            sent = ack;
        }

        LOG.info("Lowest Latency Path on Tor A -> C: ");
        llPath = mgr.findLowestLatencyPath(pATor, pCTor);
        for(NetworkPeer np : llPath) {
            LOG.info(np.toString());
        }

        // Bluetooth latencies
        // A -> C
        for(int i=0; i<2; i++) {
            ack = sent + 5000;
            mgr.savePeerStatusTimes(pABT, pCBT, sent, ack);
            sent = ack;
        }
        // A -> B
        for(int i=0; i<2; i++) {
            ack = sent + 4000;
            mgr.savePeerStatusTimes(pABT, pBBT, sent, ack);
            sent = ack;
        }
        // B -> C
        for(int i=0; i<2; i++) {
            ack = sent + 6000;
            mgr.savePeerStatusTimes(pBBT, pCBT, sent, ack);
            sent = ack;
        }

        LOG.info("Lowest Latency Path on BT A -> C: ");
        llPath = mgr.findLowestLatencyPath(pABT, pCBT);
        for(NetworkPeer np : llPath) {
            LOG.info(np.toString());
        }

//        LOG.info("Lowest Latency Path on Tor Escalation Networks A -> C: ");
//        llPath = mgr.findLowestLatencyPathFiltered(pA, pC, (Network[]) SensorManager.torSensorEscalation.toArray());
//        for(NetworkPeer np : llPath) {
//            LOG.info(np.toString());
//        }
//
//        LOG.info("Lowest Latency Path on I2P Escalation Networks A -> C: ");
//        llPath = mgr.findLowestLatencyPathFiltered(pA, pC, (Network[])SensorManager.i2pSensorEscalation.toArray());
//        for(NetworkPeer np : llPath) {
//            LOG.info(np.toString());
//        }

//        LOG.info("Lowest Latency Path on 1DN Escalation Networks A -> C: ");
//        llPath = mgr.findLowestLatencyPathFiltered(pA, pC, (Network[])SensorManager.idnSensorEscalation.toArray());
//        for(NetworkPeer np : llPath) {
//            LOG.info(np.toString());
//        }
//
//        LOG.info("Lowest Latency Path with only Tor and I2P A -> C: ");
//        llPath = mgr.findLowestLatencyPathSpecifiedNetworks(pA, pC, Arrays.asList(Network.TOR, Network.I2P));
//        for(NetworkPeer np : llPath) {
//            LOG.info(np.toString());
//        }
    }
}
