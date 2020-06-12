package io.onemfive.monetary.btc.requests;

import io.onemfive.network.Request;

public class SendRequest extends Request {
    public String base58From;
    public String base58To;
    public long satoshis;
}
