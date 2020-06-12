package io.onemfive.network;

import io.onemfive.core.ServiceStatus;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkState {

    public Network network = Network.IMS; // Default
    public NetworkPeer localPeer;
    public SensorStatus sensorStatus;
    public ServiceStatus serviceStatus;
    public Integer virtualPort;
    public Integer targetPort;
    // Seeds
    public final List<NetworkPeer> seeds = new ArrayList<>();
    // Banned
    public final List<NetworkPeer> banned = new ArrayList<>();
    // Min Peers Tracked - the point at which Discovery process goes into 'hyper' mode.
    public int MinPT = 10;
    // Max Peers Tracked - the total number of Peers to attempt to maintain knowledge of
    public int MaxPT = 100;
    // Max Peers Sent - Maximum number of peers to send in a peer list (the bigger a datagram, the less chance of it getting through).
    public int MaxPS = 5;
    // Max Acknowledgments Tracked
    public int MaxAT = 20;
    // Update Interval - seconds between Discovery process
    public int UpdateInterval = 60;
    // Update Interval Hyper - seconds between Discovery process when no reliable peers are known
    public int UpdateIntervalHyper = 5;
    // Reliable Peer Min Acks
    public int MinAckRP = 20;
    // Super Reliable Peer Min Acks
    public int MinAckSRP = 10000;

    public Map<String,Object> params = new HashMap<>();

    @Override
    public String toString() {
        Map<String,Object> m = new HashMap<>();
        if(network!=null) m.put("network", network.name());
        if(sensorStatus!=null) m.put("sensorStatus", sensorStatus.name());
        if(serviceStatus!=null) m.put("serviceStatus", serviceStatus.name());
        if(virtualPort!=null) m.put("virtualPort", virtualPort);
        if(targetPort!=null) m.put("targetPort", targetPort);
        if(localPeer!=null && localPeer.getDid().getUsername()!=null) m.put("username", localPeer.getDid().getUsername());
        if(localPeer!=null) m.put("fingerprint", localPeer.getDid().getPublicKey().getFingerprint());
        if(localPeer!=null) m.put("address", localPeer.getDid().getPublicKey().getAddress());
        if(params!=null) m.put("params", params);
        return JSONPretty.toPretty(JSONParser.toString(m), 4);
    }
}
