package io.onemfive.data;

import java.util.HashMap;
import java.util.Map;

/**
 * A node on the Network. It represents the local physical
 * machine on the 1M5 network and the local peers representing
 * that machine for each network supported.
 */
public final class NetworkNode {

    private Map<Network, NetworkPeer> localPeers;

    public NetworkNode() {
        localPeers = new HashMap<>();
        // Ensure 1M5 peer is available
        localPeers.put(Network.IMS, new NetworkPeer());
    }

    public void addNetworkPeer(NetworkPeer networkPeer) {
        localPeers.put(networkPeer.getNetwork(), networkPeer);
    }

    public NetworkPeer getNetworkPeer() {
        return localPeers.get(Network.IMS);
    }

    public NetworkPeer getNetworkPeer(Network network) {
        return localPeers.get(network);
    }

    public void removeNetworkPeer(NetworkPeer networkPeer) {
        localPeers.remove(networkPeer.getNetwork());
    }

    public int numberOfNetworkPeers() {
        return localPeers.size();
    }

    @Override
    public String toString() {
        String json = "{peers:[";
        boolean first = true;
        for(NetworkPeer p : localPeers.values()) {
            if(!first)
                json+=", ";
            json+=p.toJSON();
            first = false;
        }
        return json+"]}";
    }
}
