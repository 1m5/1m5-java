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
package io.onemfive.core;

import io.onemfive.data.JSONSerializable;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

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
