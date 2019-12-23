package io.onemfive.data.route;

import io.onemfive.data.NetworkPeer;

public class SimpleExternalRoute extends SimpleRoute implements ExternalRoute {

    private NetworkPeer origination;
    private NetworkPeer destination;

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

}
