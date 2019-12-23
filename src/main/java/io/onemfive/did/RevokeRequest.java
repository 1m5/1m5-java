package io.onemfive.did;

import io.onemfive.data.ServiceMessage;
import io.onemfive.data.DID;

/**
 * Revoke Identity.
 *
 * @author objectorange
 */
public class RevokeRequest extends ServiceMessage {

    public static final int DID_REQUIRED = 1;

    public DID did;
}
