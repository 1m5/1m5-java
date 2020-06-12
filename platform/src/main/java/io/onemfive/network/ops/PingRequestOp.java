package io.onemfive.network.ops;

import io.onemfive.data.NetworkPeer;

import java.util.logging.Logger;

/**
 * Handle a ping request
 */
public class PingRequestOp extends NetworkRequestOp {

    private static final Logger LOG = Logger.getLogger(PingResponseOp.class.getName());

    @Override
    public NetworkResponseOp operate() {
        if(sensorManager==null) {
            LOG.warning("SensorManager must be set.");
            return null;
        }
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

        NetworkPeer localPeer = sensorManager.getPeerManager().getLocalNode().getNetworkPeer();
        NetworkPeer localNetworkPeer = sensorManager.getPeerManager().getLocalNode().getNetworkPeer(fromNetwork);

        if(localPeer!=null && localNetworkPeer!=null) {
            PingResponseOp responseOp = new PingResponseOp();
            responseOp.id = id;
            responseOp.fromId = localPeer.getId();
            responseOp.fromAddress = localPeer.getDid().getPublicKey().getAddress();
            responseOp.fromNetwork = fromNetwork;
            responseOp.fromNetworkFingerprint = localNetworkPeer.getDid().getPublicKey().getFingerprint();
            responseOp.fromNetworkAddress = localNetworkPeer.getDid().getPublicKey().getAddress();
            responseOp.fromNetworkPort = localNetworkPeer.getPort();
            this.responseOp = responseOp;
            return responseOp;
        } else {
            LOG.warning("Local Peer and/or Local Network Peer not set for network: "+fromNetwork.name());
        }
        return null;
    }
}
