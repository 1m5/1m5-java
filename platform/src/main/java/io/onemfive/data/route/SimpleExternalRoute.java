package io.onemfive.data.route;

import io.onemfive.data.NetworkPeer;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.Map;

public class SimpleExternalRoute extends SimpleRoute implements ExternalRoute {

    private NetworkPeer origination;
    private NetworkPeer destination;

    public SimpleExternalRoute() {}

    public SimpleExternalRoute(String service, String operation) {
        super(service, operation);
    }

    public SimpleExternalRoute(String service, String operation, NetworkPeer origination, NetworkPeer destination) {
        super(service, operation);
        this.origination = origination;
        this.destination = destination;
    }

    @Override
    public NetworkPeer getOrigination() {
        return origination;
    }

    public Route setOrigination(NetworkPeer origination) {
        this.origination = origination;
        return this;
    }

    @Override
    public NetworkPeer getDestination() {
        return destination;
    }

    public Route setDestination(NetworkPeer destination) {
        this.destination = destination;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = super.toMap();
        if(origination!=null) m.put("origination",origination.toMap());
        if(destination!=null) m.put("destination",destination.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m.get("origination")!=null) {
            origination = new NetworkPeer();
            origination.fromMap((Map<String,Object>)m.get("origination"));
        }
        if(m.get("destination")!=null) {
            destination = new NetworkPeer();
            destination.fromMap((Map<String,Object>)m.get("destination"));
        }
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
