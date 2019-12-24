package io.onemfive.ext.bitcoin.requests;

import io.onemfive.data.Request;

public class SendRequest extends Request {
    public String base58From;
    public String base58To;
    public long satoshis;
}
