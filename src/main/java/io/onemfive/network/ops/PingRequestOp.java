package io.onemfive.network.ops;

import io.onemfive.core.RequestReply;
import io.onemfive.data.Request;
import io.onemfive.data.Response;

/**
 * Handle a ping request
 */
public class PingRequestOp implements RequestReply {

    @Override
    public Response operate(Request request) {
        Response response = new Response(request.getId());

        return response;
    }
}
