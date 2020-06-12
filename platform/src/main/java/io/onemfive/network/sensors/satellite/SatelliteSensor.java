package io.onemfive.network.sensors.satellite;

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

public final class SatelliteSensor extends BaseSensor {

    public static Logger LOG = Logger.getLogger(SatelliteSensor.class.getName());

    public static final NetworkState config = new NetworkState();

    private Thread taskRunnerThread;

    public SatelliteSensor() {
        super(Network.Satellite);
//        taskRunner = new TaskRunner(1, 1);
    }

    public SatelliteSensor(SensorManager sensorManager) {
        super(sensorManager, Network.Satellite);
//        taskRunner = new TaskRunner(1, 1);
    }

    @Override
    public String[] getOperationEndsWith() {
        return new String[]{".sat"};
    }

    @Override
    public String[] getURLBeginsWith() {
        return new String[]{"sat"};
    }

    @Override
    public String[] getURLEndsWith() {
        return new String[]{".sat"};
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
     * Sends UTF-8 content to a Satellite Peer using Software Defined Radio (SDR).
     * @param packet Envelope containing SensorRequest as data.
     *                 To DID must contain base64 encoded Radio destination key.
     * @return boolean was successful
     */
    @Override
    public boolean sendOut(NetworkPacket packet) {
        LOG.info("Sending Satellite Message...");

        return true;
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return super.sendIn(envelope);
    }

    @Override
    public void connected(SensorSession session) {
        LOG.info("Satellite Session reporting connection.");
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
        LOG.info("Satellite Session reporting disconnection.");

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
        LOG.info("SatelliteSensor status:\n\t"+getStatus().name());
    }

    private void routerStatusChanged() {
        String statusText;
        switch (getStatus()) {
            case NETWORK_CONNECTING:
                statusText = "Testing Satellite Network...";
                break;
            case NETWORK_CONNECTED:
                statusText = "Connected to Satellite Network.";
                restartAttempts = 0; // Reset restart attempts
                break;
            case NETWORK_STOPPED:
                statusText = "Disconnected from Satellite Network.";
                break;
            default: {
                statusText = "Unhandled Satellite Network Status: "+getStatus().name();
            }
        }
        LOG.info(statusText);
    }

    @Override
    public boolean start(Properties properties) {

        taskRunnerThread = new Thread(taskRunner);
        taskRunnerThread.setDaemon(true);
        taskRunnerThread.setName("SatelliteSensor-TaskRunnerThread");
        taskRunnerThread.start();
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
