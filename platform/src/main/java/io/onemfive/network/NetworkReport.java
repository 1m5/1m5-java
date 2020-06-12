package io.onemfive.network;

import io.onemfive.data.JSONSerializable;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.Map;

/**
 * Information regarding the status and health of access
 * to the 1M5 Network.
 */
public class NetworkReport implements JSONSerializable {



    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public void fromMap(Map<String, Object> m) {

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
