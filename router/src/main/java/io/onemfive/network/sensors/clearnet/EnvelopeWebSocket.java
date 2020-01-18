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
import io.onemfive.data.EventMessage;
import io.onemfive.data.ManCon;
import io.onemfive.util.DLC;
import io.onemfive.network.NetworkService;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Handles incoming web requests from browsers by sending text within
 * a Low Sensitivity Envelope to the bus.
 *
 * Handles Envelope events from bus Notification Service by using
 * Session to send their event body as text to browser (push).
 *
 * Jetty sets Session through onWebSocketConnect().
 *
 * Feel free to extend overriding onWebSocketText() and pushEnvelope().
 *
 * @author objectorange
 */
public class EnvelopeWebSocket extends WebSocketAdapter {

    private static Logger LOG = Logger.getLogger(EnvelopeWebSocket.class.getName());

    protected ClearnetSensor sensor;
    protected Session session;

    public EnvelopeWebSocket() {}

    public EnvelopeWebSocket(ClearnetSensor sensor) {
        this.sensor = sensor;
    }

    public void setClearnetServerSensor(ClearnetSensor sensor) {
        this.sensor = sensor;
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
            LOG.info("Sending WebSocket text receieved to bus...");
            Envelope e = Envelope.eventFactory(EventMessage.Type.TEXT);
            // Flag as LOW for HTTP
            e.setManCon(ManCon.LOW);
            // Add Data
            DLC.addContent(message, e);
            // Add Route
            DLC.addRoute(NetworkService.class, NetworkService.OPERATION_REPLY, e);
            // Send to bus
            sensor.sendIn(e);
        }
    }

    public void pushEnvelope(Envelope e) {
        EventMessage em = DLC.getEventMessage(e);
        Object obj = em.getMessage();
        if(obj instanceof String) {
            String txt = (String)obj;
            LOG.info("Received Text Message to send to browser: " + txt);
            if (session == null) {
                LOG.warning("Jetty WebSocket session not yet established. Unable to send message.");
                return;
            }
            try {
                RemoteEndpoint endpoint = session.getRemote();
                if (endpoint == null) {
                    LOG.warning("No RemoteEndpoint found for current Jetty WebSocket session.");
                } else {
                    LOG.info("Sending text message to browser...");
                    endpoint.sendString(txt);
                    LOG.info("Text message sent to browser.");
                }
            } catch (IOException ex) {
                LOG.warning(ex.getLocalizedMessage());
            }
        } else {
            LOG.warning("Object received not a String and thus not handled by this adapter. Ignoring.");
        }
    }

}
