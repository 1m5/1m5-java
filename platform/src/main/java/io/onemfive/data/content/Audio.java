package io.onemfive.data.content;

public class Audio extends Binary {

    public Audio() {}

    public Audio(byte[] body, String contentType) {
        super(body, contentType);
    }

    public Audio(byte[] body, String contentType, String name, boolean generateHash, boolean generateFingerprint) {
        super(body, contentType, name, generateHash, generateFingerprint);
    }
}
