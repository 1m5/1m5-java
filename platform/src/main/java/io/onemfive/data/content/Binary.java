package io.onemfive.data.content;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public abstract class Binary extends Content {
    public Binary() {
    }

    public Binary(byte[] body) {
        super.body = body;
    }

    public Binary(byte[] body, String contentType) {
        super(body, contentType);
    }

    public Binary(byte[] body, String contentType, String name, boolean generateHash, boolean generateFingerprint) {
        super(body, contentType, name, generateHash, generateFingerprint);
    }
}
