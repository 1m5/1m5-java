package io.onemfive.core;

import io.onemfive.network.Packet;

public interface Notification extends Operation {
    void notify(Packet packet);
}
