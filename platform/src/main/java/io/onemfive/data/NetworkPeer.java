package io.onemfive.data;

import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

;

/**
 * A Peer on the 1M5 peer-to-peer network identified by DID and Network and
 * whether or not it is local.
 */
public final class NetworkPeer implements JSONSerializable {

    private static Logger LOG = Logger.getLogger(NetworkPeer.class.getName());

    public static final String ID = "id";
    public static final String LOCAL = "local";
    public static final String NETWORK = "network";
    public static final String DID = "did";
    public static final String USERNAME = "username";
    public static final String PASSPHRASE_HASH = "passphraseHash";
    public static final String PASSPHRASE_HASH_ALG = "passphraseHashAlg";
    public static final String ALIAS = "alias";
    public static final String ADDRESS = "address";
    public static final String FINGERPRINT = "fingerprint";
    public static final String KEY_TYPE = "keyType";
    public static final String ATTRIBUTES = "attributes";
    public static final String PORT = "port";

    private String id;
    private Network network;
    private DID did;
    private Integer port;

    public NetworkPeer() {
        this(Network.IMS, null, null);
    }

    public NetworkPeer(Network network) {
        this(network, null, null);
    }

    public NetworkPeer(String username, String passphrase) {
        this(Network.IMS, username, passphrase);
    }

    public NetworkPeer(Network network, String username, String passphrase) {
        this.network = network;
        did = new DID();
        did.setUsername(username);
        did.setPassphrase(passphrase);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public DID getDid() {
        return did;
    }

    public void setDid(DID did) {
        this.did = did;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(id!=null)
            m.put(ID, id);
        if(network!=null)
            m.put(NETWORK, network.name());
        if(did!=null)
            m.put(DID,did.toMap());
        if(port!=null)
            m.put(PORT, port);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get(ID)!=null) id = (String)m.get(ID);
        if(m.get(NETWORK)!=null) network = Network.valueOf((String)m.get(NETWORK));
        if(m.get(DID)!=null) {
            did = new DID();
            if(m.get(DID) instanceof String)
                did.fromMap((Map<String,Object>) JSONParser.parse((String)m.get(DID)));
            else
                did.fromMap((Map<String,Object>)m.get(DID));
        }
        if(m.get(PORT)!=null) port = (Integer)m.get(PORT);
    }

    @Override
    public String toJSON() {
        return JSONPretty.toPretty(JSONParser.toString(toMap()), 4);
    }

    @Override
    public void fromJSON(String json) {
        fromMap((Map<String,Object>)JSONParser.parse(json));
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
