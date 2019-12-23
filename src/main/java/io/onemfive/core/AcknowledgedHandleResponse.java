package io.onemfive.core;

import io.onemfive.data.Ack;
import io.onemfive.data.Response;

public interface AcknowledgedHandleResponse extends Operation {
    Ack operate(Response response);
}
