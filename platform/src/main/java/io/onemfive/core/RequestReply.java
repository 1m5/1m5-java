package io.onemfive.core;

import io.onemfive.network.Request;
import io.onemfive.network.Response;

/**
 * An Operation with a Request expecting a Response.
 */
public interface RequestReply extends Operation {
    Response operate(Request request);
}
