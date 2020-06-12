package io.onemfive.data;

/**
 * TODO: Add Description
 *
 */
public abstract class KeyRingsRequest extends ServiceMessage {
    public static int KEY_RING_IMPLEMENTATION_UNKNOWN = 1;

    public String keyRingImplementation = "io.onemfive.core.keyring.OpenPGPKeyRing"; // default
}
