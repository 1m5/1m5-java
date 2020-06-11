package io.onemfive.network.peers.graph;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.peers.graph.neo4j.P2PRelationship;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.List;
import java.util.Properties;

public interface PeerGraph {
    GraphDatabaseService getGraphDb();

    List<NetworkPeer> findLeastHopsPath(String fromPeerId, String toPeerId);

    List<NetworkPeer> findLowestLatencyPath(String fromPeerId, String toPeerId);

    List<NetworkPeer> findLowestLatencyPathFiltered(String fromPeerId, String toPeerId, Network[] networks);

    boolean savePeer(NetworkPeer networkPeer, Boolean autoCreate);

    boolean isRelatedByNetwork(String startPeerId, Network network, String endPeerId);

    P2PRelationship relateByNetwork(String idLeftPeer, Network network, String idRightPeer);

    P2PRelationship getNetworkRelationship(String idLeftPeer, Network network, String idRightPeer);

    long numberPeersByNetwork(String id, Network network);

    boolean removeNetworkRelationship(String startPeerId, Network network, String endPeerId);

    Boolean savePeerStatusTimes(String startPeerId, Network network, String endPeerId, Long timeSent, Long timeAcknowledged);

    String getLocation();

    void setLocation(String location);

    String getName();

    void setName(String name);

    boolean init(Properties properties);

    boolean teardown();
}
