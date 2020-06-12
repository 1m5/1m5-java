package io.onemfive.data;

/**
 * Indicates object is addressable on network by its public key.
 *
 */
public interface Addressable {
    String getFingerprint();
    void setFingerprint(String fingerprint);
    String getAddress();
    void setAddress(String address);
}
