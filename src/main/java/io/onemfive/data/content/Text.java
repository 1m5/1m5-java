package io.onemfive.data.content;

public class Text extends Content {

    public Text() {
        super(null, "text/plain");
    }

    public Text(byte[] body){
        super(body, "text/plain");
    }

    protected Text(byte[] body, String contentType){
        super(body, contentType);
    }

    public Text(byte[] body, String name, boolean generateHash, boolean generateFingerprint) {
        super(body, "text/plain", name, generateHash, generateFingerprint);
    }

    protected Text(byte[] body, String contentType, String name, boolean generateHash, boolean generateFingerprint) {
        super(body, contentType, name, generateHash, generateFingerprint);
    }
}
