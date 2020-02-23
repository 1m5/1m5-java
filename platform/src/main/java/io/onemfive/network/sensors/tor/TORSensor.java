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
package io.onemfive.network.sensors.tor;

import io.onemfive.data.*;
import io.onemfive.network.*;
import io.onemfive.network.peers.NetworkPeerDiscovery;
import io.onemfive.network.sensors.*;
import io.onemfive.network.sensors.tor.embedded.TORSensorSessionEmbedded;
import io.onemfive.network.sensors.tor.external.TORSensorSessionExternal;
import io.onemfive.util.Wait;
import io.onemfive.util.tasks.TaskRunner;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Sets up an HttpClientSensor with the local Tor instance as a proxy (127.0.0.1:9150).
 *
 * TODO: Add local node as Tor Hidden Service accepting external Tor calls
 *
 * @author objectorange
 */
public final class TORSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(TORSensor.class.getName());

    public static final String TOR_ROUTER_EMBEDDED = "settings.network.tor.routerEmbedded";
    public static final String TOR_HIDDENSERVICES_CONFIG = "settings.network.tor.hiddenServicesConfig";
    public static final String TOR_HIDDENSERVICE_CONFIG = "settings.network.tor.hiddenServiceConfig";

    public static final NetworkPeer seedATOR;
    private Process tor;

    static {
        seedATOR = new NetworkPeer(Network.TOR);
        seedATOR.setId("+sKVViuz2FPsl/XQ+Da/ivbNfOI=");
        seedATOR.setPort(35910); // virtual port
        seedATOR.getDid().getPublicKey().setAddress("5pdavjxcwrfx2meu");
        seedATOR.getDid().getPublicKey().setFingerprint("5pdavjxcwrfx2meu");
        seedATOR.getDid().getPublicKey().setType("RSA2048");
        seedATOR.getDid().getPublicKey().isIdentityKey(true);
        seedATOR.getDid().getPublicKey().setBase64Encoded(true);
    }

    private NetworkPeerDiscovery discovery;

    private File sensorDir;
    private boolean embedded = false;
    private final Map<String, SensorSession> sessions = new HashMap<>();
    private Thread taskRunnerThread;

    public TORSensor() {
        super(Network.TOR);
        taskRunner = new TaskRunner(1, 1);
    }

    public TORSensor(SensorManager sensorManager) {
        super(sensorManager, Network.TOR);
        taskRunner = new TaskRunner(1, 1);
    }

    public String[] getOperationEndsWith() {
        return new String[]{".onion"};
    }

    @Override
    public String[] getURLBeginsWith() {
        return new String[]{"tor"};
    }

    @Override
    public String[] getURLEndsWith() {
        return new String[]{".onion"};
    }

    @Override
    public boolean sendOut(NetworkPacket packet) {
        LOG.info("Tor Sensor sending request...");
        SensorSession sensorSession = establishSession(null, true);
        if(sensorSession==null) {
            return false;
        }
        boolean successful = sensorSession.send(packet);
        if (successful) {
            LOG.info("Tor Sensor successful response received.");
            if (!getStatus().equals(SensorStatus.NETWORK_CONNECTED)) {
                LOG.info("Tor Network status changed back to CONNECTED.");
                updateStatus(SensorStatus.NETWORK_CONNECTED);
            }
        }
        return successful;
    }

    @Override
    public SensorSession establishSession(String address, Boolean autoConnect) {
        if(address==null) {
            address = "127.0.0.1";
        }
        if(sessions.get(address)==null) {
            SensorSession sensorSession = embedded ? new TORSensorSessionEmbedded(this) : new TORSensorSessionExternal(this);

            if(sensorSession.init(properties) && sensorSession.open(address)) {
                if (autoConnect) {
                    sensorSession.connect();
                }
                sessions.put(address, sensorSession);
                return sessions.get(address);
            }
        }
        return null;
    }

    @Override
    public void updateConfig(NetworkState config) {
        LOG.warning("Not implemented.");
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return super.sendIn(envelope);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting TOR Sensor...");
        updateStatus(SensorStatus.STARTING);
        this.properties = properties;
        String sensorsDirStr = properties.getProperty("1m5.dir.sensors");
        if (sensorsDirStr == null) {
            LOG.warning("1m5.dir.sensors property is null. Please set prior to instantiating Tor Sensor.");
            return false;
        }
        try {
            sensorDir = new File(new File(sensorsDirStr), "tor");
            if (!sensorDir.exists() && !sensorDir.mkdir()) {
                LOG.warning("Unable to create Tor sensor directory.");
                return false;
            } else {
                properties.put("1m5.dir.sensors.tor", sensorDir.getCanonicalPath());
            }
        } catch (IOException e) {
            LOG.warning("IOException caught while building Tor sensor directory: \n" + e.getLocalizedMessage());
            return false;
        }

        Wait.aMs(500); // Give the infrastructure a bit of breathing room before saving seeds
        if(sensorManager.getPeerManager().savePeer(seedATOR, true)) {
            networkState.seeds.add(seedATOR);
        }

        updateStatus(SensorStatus.STARTING);

        embedded = "true".equals(properties.getProperty(TOR_ROUTER_EMBEDDED));
        networkState.params.put(TOR_ROUTER_EMBEDDED, String.valueOf(embedded));

        SensorSession torSession = null;
        do {
            torSession = establishSession("127.0.0.1", true);
            LOG.warning(getStatus().name());
            if (torSession != null && torSession.isConnected()) {
                updateStatus(SensorStatus.NETWORK_CONNECTED);
                kickOffDiscovery();
            } else if(getStatus()==SensorStatus.NETWORK_UNAVAILABLE) {
                if(!embedded) {
                    LOG.warning("TOR Unavailable and not embedded; attempting to start TOR externally...");
                    try {
                        tor = Runtime.getRuntime().exec("tor");
                        LOG.warning("TOR (pid="+tor.pid()+") started. Waiting a few seconds to warm up...");
                        Wait.aSec(3);
                        torSession = establishSession("127.0.0.1", true);
                        if (torSession != null && torSession.isConnected()) {
                            updateStatus(SensorStatus.NETWORK_CONNECTED);
                            kickOffDiscovery();
                        }
                    } catch (IOException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
                }
            } else if(getStatus()!=SensorStatus.NETWORK_CONNECTING) {
                updateStatus(SensorStatus.NETWORK_CONNECTING);
            }
            Wait.aSec(3);
        } while(torSession == null || !torSession.isConnected());

        return true;
    }

    private void kickOffDiscovery() {
        // Setup Discovery
        discovery = new NetworkPeerDiscovery(taskRunner, this);
        taskRunner.addTask(discovery);
        taskRunnerThread = new Thread(taskRunner);
        taskRunnerThread.setDaemon(true);
        taskRunnerThread.setName("TORSensor-TaskRunnerThread");
        taskRunnerThread.start();
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
        updateStatus(SensorStatus.SHUTTING_DOWN);
        if(taskRunnerThread!=null)
            taskRunnerThread.interrupt();
        for(SensorSession session : sessions.values()) {
            session.disconnect();
            session.close();
        }
        sessions.clear();
        if(tor!=null) {
            tor.destroyForcibly();
            tor=null;
        }
        updateStatus(SensorStatus.SHUTDOWN);
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        updateStatus(SensorStatus.GRACEFULLY_SHUTTING_DOWN);
        if(taskRunnerThread!=null)
            taskRunnerThread.interrupt();
        for(SensorSession session : sessions.values()) {
            session.disconnect();
            session.close();
        }
        sessions.clear();
        if(tor!=null) {
            tor.destroy();
            tor=null;
        }
        updateStatus(SensorStatus.GRACEFULLY_SHUTDOWN);
        return true;
    }
}
