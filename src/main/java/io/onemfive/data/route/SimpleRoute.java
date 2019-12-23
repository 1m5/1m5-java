package io.onemfive.data.route;

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
}
