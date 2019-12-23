package io.onemfive.network.sensors.radio;

import io.onemfive.network.sensors.radio.tasks.TaskRunner;
import io.onemfive.network.peers.PeerReport;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class BaseRadio implements Radio {

    private static final Logger LOG = Logger.getLogger(BaseRadio.class.getName());

    protected Map<Integer,RadioSession> sessions = new HashMap<>();
    protected PeerReport peerReport;
    protected RadioSensor sensor;
    protected TaskRunner taskRunner;

    public void setRadioSensor(RadioSensor sensor) {
        this.sensor = sensor;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    @Override
    public void setPeerReport(PeerReport peerReport) {
        this.peerReport = peerReport;
    }

    @Override
    public RadioSession getSession(Integer sessId) {
        return sessions.getOrDefault(sessId, null);
    }

    @Override
    public Boolean closeSession(Integer sessionId) {
        RadioSession session = sessions.get(sessionId);
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

    @Override
    public Boolean disconnected() {
        return sessions.size()==0;
    }

    @Override
    public boolean start(Properties properties) {
        return false;
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
        boolean success = true;
        if(sessions!=null) {
            Collection<RadioSession> rl = sessions.values();
            for(RadioSession r : rl) {
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
