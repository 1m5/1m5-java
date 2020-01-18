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
package io.onemfive.network.sensors.wifidirect;

import io.onemfive.data.Envelope;
import io.onemfive.network.Network;
import io.onemfive.network.NetworkPeer;
import io.onemfive.network.Packet;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorSession;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import static io.onemfive.network.sensors.SensorStatus.NETWORK_CONNECTED;

public class WiFiDirectSensor extends BaseSensor {

    public static Logger LOG = Logger.getLogger(WiFiDirectSensor.class.getName());

    public WiFiDirectSensor() {
        super(new NetworkPeer(Network.RADIO_WIFI_DIRECT));
    }

    public WiFiDirectSensor(SensorManager sensorManager) {
        super(sensorManager, new NetworkPeer(Network.RADIO_WIFI_DIRECT));
    }

    @Override
    public String[] getOperationEndsWith() {
        return new String[]{".wifid"};
    }

    @Override
    public String[] getURLBeginsWith() {
        return new String[]{"wifid"};
    }

    @Override
    public String[] getURLEndsWith() {
        return new String[]{".wifid"};
    }

    @Override
    public SensorSession establishSession(NetworkPeer peer, Boolean autoConnect) {
        return null;
    }

    /**
     * Sends UTF-8 content to a Radio Peer using Software Defined Radio (SDR).
     * @param packet Envelope containing SensorRequest as data.
     *                 To DID must contain base64 encoded Radio destination key.
     * @return boolean was successful
     */
    @Override
    public boolean sendOut(Packet packet) {
        LOG.info("Sending Radio Message...");
//        Envelope envelope = packet.getEnvelope();
//        NetworkRequest request = (NetworkRequest) DLC.getData(NetworkRequest.class,envelope);
//        if(request == null){
//            LOG.warning("No SensorRequest in Envelope.");
//            request.statusCode = ServiceMessage.REQUEST_REQUIRED;
//            return false;
//        }
//        NetworkPeer toPeer = request.destination.getPeer(Network.SDR.name());
//        if(toPeer == null) {
//            LOG.warning("No Peer for Radio found in toDID while sending to Radio.");
//            request.statusCode = NetworkRequest.DESTINATION_PEER_REQUIRED;
//            return false;
//        }
//        if(!Network.SDR.name().equals((toPeer.getNetwork()))) {
//            LOG.warning("Radio requires an SDR Peer.");
//            request.statusCode = NetworkRequest.DESTINATION_PEER_WRONG_NETWORK;
//            return false;
//        }
//        NetworkPeer fromPeer = request.origination.getPeer(Network.SDR.name());
//        LOG.info("Content to send: "+request.content);
//        if(request.content == null) {
//            LOG.warning("No content found in Envelope while sending to Radio.");
//            request.statusCode = NetworkRequest.NO_CONTENT;
//            return false;
//        }

//        Radio radio = RadioSelector.determineBestRadio(toRPeer);
//        if(radio==null) {
//            LOG.warning("Unhandled issue #1 here.");
//            return false;
//        }
//        RadioSession session = radio.establishSession(toRPeer, true);
//        if(session==null) {
//            LOG.warning("Unhandled issue #2 here.");
//            return false;
//        }
//        RadioDatagram datagram = session.toRadioDatagram(request);
//        Properties options = new Properties();
//        if(session.sendDatagram(datagram)) {
//            LOG.info("Radio Message sent.");
//            return true;
//        } else {
//            LOG.warning("Radio Message sending failed.");
//            request.statusCode = NetworkRequest.SENDING_FAILED;
//            return false;
//        }
        return true;
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return super.sendIn(envelope);
    }

    /**
     * Will be called only if you register via addSessionListener().
     *
     * After this is called, the client should call receiveMessage(msgId).
     * There is currently no method for the client to reject the message.
     * If the client does not call receiveMessage() within a timeout period
     * (currently 30 seconds), the session will delete the message and
     * log an error.
     *
     * @param session session to notify
     */
    public void messageAvailable(SensorSession session) {
//        RadioDatagram d = session.receiveDatagram(port);
//        LOG.info("Received Radio Message:\n\tFrom: " + d.from.getSDRAddress());
//        Envelope e = Envelope.eventFactory(EventMessage.Type.TEXT);
//        DID did = new DID();
//        did.addPeer(d.from);
//        e.setDID(did);
//        EventMessage m = (EventMessage) e.getMessage();
//        m.setName(d.from.getSDRFingerprint());
//        m.setMessage(d);
//        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
//        LOG.info("Sending Event Message to Notification Service...");
//        sendIn(e);
    }

    @Override
    public void connected(SensorSession session) {
        LOG.info("Radio Session reporting connection.");
        updateStatus(NETWORK_CONNECTED);
        routerStatusChanged();
    }

    /**
     * Notify the service that the session has been terminated.
     * All registered listeners will be called.
     *
     * @param session session to report disconnect to
     */
    @Override
    public void disconnected(SensorSession session) {
        LOG.info("Radio Session reporting disconnection.");
//        if(session.getRadio().disconnected())){
//            updateStatus(NETWORK_STOPPED);
//        }
//        routerStatusChanged();
    }

    /**
     * Notify the client that some throwable occurred.
     * All registered listeners will be called.
     *
     * @param session session to report error occurred
     * @param message message received describing error
     * @param throwable throwable thrown during error
     */
    @Override
    public void errorOccurred(SensorSession session, String message, Throwable throwable) {
        LOG.warning("Router says: "+message+": "+throwable.getLocalizedMessage());
        routerStatusChanged();
    }

    public void checkRouterStats() {
        LOG.info("RadioSensor status:\n\t"+getStatus().name());
    }

    private void routerStatusChanged() {
        String statusText;
        switch (getStatus()) {
            case NETWORK_CONNECTING:
                statusText = "Testing Radio Network...";
                break;
            case NETWORK_CONNECTED:
                statusText = "Connected to Radio Network.";
                restartAttempts = 0; // Reset restart attempts
                break;
            case NETWORK_STOPPED:
                statusText = "Disconnected from Radio Network.";
                break;
            default: {
                statusText = "Unhandled Radio Network Status: "+getStatus().name();
            }
        }
        LOG.info(statusText);
    }

    @Override
    public boolean start(Properties properties) {
        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }
}
