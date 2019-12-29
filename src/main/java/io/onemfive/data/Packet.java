package io.onemfive.data;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Packet extends ServiceMessage {

    public static int DESTINATION_PEER_REQUIRED = 1;
    public static int DESTINATION_PEER_WRONG_NETWORK = 2;
    public static int DESTINATION_PEER_NOT_FOUND = 3;
    public static int NO_ENVELOPE = 4;
    public static int NO_ROUTE = 5;
    public static int NO_SERVICE = 6;
    public static int NO_OPERATION = 7;
    public static int SENDING_FAILED = 8;

    private Logger LOG = Logger.getLogger(Packet.class.getName());

    private String id;
    protected Envelope envelope;

    protected NetworkPeer originationPeer;
    protected NetworkPeer fromPeer;
    protected NetworkPeer toPeer;
    protected NetworkPeer destinationPeer;

    public Packet() {}

    public String getId() {
        return id;
    }

    public Packet setId(String id) {
        this.id = id;
        return this;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public NetworkPeer getOriginationPeer() {
        return originationPeer;
    }

    public Packet setOriginationPeer(NetworkPeer originationPeer) {
        this.originationPeer = originationPeer;
        return this;
    }

    public NetworkPeer getFromPeer() {
        return fromPeer;
    }

    public Packet setFromPeer(NetworkPeer fromPeer) {
        this.fromPeer = fromPeer;
        return this;
    }

    public NetworkPeer getToPeer() {
        return toPeer;
    }

    public Packet setToPeer(NetworkPeer toPeer) {
        this.toPeer = toPeer;
        return this;
    }

    public NetworkPeer getDestinationPeer() {
        return destinationPeer;
    }

    public Packet setDestinationPeer(NetworkPeer destinationPeer) {
        this.destinationPeer = destinationPeer;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        if(id != null) m.put("id", String.valueOf(id));
        if(envelope != null) m.put("envelope", envelope.toMap());
        if(originationPeer != null) m.put("originationPeer", originationPeer.toMap());
        if(fromPeer != null) m.put("fromPeer", fromPeer.toMap());
        if(toPeer != null) m.put("toPeer", toPeer.toMap());
        if(destinationPeer != null) m.put("destinationPeer", destinationPeer.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("id") != null) id = (String)m.get("id");
        if(m.get("envelope") != null) {
            Map<String, Object> dm = (Map<String, Object>)m.get("envelope");
            envelope = new Envelope();
        }
        if(m.get("originationPeer") != null) {
            fromPeer = new NetworkPeer();
            fromPeer.fromMap((Map<String, Object>)m.get("originationPeer"));
        }
        if(m.get("fromPeer") != null) {
            fromPeer = new NetworkPeer();
            fromPeer.fromMap((Map<String, Object>)m.get("fromPeer"));
        }
        if(m.get("toPeer") != null) {
            toPeer = new NetworkPeer();
            toPeer.fromMap(((Map<String, Object>)m.get("toPeer")));
        }
        if(m.get("destinationPeer") != null) {
            fromPeer = new NetworkPeer();
            fromPeer.fromMap((Map<String, Object>)m.get("destinationPeer"));
        }
    }
}
