package io.onemfive.network.sensors.tor;

import io.onemfive.data.*;

/**
 * A peer on the Tor overlay network.
 */
public class TorPeer extends NetworkPeer implements Addressable, JSONSerializable {

    public TorPeer() {
        this(null, null);
    }

    public TorPeer(String username, String passphrase) {
        super(Network.SDR.name(), username, passphrase);
    }

    public TorPeer(NetworkPeer peer) {
        fromMap(peer.toMap());
    }

    @Override
    public Object clone() {
        TorPeer clone = new TorPeer();
        clone.did = (DID)did.clone();
        clone.network = network;
        return clone;
    }
}
