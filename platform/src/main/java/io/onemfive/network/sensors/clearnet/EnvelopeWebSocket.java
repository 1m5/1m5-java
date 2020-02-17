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
package io.onemfive.network.sensors.clearnet;

import io.onemfive.data.Envelope;
import io.onemfive.network.Request;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Handles incoming web requests from browsers by receiving Envelopes in JSON,
 * converting them to Java Envelopes, sending them to the Bus, receiving
 * Envelopes from the Bus, serializing them into JSON, and sending them to the browser.
 *
 * All routes and data must be placed appropriately within the Envelope for it to route properly.
 *
 * Jetty sets Session through onWebSocketConnect().
 *
 * Feel free to extend overriding onWebSocketText() and pushEnvelope().
 *
 * @author objectorange
 */
public class EnvelopeWebSocket extends WebSocketAdapter {

    private static Logger LOG = Logger.getLogger(EnvelopeWebSocket.class.getName());

    protected ClearnetSession clearnetSession;
    protected Session session;

    public EnvelopeWebSocket() {}

    public EnvelopeWebSocket(ClearnetSession clearnetSession) {
        this.clearnetSession = clearnetSession;
    }

    public void setClearnetSession(ClearnetSession clearnetSession) {
        this.clearnetSession = clearnetSession;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        LOG.info("+++ WebSocket Connect...");
        this.session = session;
        LOG.info("Host: "+session.getRemoteAddress().getAddress().getCanonicalHostName());
    }

    @Override
    public void onWebSocketText(String message) {
        LOG.info("WebSocket Text received: "+message);
        if(message != null && !message.equals("keep-alive")) {
            Envelope e = Envelope.documentFactory();
            e.fromJSON(message);
            LOG.info("Sending Envelope received to bus...\n\t"+e);
            Request request = new Request();
            request.setEnvelope(e);
            // Send to bus
            clearnetSession.sendIn(request);
        }
    }

    public void pushEnvelope(Envelope e) {
        LOG.info("Received Envelope to send to browser:\n\t" + e);
        if (session == null) {
            LOG.warning("Jetty WebSocket session not yet established. Unable to send message.");
            return;
        }
        try {
            RemoteEndpoint endpoint = session.getRemote();
            if (endpoint == null) {
                LOG.warning("No RemoteEndpoint found for current Jetty WebSocket session.");
            } else {
                LOG.info("Sending Envelope as JSON text to browser...");
                endpoint.sendString(e.toJSON());
                LOG.info("Envelope as JSON text sent to browser.");
            }
        } catch (IOException ex) {
            LOG.warning(ex.getLocalizedMessage());
        }
    }

}
