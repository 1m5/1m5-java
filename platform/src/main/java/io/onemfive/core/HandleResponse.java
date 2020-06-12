package io.onemfive.core;

import io.onemfive.network.Response;

public interface HandleResponse extends Operation {
    Boolean operate(Response response);
}
