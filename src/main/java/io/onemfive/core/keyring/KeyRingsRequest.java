package io.onemfive.core.keyring;

import io.onemfive.data.ServiceMessage;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public abstract class KeyRingsRequest extends ServiceMessage {
    public static int KEY_RING_IMPLEMENTATION_UNKNOWN = 1;

    public String keyRingImplementation = OpenPGPKeyRing.class.getName(); // default
}
