package io.onemfive.network.ops;

public abstract class NetworkRequestOp extends NetworkOp {

    public NetworkResponseOp responseOp;

    public abstract NetworkResponseOp operate();
}
