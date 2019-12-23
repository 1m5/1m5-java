package io.onemfive.core;

import io.onemfive.data.Response;

public interface HandleResponse extends Operation {
    Boolean operate(Response response);
}
