package io.onemfive.data.route;

import io.onemfive.data.JSONSerializable;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface Route extends JSONSerializable {
    Route setRouteId(Long id);
    Long getRouteId();
    String getService();
    String getOperation();
//    Route setEnvelope(Envelope envelope);
    Route setRouted(Boolean routed);
    Boolean getRouted();
}
