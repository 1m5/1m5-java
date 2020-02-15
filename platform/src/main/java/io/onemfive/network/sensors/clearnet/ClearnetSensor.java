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

import io.onemfive.data.Network;
import io.onemfive.network.NetworkPacket;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorSession;
import io.onemfive.network.sensors.SensorStatus;
import org.eclipse.jetty.server.Server;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Clearnet access acting as a Client and Server
 */
public class ClearnetSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(ClearnetSensor.class.getName());

    /**
     * Configuration of Sessions in the form:
     *      name, port, launch on start, concrete implementation of io.onemfive.network.sensors.clearnet.AsynchronousEnvelopeHandler, run websocket, relative resource directory|n,...}
     */
    public static final String SESSIONS_CONFIG = "settings.network.clearnet.sessionConfigs";
    public static final String SESSION_CONFIG = "settings.network.clearnet.sessionConfig";

    private boolean isTest = false;
    private boolean clientsEnabled = false;
    private boolean serversEnabled = false;

    public ClearnetSensor() {
        super(Network.HTTPS);
    }

    public ClearnetSensor(Network network) {
        super(network);
    }

    public ClearnetSensor(SensorManager sensorManager) {
        super(sensorManager, Network.HTTPS);
    }

    public ClearnetSensor(SensorManager sensorManager, Network network) {
        super(sensorManager, network);
    }

    @Override
    public String[] getOperationEndsWith() {
        return new String[]{".html",".htm",".do",".json"};
    }

    @Override
    public String[] getURLBeginsWith() {
        return new String[]{"http","https"};
    }

    @Override
    public String[] getURLEndsWith() {
        return new String[]{".html",".htm",".do",".json"};
    }

    @Override
    public SensorSession establishSession(String spec, Boolean autoConnect) {
        Properties props;
        if(sessions.get(spec)==null) {
            SensorSession sensorSession = new ClearnetSession(this);
            props = new Properties();
            props.setProperty(SESSION_CONFIG, spec);
            sensorSession.init(props);
            sensorSession.open("127.0.0.1");
            if (autoConnect) {
                sensorSession.connect();
            }
            sessions.put(spec, sensorSession);
        }
        return sessions.get(spec);
    }

    @Override
    public boolean sendOut(NetworkPacket packet) {
        LOG.info("Send Clearnet Message Out Packet received...");
        SensorSession sensorSession = establishSession(null, true);
        return sensorSession.send(packet);
    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Starting...");
        properties = p;
        updateStatus(SensorStatus.INITIALIZING);
        String sensorsDirStr = properties.getProperty("1m5.dir.sensors");
        if (sensorsDirStr == null) {
            LOG.warning("1m5.dir.sensors property is null. Please set prior to instantiating Clearnet Client Sensor.");
            return false;
        }
        try {
            File sensorDir = new File(new File(sensorsDirStr), "clearnet");
            if (!sensorDir.exists() && !sensorDir.mkdir()) {
                LOG.warning("Unable to create Clearnet Sensor directory.");
                return false;
            } else {
                properties.put("1m5.dir.sensors.clearnet", sensorDir.getCanonicalPath());
            }
        } catch (IOException e) {
            LOG.warning("IOException caught while building Clearnet sensor directory: \n" + e.getLocalizedMessage());
            return false;
        }
        String sessionsConfig = properties.getProperty(SESSIONS_CONFIG);
        LOG.info("Building sessions from configuration: " + sessionsConfig);
        String[] sessionsSpecs = sessionsConfig.split(":");
        LOG.info("Number of sessions to start: " + sessionsSpecs.length);

        updateStatus(SensorStatus.STARTING);

        for(String spec : sessionsSpecs) {
            establishSession(spec, true);
        }
        return true;
    }

    @Override
    public boolean pause() {
        LOG.warning("Pausing not supported.");
        return false;
    }

    @Override
    public boolean unpause() {
        LOG.warning("Pausing not supported.");
        return false;
    }

    @Override
    public boolean restart() {
        LOG.info("Restarting...");
        for(SensorSession session : sessions.values()) {
            String address = session.getAddress();
            if(!session.close() || !session.open(address))
                return false;
        }
        LOG.info("Restarted.");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down...");
        for(SensorSession session : sessions.values()) {
            session.close();
        }
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }

}
