package io.onemfive.data.content;

import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.Map;

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

    @Override
    public String toJSON() {
        return JSONPretty.toPretty(JSONParser.toString(toMap()), 4);
    }

    @Override
    public void fromJSON(String json) {
        fromMap((Map<String,Object>)JSONParser.parse(json));
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
