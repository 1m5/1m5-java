package io.onemfive.data.content;

public class HTML extends Text {

    public HTML() {
        super(null, "text/html");
    }

    public HTML(byte[] body) {
        super(body, "text/html");
    }

    public HTML(byte[] body, String name, boolean generateHash, boolean generateFingerprint) {
        super(body, "text/html", name, generateHash, generateFingerprint);
    }
}
