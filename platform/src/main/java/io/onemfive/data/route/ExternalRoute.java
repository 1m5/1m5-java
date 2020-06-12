package io.onemfive.data.route;

import io.onemfive.data.NetworkPeer;

public interface ExternalRoute extends Route {
    NetworkPeer getOrigination();
    NetworkPeer getDestination();
}
