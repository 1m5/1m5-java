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
package io.onemfive.network.sensors.lifi;

import io.onemfive.core.notification.NotificationService;
import io.onemfive.data.*;
import io.onemfive.network.NetworkState;
import io.onemfive.network.NetworkPacket;
import io.onemfive.network.sensors.SensorSession;
import io.onemfive.util.DLC;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorStatus;

import java.util.Properties;
import java.util.logging.Logger;

import static io.onemfive.network.sensors.SensorStatus.NETWORK_CONNECTED;

public class LiFiSensor extends BaseSensor implements LiFiSessionListener {

    private static final Logger LOG = Logger.getLogger(LiFiSensor.class.getName());

    public static final NetworkState config = new NetworkState();

    private LiFiSession session;

    public LiFiSensor() {
       super(Network.LiFi);
    }

    public LiFiSensor(SensorManager sensorManager) {
        super(sensorManager, Network.LiFi);
    }

    @Override
    public String[] getOperationEndsWith() {
        return new String[]{".lifi"};
    }

    @Override
    public String[] getURLBeginsWith() {
        return new String[]{"lifi"};
    }

    @Override
    public String[] getURLEndsWith() {
        return new String[]{".lifi"};
    }

    @Override
    public SensorSession establishSession(String address, Boolean autoConnect) {
        LiFiSession session = new LiFiSession(this);

        return session;
    }

    @Override
    public void updateConfig(NetworkState config) {
        LOG.warning("Not implemented.");
    }

    /**
     * Sends UTF-8 content to a Destination using LiFi.
     * @param packet Packet of data for request.
     *                 To DID must contain base64 encoded LiFi destination key.
     * @return boolean was successful
     */
    @Override
    public boolean sendOut(NetworkPacket packet) {
        LOG.info("Sending LiFi Message...");
        if(packet instanceof NetworkPacket) {
            NetworkPacket np = (NetworkPacket)packet;
            NetworkPeer toPeer = np.getToPeer();
//        if(toPeer == null) {
//            LOG.warning("No Peer for LiFi found in toDID while sending to LiFi.");
//            packet.statusCode = NetworkRequest.DESTINATION_DID_REQUIRED;
//            return false;
//        }
//        if(!Network.LIFI.name().equals((toPeer.getNetwork()))) {
//            LOG.warning("LiFi requires a LiFiPeer.");
//            packet.statusCode = NetworkRequest.DESTINATION_DID_WRONG_NETWORK;
//            return false;
//        }
            LOG.info("Envelope to send: " + np.getEnvelope());
//        if(packet.getEnvelope() == null) {
//            LOG.warning("No Envelope while sending to LiFi.");
//            packet.statusCode = NetworkRequest.NO_CONTENT;
//            return false;
//        }
        }

        if(session.send(packet)) {
            LOG.info("LiFi Message sent.");
            return true;
        } else {
            LOG.warning("LiFi Message sending failed.");
//            packet.statusCode = NetworkRequest.SENDING_FAILED;
            return false;
        }
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return super.sendIn(envelope);
    }

    /**
     * Will be called only if you register via
     * setSessionListener() or addSessionListener().
     * And if you are doing that, just use LiFiSessionListener.
     *
     * @param session session to notify
     * @param msgId message number available
     * @param size size of the message - why it's a long and not an int is a mystery
     */
    @Override
    public void messageAvailable(LiFiSession session, int msgId, long size) {
        LOG.info("Message received by LiFi Sensor...");

//        EventMessage m = (EventMessage) e.getMessage();
//        m.setName(fingerprint);
//        m.setMessage(strPayload);
//        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
//        LOG.info("Sending Event Message to Notification Service...");
//        sendIn(e);
    }

    /**
     * Notify the service that the session has been terminated.
     * All registered listeners will be called.
     *
     * @param session session to report disconnect to
     */
    @Override
    public void disconnected(LiFiSession session) {
        LOG.warning("LiFi Session reporting disconnection.");
        routerStatusChanged();
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
    public void errorOccurred(LiFiSession session, String message, Throwable throwable) {
        LOG.severe("Router says: "+message+": "+throwable.getLocalizedMessage());
        routerStatusChanged();
    }

    public void checkRouterStats() {
        LOG.info("LiFiSensor stats:" +
                "\n\t...");
    }

    private void routerStatusChanged() {
        String statusText;
        switch (getStatus()) {
            case NETWORK_CONNECTING:
                statusText = "Testing LiFi Network...";
                break;
            case NETWORK_CONNECTED:
                statusText = "Connected to LiFi Network.";
                restartAttempts = 0; // Reset restart attempts
                break;
            case NETWORK_STOPPED:
                statusText = "Disconnected from LiFi Network.";
                restart();
                break;
            default: {
                statusText = "Unhandled LiFi Network Status: "+getStatus().name();
            }
        }
        LOG.info(statusText);
    }

    /**
     * Sets up a {@link LiFiSession}, using the LiFi Destination stored on disk or creating a new LiFi
     * destination if no key file exists.
     */
    private void initializeSession() throws Exception {
        LOG.info("Initializing LiFi Session....");
        updateStatus(SensorStatus.INITIALIZING);

        Properties sessionProperties = new Properties();
        session = new LiFiSession(this);
        session.connect();

        session.addSessionListener(this);

//        localPeer.getDid().getPublicKey().setFingerprint(fingerprint);
//        localPeer.getDid().getPublicKey().setAddress(address);

        // Publish local LiFi address
        LOG.info("Publishing LiFi Network Peer's DID...");
        Envelope e = Envelope.eventFactory(EventMessage.Type.PEER_STATUS);
        EventMessage m = (EventMessage) e.getMessage();
//        m.setName(fingerprint);
        m.setMessage(localPeer);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
//        sensorManager.sendToBus(e);
    }

    @Override
    public boolean start(Properties properties) {
        // TODO: for now just set as connected; we need to implement this within discovery
        updateStatus(NETWORK_CONNECTED);
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

    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean gracefulShutdown() {
        return false;
    }
}
