package io.onemfive.network.peers.db;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkNode;
import io.onemfive.data.NetworkPeer;
import io.onemfive.util.JSONParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public interface PeerDB {
    Boolean savePeer(NetworkPeer p, Boolean autocreate);

    int numberPeersByNetwork(Network network);

    NetworkPeer randomPeer(NetworkPeer fromPeer);

    NetworkNode loadNode(String id);

    NetworkPeer loadPeerById(String id);

    NetworkPeer loadPeerByIdAndNetwork(String id, Network network);

    NetworkPeer loadPeerByAddress(String address);

    default NetworkPeer toPeer(ResultSet rs) throws SQLException {
        NetworkPeer p = new NetworkPeer();
        p.setId(rs.getString(NetworkPeer.ID));
        p.setNetwork(Network.valueOf(rs.getString(NetworkPeer.NETWORK)));
        p.getDid().setUsername(rs.getString(NetworkPeer.USERNAME));
        p.getDid().getPublicKey().setAlias(rs.getString(NetworkPeer.ALIAS));
        p.getDid().getPublicKey().setAddress(rs.getString(NetworkPeer.ADDRESS));
        p.getDid().getPublicKey().setFingerprint(rs.getString(NetworkPeer.FINGERPRINT));
        p.getDid().getPublicKey().setType(rs.getString(NetworkPeer.KEY_TYPE));
        p.setPort(rs.getInt(NetworkPeer.PORT));
        p.getDid().getPublicKey().setAttributes((Map<String,Object>) JSONParser.parse(rs.getString(NetworkPeer.ATTRIBUTES)));
        return p;
    }

    String getLocation();

    void setLocation(String location);

    String getName();

    void setName(String name);

    boolean init(Properties p);

    boolean teardown();
}
