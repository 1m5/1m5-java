package io.onemfive.data.content;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class JSON extends Text {

    public JSON() {
        super(null, "application/json");
    }

    public JSON(byte[] body) {
        super(body, "application/json");
    }

    public JSON(byte[] body, String name, boolean generateHash, boolean generateFingerprint) {
        super(body, "application/json", name, generateHash, generateFingerprint);
    }
}
