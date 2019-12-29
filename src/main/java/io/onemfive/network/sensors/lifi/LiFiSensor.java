package io.onemfive.network.sensors.lifi;

import io.onemfive.data.ServiceMessage;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.data.*;
import io.onemfive.data.util.DLC;
import io.onemfive.data.util.DataFormatException;
import io.onemfive.data.util.JSONParser;
import io.onemfive.data.util.JSONPretty;
import io.onemfive.network.*;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorStatus;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

import static io.onemfive.network.sensors.SensorStatus.NETWORK_CONNECTED;

public class LiFiSensor extends BaseSensor implements LiFiSessionListener {

    private static final Logger LOG = Logger.getLogger(LiFiSensor.class.getName());

    private LiFiSession session;
    private LiFiPeer localNode;

    public LiFiSensor() {
        super();
    }

    public LiFiSensor(SensorManager sensorManager, Sensitivity sensitivity, Integer priority) {
        super(sensorManager, sensitivity, priority);
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

    /**
     * Sends UTF-8 content to a Destination using LiFi.
     * @param request Packet of data for request.
     *                 To DID must contain base64 encoded LiFi destination key.
     * @return boolean was successful
     */
    @Override
    public boolean sendOut(Packet request) {
        LOG.info("Sending LiFi Message...");
        NetworkPeer toPeer = request.getToPeer();
        if(toPeer == null) {
            LOG.warning("No Peer for LiFi found in toDID while sending to LiFi.");
            request.statusCode = NetworkRequest.DESTINATION_PEER_REQUIRED;
            return false;
        }
        if(!Network.LIFI.name().equals((toPeer.getNetwork()))) {
            LOG.warning("LiFi requires a LiFiPeer.");
            request.statusCode = NetworkRequest.DESTINATION_PEER_WRONG_NETWORK;
            return false;
        }
        LOG.info("Envelope to send: "+request.getEnvelope());
        if(request.getEnvelope() == null) {
            LOG.warning("No Envelope while sending to LiFi.");
            request.statusCode = NetworkRequest.NO_CONTENT;
            return false;
        }

        Destination toDestination = session.lookupDestination(toPeer.getAddress());
        if(toDestination == null) {
            LOG.warning("LiFi Peer To Destination not found.");
            request.statusCode = NetworkRequest.DESTINATION_PEER_NOT_FOUND;
            return false;
        }
        LiFiDatagramBuilder builder = new LiFiDatagramBuilder(session);
        LiFiDatagram datagram = builder.makeLiFIDatagram(JSONPretty.toPretty(JSONParser.toString(request),4).getBytes());
        Properties options = new Properties();
        if(session.sendMessage(toDestination, datagram, options)) {
            LOG.info("LiFi Message sent.");
            return true;
        } else {
            LOG.warning("LiFi Message sending failed.");
            request.statusCode = NetworkRequest.SENDING_FAILED;
            return false;
        }
    }

    /**
     * Outgoing reply to incoming request
     * @param packet
     * @return
     */
    @Override
    public boolean replyOut(Packet packet) {

        return true;
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return super.sendIn(envelope);
    }

    @Override
    public boolean replyIn(Envelope envelope) {
        return super.replyIn(envelope);
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
        byte[] msg = session.receiveMessage(msgId);

        LOG.info("Loading LiFi Datagram...");
        LiFiDatagramExtractor d = new LiFiDatagramExtractor();
        d.extractLiFiDatagram(msg);
        LOG.info("LiFi Datagram loaded.");
        byte[] payload = d.getPayload();
        String strPayload = new String(payload);
        LOG.info("Getting sender as LiFi Destination...");
        Destination sender = d.getSender();
        String address = sender.toBase64();
        String fingerprint = null;
        try {
            fingerprint = sender.getHash().toBase64();
        } catch (DataFormatException e) {
            LOG.warning(e.getLocalizedMessage());
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
        }
        LOG.info("Received LiFi Message:\n\tFrom: " + address +"\n\tContent: " + strPayload);

        Envelope e = Envelope.eventFactory(EventMessage.Type.TEXT);
        NetworkPeer from = new NetworkPeer(Network.LIFI.name());
        from.setAddress(address);
        from.setFingerprint(fingerprint);
        DID did = new DID();
        did.addPeer(from);
        e.setDID(did);
        EventMessage m = (EventMessage) e.getMessage();
        m.setName(fingerprint);
        m.setMessage(strPayload);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
        LOG.info("Sending Event Message to Notification Service...");
        sendIn(e);
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
        session = new LiFiSession();
        session.connect();

        Destination localDestination = session.getLocalDestination();
        String address = localDestination.toBase64();
        String fingerprint = localDestination.getHash().toBase64();
        LOG.info("LiFiSensor Local destination key in base64: " + address);
        LOG.info("LiFiSensor Local destination fingerprint (hash) in base64: " + fingerprint);

        session.addSessionListener(this);

        NetworkPeer np = new NetworkPeer(Network.LIFI.name());
        np.getDid().getPublicKey().setFingerprint(fingerprint);
        np.getDid().getPublicKey().setAddress(address);

        DID localDID = new DID();
        localDID.addPeer(np);

        // Publish local LiFi address
        LOG.info("Publishing LiFi Network Peer's DID...");
        Envelope e = Envelope.eventFactory(EventMessage.Type.STATUS_DID);
        EventMessage m = (EventMessage) e.getMessage();
        m.setName(fingerprint);
        m.setMessage(localDID);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
//        sensorManager.sendToBus(e);
    }

    public LiFiPeer getLocalNode() {
        return localNode;
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
