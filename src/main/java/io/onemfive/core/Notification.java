package io.onemfive.core;

import io.onemfive.data.Packet;

public interface Notification extends Operation {
    void notify(Packet packet);
}
