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

import io.onemfive.data.AuthNRequest;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.data.PublicKey;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class PeerManagerTest {

    private static Logger LOG = Logger.getLogger(PeerManagerTest.class.getName());

    private Properties properties = new Properties();
    private PeerManager mgr = new PeerManager();

    @Before
    public void setup() {
        properties.setProperty("1m5.network.peers.dir","/home/objectorange/Projects/1m5/1m5/platform/src/test/resources");
        properties.setProperty("1m5.peers.db.cleanOnRestart","true");
        mgr.init(properties);
        mgr.getLocalNode().getNetworkPeer().setId("1234");
        AuthNRequest request = new AuthNRequest();
        request.identityPublicKey = new PublicKey("ABCDEFG");
        request.alias = "Alice";
        request.aliasPassphrase = "1234";
        mgr.updateLocalAuthNPeer(request);
    }

    @Ignore
    @Test
    public void saveAndLoad() {
        NetworkPeer np = new NetworkPeer();
        np.setId("ABCDEFG");
        np.getDid().setUsername("Bob");
        np.getDid().setPassphrase("5678");
        np.getDid().getPublicKey().setAddress("12345678");
        np.getDid().getPublicKey().setFingerprint("123456");
        mgr.savePeer(np, true);

        NetworkPeer npLoaded = mgr.loadPeer("ABCDEFG");
        assert npLoaded != null && "123456".equals(npLoaded.getDid().getPublicKey().getFingerprint());
    }

    @Ignore
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

        // Node A

        NetworkPeer pA = new NetworkPeer();
        pA.setId("pA");
        pA.getDid().setUsername("Alice");
        pA.getDid().getPublicKey().setAddress("Alice-1m5-Address");
        pA.getDid().getPublicKey().setFingerprint("Alice-1m5-Fingerprint");
        mgr.updateLocalNode(pA);
        NetworkPeer pAI2P = new NetworkPeer(Network.I2P);
        pAI2P.setId(pA.getId());
        pAI2P.getDid().getPublicKey().setAddress("Alice-i2p-Address");
        pAI2P.getDid().getPublicKey().setFingerprint("Alice-i2p-Fingerprint");
        mgr.updateLocalNode(pAI2P);
        NetworkPeer pATor = new NetworkPeer(Network.TOR);
        pATor.setId(pA.getId());
        pATor.getDid().getPublicKey().setAddress("Alice-tor-Address");
        pATor.getDid().getPublicKey().setFingerprint("Alice-tor-Fingerprint");
        mgr.updateLocalNode(pATor);
        NetworkPeer pABT = new NetworkPeer(Network.Bluetooth);
        pABT.setId(pA.getId());
        pABT.getDid().getPublicKey().setAddress("Alice-bt-Address");
        pABT.getDid().getPublicKey().setFingerprint("Alice-bt-Fingerprint");
        mgr.updateLocalNode(pABT);

        // Node B
        NetworkPeer pB = new NetworkPeer();
        pB.setId("pB");
        pB.getDid().setUsername("Bob");
        pB.getDid().getPublicKey().setAddress("Bob-1m5-Address");
        pB.getDid().getPublicKey().setFingerprint("Bob-1m5-Fingeprint");
        mgr.savePeer(pB, true);
        NetworkPeer pBI2P = new NetworkPeer(Network.I2P);
        pBI2P.setId(pB.getId());
        pBI2P.getDid().getPublicKey().setAddress("Bob-i2p-Address");
        pBI2P.getDid().getPublicKey().setFingerprint("Bob-i2p-Fingerprint");
        mgr.savePeer(pBI2P, true);
        NetworkPeer pBTor = new NetworkPeer(Network.TOR);
        pBTor.setId(pB.getId());
        pBTor.getDid().getPublicKey().setAddress("Bob-tor-Address");
        pBTor.getDid().getPublicKey().setFingerprint("Bob-tor-Fingerprint");
        mgr.savePeer(pBTor, true);
        NetworkPeer pBBT = new NetworkPeer(Network.Bluetooth);
        pBBT.setId(pB.getId());
        pBBT.getDid().getPublicKey().setAddress("Bob-bt-Address");
        pBBT.getDid().getPublicKey().setFingerprint("Bob-bt-Fingerprint");
        mgr.savePeer(pBBT, true);

        // Node C
        NetworkPeer pC = new NetworkPeer();
        pC.setId("pC");
        pC.getDid().setUsername("Charlie");
        pC.getDid().getPublicKey().setAddress("Charlie-1m5-Address");
        pC.getDid().getPublicKey().setFingerprint("Charlie-1m5-Fingerprint");
        mgr.savePeer(pC, true);
        NetworkPeer pCI2P = new NetworkPeer(Network.I2P);
        pCI2P.setId(pC.getId());
        pCI2P.getDid().getPublicKey().setAddress("Charlie-i2p-Address");
        pCI2P.getDid().getPublicKey().setFingerprint("Charlie-i2p-Fingerprint");
        mgr.savePeer(pCI2P, true);
        NetworkPeer pCTor = new NetworkPeer(Network.TOR);
        pCTor.setId(pC.getId());
        pCTor.getDid().getPublicKey().setAddress("Charlie-tor-Address");
        pCTor.getDid().getPublicKey().setFingerprint("Charlie-tor-Fingerprint");
        mgr.savePeer(pCTor, true);
        NetworkPeer pCBT = new NetworkPeer(Network.Bluetooth);
        pCBT.setId(pC.getId());
        pCBT.getDid().getPublicKey().setAddress("Charlie-bt-Address");
        pCBT.getDid().getPublicKey().setFingerprint("Charlie-bt-Fingerprint");
        mgr.savePeer(pCBT, true);

        long sent = 10 *60*1000;
        long ack;

        // I2P latencies
        // A -> C
        for(int i=0; i<2; i++) {
            ack = sent + 10000;
            mgr.savePeerStatusTimes(pAI2P.getId(), Network.I2P, pCI2P.getId(), sent, ack);
            sent = ack;
        }
        // A -> B
        for(int i=0; i<2; i++) {
            ack = sent + 2000;
            mgr.savePeerStatusTimes(pAI2P.getId(), Network.I2P, pBI2P.getId(), sent, ack);
            sent = ack;
        }
        // B -> C
        for(int i=0; i<2; i++) {
            ack = sent + 40000;
            mgr.savePeerStatusTimes(pBI2P.getId(), Network.I2P, pCI2P.getId(), sent, ack);
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
            mgr.savePeerStatusTimes(pATor.getId(), Network.TOR, pCTor.getId(), sent, ack);
            sent = ack;
        }
        // A -> B
        for(int i=0; i<2; i++) {
            ack = sent + 30000;
            mgr.savePeerStatusTimes(pATor.getId(), Network.TOR, pBTor.getId(), sent, ack);
            sent = ack;
        }
        // B -> C
        for(int i=0; i<2; i++) {
            ack = sent + 30000;
            mgr.savePeerStatusTimes(pBTor.getId(), Network.TOR, pCTor.getId(), sent, ack);
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
            mgr.savePeerStatusTimes(pABT.getId(), Network.Bluetooth, pCBT.getId(), sent, ack);
            sent = ack;
        }
        // A -> B
        for(int i=0; i<2; i++) {
            ack = sent + 4000;
            mgr.savePeerStatusTimes(pABT.getId(), Network.Bluetooth, pBBT.getId(), sent, ack);
            sent = ack;
        }
        // B -> C
        for(int i=0; i<2; i++) {
            ack = sent + 6000;
            mgr.savePeerStatusTimes(pBBT.getId(), Network.Bluetooth, pCBT.getId(), sent, ack);
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
