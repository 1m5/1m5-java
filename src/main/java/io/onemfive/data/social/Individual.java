package io.onemfive.data.social;

import io.onemfive.data.DID;
import io.onemfive.data.JSONSerializable;

import java.util.Map;

/**
 * An individual using the network.
 *
 * @author objectorange
 */
public class Individual extends Party implements JSONSerializable {

    private DID did;

    public DID getDid() {
        return did;
    }

    public void setDid(DID did) {
        this.did = did;
    }

    @Override
    public Map<String, Object> toMap() {
        return did.toMap();
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        did = new DID();
        did.fromMap(m);
    }
}
