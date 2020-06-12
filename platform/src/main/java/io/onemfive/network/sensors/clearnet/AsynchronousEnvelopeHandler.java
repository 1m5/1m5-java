package io.onemfive.network.sensors.clearnet;

import io.onemfive.data.Envelope;
import org.eclipse.jetty.server.Handler;

/**
 * TODO: Add Description
 *
 */
public interface AsynchronousEnvelopeHandler extends Handler {
    void setClearnetSession(ClearnetSession clearnetSession);
    void setServiceName(String serviceName);
    void setParameters(String[] parameters);
    void reply(Envelope envelope);
}
