package io.onemfive.core;

import io.onemfive.network.Ack;
import io.onemfive.network.Response;

public interface AcknowledgedHandleResponse extends Operation {
    Ack operate(Response response);
}
