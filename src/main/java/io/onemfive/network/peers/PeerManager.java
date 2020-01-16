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

import io.onemfive.core.keyring.AuthNRequest;
import io.onemfive.data.*;
import io.onemfive.network.Network;
import io.onemfive.network.NetworkPeer;
import io.onemfive.network.Packet;

import java.util.List;
import java.util.Properties;

public interface PeerManager extends Runnable {
    Boolean init(Properties properties);
    void updateLocalAuthNPeer(AuthNRequest request);
    void updateLocalPeer(NetworkPeer networkPeer);
    NetworkPeer getLocalPeer(Network network);
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
    Packet buildPacket(DID origination, DID destination);
    Packet buildPacket(NetworkPeer origination, NetworkPeer destination);
    List<NetworkPeer> getAllPeers(NetworkPeer fromPeer, int pageSize, int beginIndex);
    Long totalPeersByRelationship(NetworkPeer fromPeer, P2PRelationship.RelType relType);
    Long totalPeersByRelationshipAndSensitivity(NetworkPeer fromPeer, P2PRelationship.RelType relType, Sensitivity sensitivity);
    NetworkPeer getRandomPeer(NetworkPeer fromPeer);
    NetworkPeer getRandomReachablePeer(NetworkPeer fromPeer, Sensitivity sensitivity);
    List<NetworkPeer> getReliablesToShare(NetworkPeer fromPeer);
    void reliablesFromRemotePeer(NetworkPeer remotePeer, List<NetworkPeer> reliables);
    Boolean savePeerStatusTimes(NetworkPeer fromPeer, NetworkPeer toPeer, Long sent, Long acknowledged, Network network);
}
