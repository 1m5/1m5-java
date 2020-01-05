package io.onemfive.data.social;

import io.onemfive.data.DID;

import java.util.Map;

/**
 * An individual using the network.
 *
 * @author objectorange
 */
public class Individual extends Party {

    private DID did;

    public DID getDid() {
        return did;
    }

    public void setDid(DID did) {
        this.did = did;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = super.toMap();
        if(did!=null) {
            m.put("did", did.toMap());
        }
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m!=null) {
            if(m.get("did")!=null) {
                DID did = new DID();
                did.fromMap((Map<String,Object>)m.get("did"));
            }
        }
    }
}
