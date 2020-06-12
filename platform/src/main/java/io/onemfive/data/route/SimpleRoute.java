package io.onemfive.data.route;

import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.Map;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class SimpleRoute extends BaseRoute {

    public SimpleRoute() {}

    public SimpleRoute(String service, String operation) {
        super.service = service;
        super.operation = operation;
    }

    @Override
    public Map<String, Object> toMap() {
        return super.toMap();
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
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
