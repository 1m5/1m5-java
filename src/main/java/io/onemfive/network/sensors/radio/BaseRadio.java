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
