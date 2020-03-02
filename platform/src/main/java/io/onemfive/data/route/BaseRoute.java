/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.data.route;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public abstract class BaseRoute implements Route {

    protected String service;
    protected String operation;
    protected Boolean routed = false;
    protected Long routeId = new Random(System.currentTimeMillis()).nextLong();

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
        if(m.get("routed")!=null) routed = (Boolean)m.get("routed");
        if(m.get("routedId")!=null) routeId = (Long)m.get("routeId");
    }
}
