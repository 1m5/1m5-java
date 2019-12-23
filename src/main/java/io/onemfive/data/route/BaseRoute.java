package io.onemfive.data.route;

import io.onemfive.data.route.Route;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public abstract class BaseRoute implements Route {

//    protected Envelope envelope;
    protected String service;
    protected String operation;
    protected Boolean routed = false;
    protected Long routeId = new Random(System.currentTimeMillis()).nextLong();

//    @Override
//    public Route setEnvelope(Envelope envelope) {
//        this.envelope = envelope;
//    }


    @Override
    public String getService() {
        return service;
    }

    public Route setService(String service) {
        this.service = service;
        return this;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    public Route setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public Boolean getRouted() {
        return routed;
    }

    public Route setRouted(Boolean routed) {
        this.routed = routed;
        return this;
    }

    public Long getRouteId() {
        return routeId;
    }

    public Route setRouteId(Long routeId) {
        this.routeId = routeId;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(service!=null) m.put("service", service);
        if(operation!=null) m.put("operation", operation);
        if(routed!=null) m.put("routed", routed);
        if(routeId!=null) m.put("routeId", routeId);
        m.put("type", this.getClass().getName());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("service")!=null) service = (String)m.get("service");
        if(m.get("operation")!=null) operation = (String)m.get("operation");
        if(m.get("routed")!=null) routed = Boolean.parseBoolean((String)m.get("routed"));
        if(m.get("routedId")!=null) routeId = Long.parseLong((String)m.get("routeId"));
    }
}
