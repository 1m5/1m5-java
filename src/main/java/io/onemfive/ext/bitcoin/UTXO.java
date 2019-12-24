package io.onemfive.ext.bitcoin;

import io.onemfive.data.Hash;
import io.onemfive.data.JSONSerializable;

import java.util.Map;

public class UTXO implements JSONSerializable {

    private boolean coinbase;
    private String address;
    private Satoshi satoshi;
    private Hash hash;

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public void fromMap(Map<String, Object> map) {

    }
}
