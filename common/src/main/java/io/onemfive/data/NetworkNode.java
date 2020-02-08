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
