package io.onemfive.network.sensors.wifidirect;

import io.onemfive.data.Envelope;
import io.onemfive.data.Network;
import io.onemfive.network.NetworkState;
import io.onemfive.network.NetworkPacket;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorSession;
import io.onemfive.util.tasks.TaskRunner;

import java.util.Properties;
import java.util.logging.Logger;

import static io.onemfive.network.sensors.SensorStatus.NETWORK_CONNECTED;

public final class WiFiDirectSensor extends BaseSensor {

    public static Logger LOG = Logger.getLogger(WiFiDirectSensor.class.getName());

    private Thread taskRunnerThread;

    public WiFiDirectSensor() {
        super(Network.WiFiDirect);
//        taskRunner = new TaskRunner(1, 1);
    }

    public WiFiDirectSensor(SensorManager sensorManager) {
        super(sensorManager, Network.WiFiDirect);
//        taskRunner = new TaskRunner(1, 1);
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
    public SensorSession establishSession(String address, Boolean autoConnect) {
        return null;
    }

    @Override
    public void updateState(NetworkState networkState) {
        LOG.warning("Not implemented.");
    }

    /**
     * Sends UTF-8 content to a Radio Peer using Software Defined Radio (SDR).
     * @param packet Envelope containing SensorRequest as data.
     *                 To DID must contain base64 encoded Radio destination key.
     * @return boolean was successful
     */
    @Override
    public boolean sendOut(NetworkPacket packet) {
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

    @Override
    public void connected(SensorSession session) {
        LOG.info("WiFi Direct Session reporting connection.");
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
        LOG.info("WiFi Direct Session reporting disconnection.");
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
                statusText = "Testing WiFi Direct Network...";
                break;
            case NETWORK_CONNECTED:
                statusText = "Connected to WiFi Direct Network.";
                restartAttempts = 0; // Reset restart attempts
                break;
            case NETWORK_STOPPED:
                statusText = "Disconnected from WiFi Direct Network.";
                break;
            default: {
                statusText = "Unhandled WiFi Direct Network Status: "+getStatus().name();
            }
        }
        LOG.info(statusText);
    }

    @Override
    public boolean start(Properties properties) {

//        taskRunnerThread = new Thread(taskRunner);
//        taskRunnerThread.setDaemon(true);
//        taskRunnerThread.setName("WiFiDirectSensor-TaskRunnerThread");
//        taskRunnerThread.start();
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
