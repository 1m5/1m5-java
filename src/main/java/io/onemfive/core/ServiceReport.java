package io.onemfive.core;

import io.onemfive.data.JSONSerializable;

import java.util.HashMap;
import java.util.Map;

public class ServiceReport implements JSONSerializable {

    public String serviceClassName;
    public ServiceStatus serviceStatus;
    public Boolean registered = false;
    public Boolean running = false;
    public String version;

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(serviceClassName!=null) m.put("serviceClassName", serviceClassName);
        if(serviceStatus!=null) m.put("serviceStatus", serviceStatus.name());
        if(registered!=null) m.put("registered", registered);
        if(running!=null) m.put("running", running);
        if(version!=null) m.put("version", version);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m!=null) {
            if(m.get("serviceClassName")!=null) serviceClassName = (String)m.get("serviceClassName");
            if(m.get("serviceStatus")!=null) serviceStatus = ServiceStatus.valueOf((String)m.get("serviceStatus"));
            if(m.get("registered")!=null) registered = Boolean.parseBoolean((String)m.get("registered"));
            if(m.get("running")!=null) running = Boolean.parseBoolean((String)m.get("running"));
            if(m.get("version")!=null) version = (String)m.get("version");
        }
    }
}
