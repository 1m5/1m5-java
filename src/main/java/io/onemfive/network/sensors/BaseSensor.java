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
package io.onemfive.network.sensors;

import io.onemfive.network.Packet;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.util.tasks.TaskRunner;
import io.onemfive.data.Envelope;
import io.onemfive.network.Network;
import io.onemfive.network.NetworkPeer;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import static io.onemfive.network.sensors.SensorStatus.NETWORK_CONNECTED;
import static io.onemfive.network.sensors.SensorStatus.NETWORK_STOPPED;

/**
 * A Base for common data and operations across all Sensors to provide a basic framework for them.
 *
 * @author objectorange
 */
public abstract class BaseSensor implements Sensor {

    private static final Logger LOG = Logger.getLogger(BaseSensor.class.getName());

    protected final NetworkPeer localPeer;
    protected Network network;
    protected SensorManager sensorManager;
    private SensorStatus sensorStatus = SensorStatus.NOT_INITIALIZED;
    protected Integer restartAttempts = 0;
    protected TaskRunner taskRunner;
    protected String directory;
    protected Map<Integer, SensorSession> sessions = new HashMap<>();
    protected Properties properties;

    public BaseSensor(NetworkPeer localPeer) {
        this.localPeer = localPeer;
        this.network = localPeer.getNetwork();
    }

    public BaseSensor(SensorManager sensorManager, NetworkPeer localPeer) {
        this.sensorManager = sensorManager;
        this.localPeer = localPeer;
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void updateStatus(SensorStatus sensorStatus) {
        this.sensorStatus = sensorStatus;
        // Might be null during localized testing
        if(sensorManager != null) {
            sensorManager.updateSensorStatus(this.getClass().getName(), sensorStatus);
        }
    }

    public SensorSession getSession(NetworkPeer peer) {
        for(SensorSession s : sessions.values()) {
            if(s.getLocalPeer().equals(peer))
                return s;
        }
        return null;
    }

    public SensorSession getSession(Integer sessId) {
        return sessions.getOrDefault(sessId, null);
    }

    public abstract SensorSession establishSession(NetworkPeer peer, Boolean autoConnect);

    public Boolean closeSession(Integer sessionId) {
        SensorSession session = sessions.get(sessionId);
        if(session==null) {
            LOG.info("No session found in sessions map for id: "+sessionId);
            return true;
        } else if (session.disconnect()) {
            sessions.remove(sessionId);
            LOG.info("Session (id="+sessionId+") disconnected and remove from sessions map.");
            return true;
        } else {
            LOG.warning("Issue with disconnection of session with id: "+sessionId);
            return false;
        }
    }

    public void connected(SensorSession session) {
        LOG.info("Radio Session reporting connection.");
        if(getStatus()!=NETWORK_CONNECTED) {
            updateStatus(NETWORK_CONNECTED);
            routerStatusChanged();
        }
    }

    /**
     * Notify the service that the session has been terminated.
     * All registered listeners will be called.
     *
     * @param session session to report disconnect to
     */
    public void disconnected(SensorSession session) {
        LOG.info("Radio Session reporting disconnection.");
        if(disconnected()) {
            updateStatus(NETWORK_STOPPED);
            routerStatusChanged();
        }
    }

    /**
     * Notify the client that some throwable occurred.
     * All registered listeners will be called.
     *
     * @param session session to report error occurred
     * @param message message received describing error
     * @param throwable throwable thrown during error
     */
    public void errorOccurred(SensorSession session, String message, Throwable throwable) {
        LOG.warning("Router says: "+message+": "+throwable.getLocalizedMessage());
        routerStatusChanged();
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

    public Boolean disconnected() {
        return sessions.size()==0;
    }

    public void checkRouterStats() {
        LOG.info("RadioSensor status:\n\t"+getStatus().name());
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return sensorManager.sendToBus(envelope);
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    @Override
    public SensorStatus getStatus() {
        return sensorStatus;
    }

    @Override
    public Integer getRestartAttempts() {
        return restartAttempts;
    }

    @Override
    public File getDirectory() {
        return sensorManager.getSensorDirectory(this.getClass().getName());
    }

    @Override
    public boolean shutdown() {
        boolean success = true;
        if(sessions!=null) {
            Collection<SensorSession> rl = sessions.values();
            for(SensorSession r : rl) {
                if(!r.disconnect()) {
                    success = false;
                }
            }
        }
        return success;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
