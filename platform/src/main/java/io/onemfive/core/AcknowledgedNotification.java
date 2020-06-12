package io.onemfive.core;

import io.onemfive.network.Ack;
import io.onemfive.network.NetworkPacket;

public interface AcknowledgedNotification extends Operation {
    Ack operate(NetworkPacket packet);
}
