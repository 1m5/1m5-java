package io.onemfive.core;

import io.onemfive.data.Request;
import io.onemfive.data.Response;

public interface RequestReply extends Operation {
    Response operate(Request request);
}
