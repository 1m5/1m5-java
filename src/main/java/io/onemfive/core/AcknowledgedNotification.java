package io.onemfive.core;

import io.onemfive.data.Ack;
import io.onemfive.data.Packet;

public interface AcknowledgedNotification extends Operation {
    Ack operate(Packet packet);
}
