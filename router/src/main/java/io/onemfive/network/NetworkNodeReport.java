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
package io.onemfive.network;

import io.onemfive.core.ServiceReport;
import io.onemfive.data.JSONSerializable;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Information on the status of a Network Peer,
 * its Services, and its access to the 1M5 Network.
 */
public class NetworkNodeReport implements JSONSerializable {

    public List<ServiceReport> serviceReports;
    public NetworkReport networkReport;

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(serviceReports!=null) {
            List<Map<String,Object>> lm = new ArrayList<>();
            m.put("serviceReports", lm);
            for(ServiceReport sr : serviceReports) {
                lm.add(sr.toMap());
            }
        }
        if(networkReport!=null) m.put("networkReport", networkReport.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("serviceReports")!=null) {
            serviceReports = new ArrayList<>();
            List<Map<String,Object>> lm = (List<Map<String,Object>>)m.get("serviceReports");
            ServiceReport sr;
            for(Map<String,Object> sm : lm) {
                sr = new ServiceReport();
                sr.fromMap(sm);
                serviceReports.add(sr);
            }
        }
        if(m.get("networkReport")!=null) {
            networkReport = new NetworkReport();
            networkReport.fromMap((Map<String,Object>)m.get("networkReport"));
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
