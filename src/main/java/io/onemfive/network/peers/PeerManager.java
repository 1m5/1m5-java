package io.onemfive.network.peers;

import io.onemfive.core.keyring.AuthNRequest;
import io.onemfive.data.DID;
import io.onemfive.data.NetworkPeer;
import io.onemfive.data.Packet;
import io.onemfive.data.Sensitivity;

import java.util.List;
import java.util.Properties;

public interface PeerManager extends Runnable {
    Boolean init(Properties properties);
    void updateLocalPeer(AuthNRequest request);
    void updateLocalPeer(DID did);
    NetworkPeer getLocalPeer();
    Boolean isKnown(NetworkPeer peer);

    /**
     * Is the provided toPeer reachable by the provided fromPeer?
     * This will return false if the Network Peer is unknown or
     * if known yet unable to reach by supplied Sensitivity level.
     * @param fromPeer
     * @param toPeer
     * @param sensitivity
     * @return
     */
    Boolean isReachable(NetworkPeer fromPeer, NetworkPeer toPeer, Sensitivity sensitivity);
    Boolean verifyPeer(NetworkPeer peer);
    Boolean savePeer(NetworkPeer peer, Boolean autocreate);
    NetworkPeer loadPeer(NetworkPeer peer);
    Boolean isRemoteLocal(NetworkPeer r);
    Packet buildPacket(DID origination, DID destination, Sensitivity sensitivity);
    List<NetworkPeer> getAllPeers(NetworkPeer fromPeer, int pageSize, int beginIndex);
    Long totalPeersByRelationship(NetworkPeer fromPeer, P2PRelationship.RelType relType);
    Long totalPeersByRelationshipAndSensitivity(NetworkPeer fromPeer, P2PRelationship.RelType relType, Sensitivity sensitivity);
    NetworkPeer getRandomPeer(NetworkPeer fromPeer);
    NetworkPeer getRandomReachablePeer(NetworkPeer fromPeer, Sensitivity sensitivity);
    List<NetworkPeer> getReliablesToShare(NetworkPeer fromPeer);
    void reliablesFromRemotePeer(NetworkPeer remotePeer, List<NetworkPeer> reliables);
    Boolean savePeerStatusTimes(NetworkPeer fromPeer, NetworkPeer toPeer, Long sent, Long acknowledged);
}
