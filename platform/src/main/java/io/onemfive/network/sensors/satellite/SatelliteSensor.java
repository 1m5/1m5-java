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
package io.onemfive.network.sensors.satellite;

import io.onemfive.data.Envelope;
import io.onemfive.data.Network;
import io.onemfive.network.NetworkConfig;
import io.onemfive.network.NetworkPacket;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorSession;

import java.util.Properties;
import java.util.logging.Logger;

import static io.onemfive.network.sensors.SensorStatus.NETWORK_CONNECTED;

public class SatelliteSensor extends BaseSensor {

    public static Logger LOG = Logger.getLogger(SatelliteSensor.class.getName());

    public static final NetworkConfig config = new NetworkConfig();

    public SatelliteSensor() {
        super(Network.Satellite);
    }

    public SatelliteSensor(SensorManager sensorManager) {
        super(sensorManager, Network.Satellite);
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
}
