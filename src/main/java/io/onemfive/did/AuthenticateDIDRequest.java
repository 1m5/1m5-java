package io.onemfive.did;

import io.onemfive.data.ServiceMessage;
import io.onemfive.data.DID;

import java.util.Map;

public class AuthenticateDIDRequest extends ServiceMessage {
    public static final int DID_REQUIRED = 1;
    public static final int DID_USERNAME_REQUIRED = 2;
    public static final int DID_PASSPHRASE_REQUIRED = 3;
    public static final int DID_USERNAME_UNKNOWN = 4;
    public static final int DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN = 5;
    public static final int DID_PASSPHRASE_HASH_ALGORITHM_MISMATCH = 6;
    public static final int DID_PASSPHRASE_MISMATCH = 7;
    public static final int DID_TOKEN_FORMAT_MISMATCH = 8;

    public boolean autogenerate = false;
    public DID did;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = super.toMap();
        if(did!=null) m.put("did",did.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m.get("did")!=null) {
            did = new DID();
            did.fromMap((Map<String, Object>)m.get("did"));
        }
    }
}
