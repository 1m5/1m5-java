package io.onemfive.network.ops;

public abstract class NetworkResponseOp extends NetworkOp {
    public NetworkRequestOp requestOp;
    public abstract void operate();
}
