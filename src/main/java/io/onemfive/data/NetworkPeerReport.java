package io.onemfive.data;

import io.onemfive.core.ServiceReport;
import io.onemfive.network.NetworkReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkPeerReport implements JSONSerializable {

    public List<ServiceReport> serviceReports;
    public NetworkReport networkReport;

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(serviceReports!=null) m.put("serviceReports", serviceReports);
        if(networkReport!=null) m.put("networkReport", networkReport.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("serviceReports")!=null) {
            serviceReports = (List<ServiceReport>)m.get("serviceReports");
        }
        if(m.get("networkReport")!=null) {
            networkReport = new NetworkReport();
            networkReport.fromMap((Map<String,Object>)m.get("networkReport"));
        }
    }
}
