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
