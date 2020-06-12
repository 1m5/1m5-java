package io.onemfive.network.ops;

import io.onemfive.data.NetworkPeer;

import java.util.logging.Logger;

public class PingResponseOp extends NetworkResponseOp {

    private static final Logger LOG = Logger.getLogger(PingResponseOp.class.getName());

    public void operate() {
        if(sensorManager==null) {
            LOG.warning("SensorManager can not be null.");
        } else {
            NetworkPeer remote1M5Peer = new NetworkPeer();
            remote1M5Peer.setId(fromId);
            remote1M5Peer.getDid().getPublicKey().setFingerprint(fromId);
            remote1M5Peer.getDid().getPublicKey().setAddress(fromAddress);
            sensorManager.getPeerManager().savePeer(remote1M5Peer,true);
            NetworkPeer remoteNetworkPeer = new NetworkPeer(fromNetwork);
            remoteNetworkPeer.setId(fromId);
            remoteNetworkPeer.setPort(fromNetworkPort);
            remoteNetworkPeer.getDid().getPublicKey().setAddress(fromNetworkAddress);
            remoteNetworkPeer.getDid().getPublicKey().setFingerprint(fromNetworkFingerprint);
            sensorManager.getPeerManager().savePeer(remoteNetworkPeer, true);
        }
    }
}
