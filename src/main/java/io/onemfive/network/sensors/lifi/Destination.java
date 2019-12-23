package io.onemfive.network.sensors.lifi;

import io.onemfive.core.util.data.Base64;
import io.onemfive.data.Hash;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;

public class Destination extends NetworkPeer {

    private String address;
    private Hash hash;

    public Destination() {
        super(Network.LIFI.name(), null, null);
    }

    public String toBase64() {
        return Base64.encode(address);
    }

    public Hash getHash() {
        return hash;
    }

}
