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

import io.onemfive.data.Envelope;
import io.onemfive.data.ManCon;
import io.onemfive.data.route.Route;
import io.onemfive.network.Network;
import io.onemfive.network.NetworkService;
import io.onemfive.network.Packet;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.util.AppThread;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * General ManCon to Network mappings:
 *
 * Internal:
 *      NONE: IMS
 * Internet:
 *      LOW: CLEAR (HTTPS)
 *      MEDIUM: Tor
 *      HIGH: I2P
 * Outernet:
 *      VERYHIGH: 1DN - Wireless Direct Ad-Hoc Network Radio Sensors (WiFi Direct, Bluetooth, Bluetooth LE, Satellite, Full Spectrum)
 *      EXTREME: 1DN - LiFi
 * Combination:
 *      NEO: A combination of Networks from MEDIUM to EXTREME with initial use of EXTREME
 *
 * TODO: Don't directly map ManCon's to Networks. Define each ManCon by threats, conditions to be observed to identify them, and how to mitigate them.
 *
 * @author objectorange
 */
public final class SensorManager {

    private static Logger LOG = Logger.getLogger(SensorManager.class.getName());

    public static final Network[] escalationPath = new Network[]{
            Network.IMS,
            Network.CLEAR,
            Network.TOR,
            Network.I2P,
            Network.RADIO_WIFI_DIRECT,
            Network.RADIO_BLUETOOTH,
            Network.RADIO_BLUETOOTH_LE,
            Network.RADIO_SATELLITE,
            Network.RADIO_FULLSPECTRUM,
            Network.LIFI
    };

    private final Map<String, Sensor> registeredSensors = new HashMap<>();
    private final Map<String, Sensor> activeSensors = new HashMap<>();
    private final Map<String, Sensor> blockedSensors = new HashMap<>();
    private final Map<String, List<SensorStatusListener>> listeners = new HashMap<>();

    private PeerManager peerManager;

    private NetworkService networkService;

    private final long MAX_BLOCK_TIME_BETWEEN_RESTARTS = 10 * 60 * 1000; // 10 minutes
    private Map<String,Long> sensorBlocks = new HashMap<>();

    public boolean init(final Properties properties) {
        // TODO: Add loop with checks
        Collection<Sensor> sensors = registeredSensors.values();
        for(final Sensor s : sensors) {
            LOG.info("Launching sensor "+s.getClass().getName());
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    s.start(properties);
                    activeSensors.put(s.getClass().getName(),s);
                }
            }).start();
        }
        return true;
    }

    public Sensor selectSensor(Packet packet) {
        // Lookup sensor by simple normal means
        Envelope e = packet.getEnvelope();
        Sensor selected = null;
        Route r = e.getRoute();
        // Lookup by ManCon
        if(e.getManCon() != null)
            selected = lookupByManCon(e.getManCon(), true);
        // Lookup by Operation
        if(selected == null) {
            selected = lookupByOperation(r.getOperation(), true);
        }
        // Lookup by URL
        if(selected == null && e.getURL() != null){
            selected = lookupByURL(e.getURL(), true);
        }
        return selected;
    }

    public Sensor lookupByManCon(ManCon minManCon, boolean onlyConnected) {
        Network[] potentialNetworks = withinManCon(minManCon);
        if(potentialNetworks!=null) {
            List<Sensor> sensors = new ArrayList<>(activeSensors.values());
            sortByNetworkPrecedence(sensors);
            for (Sensor s : sensors) {
                for (Network n : potentialNetworks) {
                    if (s.getNetwork() == n && (!onlyConnected || s.getStatus() == SensorStatus.NETWORK_CONNECTED)) {
                        return s;
                    }
                }
            }
        }
        return null;
    }

    public Sensor lookupByOperation(String operation, boolean onlyConnected) {
        List<Sensor> sensors = new ArrayList<>(activeSensors.values());
        sortByNetworkPrecedence(sensors);
        String[] ops;
        for(Sensor s : sensors) {
            ops = s.getOperationEndsWith();
            for(String op : ops) {
                if(op.equals(operation) && (!onlyConnected || s.getStatus() == SensorStatus.NETWORK_CONNECTED))
                    return s;
            }
        }
        return null;
    }

    public Sensor lookupByURL(URL url, boolean onlyConnected) {
        String protocol = url.getProtocol();
        List<Sensor> sensors = new ArrayList<>(activeSensors.values());
        sortByNetworkPrecedence(sensors);
        String[] urls;
        for(Sensor s : sensors) {
            urls = s.getURLBeginsWith();
            for(String u : urls) {
                if(u.equals(protocol) && (!onlyConnected || s.getStatus() == SensorStatus.NETWORK_CONNECTED))
                    return s;
            }
        }
        String path = url.getPath();
        for(Sensor s : sensors) {
            urls = s.getURLEndsWith();
            for(String u : urls) {
                if(u.equals(path) && (!onlyConnected || s.getStatus() == SensorStatus.NETWORK_CONNECTED))
                    return s;
            }
        }
        return null;
    }

    public static int escalationIndex(Network network) {
        int i = 0;
        for(Network n : escalationPath) {
            if(n==network) return i;
            i++;
        }
        return -1;
    }

    public static void sortByNetworkPrecedence(List<Sensor> sensors) {
        Collections.sort(sensors, new Comparator<Sensor>() {
            @Override
            public int compare(Sensor t, Sensor t1) {
                return Integer.compare(escalationIndex(t.getNetwork()), escalationIndex(t1.getNetwork()));
            }
        });
    }

    public static Network[] atManCon(ManCon manCon) {
        switch(manCon) {
            case NONE: return new Network[]{Network.IMS};
            case LOW: return new Network[]{Network.CLEAR};
            case MEDIUM: return new Network[]{Network.TOR};
            case HIGH: return new Network[]{Network.I2P};
            case VERYHIGH: return new Network[]{Network.RADIO_WIFI_DIRECT, Network.RADIO_BLUETOOTH, Network.RADIO_BLUETOOTH_LE, Network.RADIO_SATELLITE, Network.RADIO_FULLSPECTRUM};
            case EXTREME: return new Network[]{Network.LIFI};
            default: LOG.warning("ManCon not yet supported: "+manCon.name());
            return null;
        }
    }

    public static Network[] withinManCon(ManCon manCon) {
        switch(manCon) {
            case NONE: return new Network[]{Network.IMS,Network.CLEAR,Network.TOR,Network.I2P,Network.RADIO_WIFI_DIRECT, Network.RADIO_BLUETOOTH, Network.RADIO_BLUETOOTH_LE, Network.RADIO_SATELLITE, Network.RADIO_FULLSPECTRUM,Network.LIFI};
            case LOW: return new Network[]{Network.CLEAR,Network.TOR,Network.I2P,Network.RADIO_WIFI_DIRECT, Network.RADIO_BLUETOOTH, Network.RADIO_BLUETOOTH_LE, Network.RADIO_SATELLITE, Network.RADIO_FULLSPECTRUM,Network.LIFI};
            case MEDIUM: return new Network[]{Network.TOR,Network.I2P,Network.RADIO_WIFI_DIRECT, Network.RADIO_BLUETOOTH, Network.RADIO_BLUETOOTH_LE, Network.RADIO_SATELLITE, Network.RADIO_FULLSPECTRUM,Network.LIFI};
            case HIGH: return new Network[]{Network.I2P,Network.RADIO_WIFI_DIRECT, Network.RADIO_BLUETOOTH, Network.RADIO_BLUETOOTH_LE, Network.RADIO_SATELLITE, Network.RADIO_FULLSPECTRUM,Network.LIFI};
            case VERYHIGH: return new Network[]{Network.RADIO_WIFI_DIRECT, Network.RADIO_BLUETOOTH, Network.RADIO_BLUETOOTH_LE, Network.RADIO_SATELLITE, Network.RADIO_FULLSPECTRUM,Network.LIFI};
            case EXTREME: return new Network[]{Network.LIFI};
            default: LOG.warning("ManCon not yet supported: "+manCon.name());
                return null;
        }
    }

    public void updateSensorStatus(String sensorID, SensorStatus sensorStatus) {
        switch (sensorStatus) {
            case INITIALIZING: {
                LOG.info(sensorID + " reporting initializing....");
                break;
            }
            case STARTING: {
                LOG.info(sensorID + " reporting starting up....");
                break;
            }
            case WAITING: {
                LOG.info(sensorID + " reporting waiting....");
                break;
            }
            case NETWORK_WARMUP: {
                LOG.info(sensorID + " reporting network warming up....");
                break;
            }
            case NETWORK_PORT_CONFLICT: {
                LOG.info(sensorID + " reporting port conflict....");
                break;
            }
            case NETWORK_CONNECTING: {
                LOG.info(sensorID + " reporting connecting....");
                break;
            }
            case NETWORK_CONNECTED: {
                LOG.info(sensorID + " reporting connected.");
                if(sensorBlocks.get(sensorID)!=null) {
                    sensorBlocks.remove(sensorID);
                }
                break;
            }
            case NETWORK_STOPPING: {
                LOG.info(sensorID + " reporting stopping....");
                break;
            }
            case NETWORK_STOPPED: {
                LOG.info(sensorID + " reporting stopped.");
                if(activeSensors.containsKey(sensorID)) {
                    // Active Sensor Stopped, attempt to restart
                    Sensor sensor = activeSensors.get(sensorID);
                    if(sensor.restart()) {
                        LOG.info(sensorID+" restarted after disconnection.");
                    }
                }
                break;
            }
            case NETWORK_BLOCKED: {
                LOG.info(sensorID + " reporting blocked.");
                long now = System.currentTimeMillis();
                sensorBlocks.putIfAbsent(sensorID, now);
                if((now - sensorBlocks.get(sensorID)) > MAX_BLOCK_TIME_BETWEEN_RESTARTS) {
                    LOG.warning(sensorID + " reporting blocked longer than "+(MAX_BLOCK_TIME_BETWEEN_RESTARTS/60000)+" minutes. Restarting...");
                    // Active Sensor Blocked, attempt to restart
                    activeSensors.get(sensorID).restart();
                    // Reset blocked start time
                    sensorBlocks.put(sensorID, now);
                }
                break;
            }
            case NETWORK_ERROR: {
                LOG.info(sensorID + " reporting network error.");
                break;
            }
            case PAUSING: {
                LOG.info(sensorID + " reporting pausing....");
                // TODO: Persist messages to this sensor until unpaused then replay in order.
                break;
            }
            case PAUSED: {
                LOG.info(sensorID + " reporting paused....");
                break;
            }
            case UNPAUSING: {
                LOG.info(sensorID + " reporting unpausing....");
                // TODO: Replay any paused messages in order while resuming normal operations
                break;
            }
            case SHUTTING_DOWN: {
                LOG.info(sensorID + " reporting shutting down....");
                activeSensors.remove(sensorID);
                break;
            }
            case GRACEFULLY_SHUTTING_DOWN: {
                LOG.info(sensorID + " reporting gracefully shutting down....");
                activeSensors.remove(sensorID);
                break;
            }
            case SHUTDOWN: {
                LOG.info(sensorID + " reporting shutdown.");
                break;
            }
            case GRACEFULLY_SHUTDOWN: {
                LOG.info(sensorID + " reporting gracefully shutdown.");
                break;
            }
            case RESTARTING: {
                LOG.info(sensorID + " reporting restarting....");
                break;
            }
            case ERROR: {
                LOG.info(sensorID + " reporting error. Initiating hard restart...");
                Sensor s = activeSensors.get(sensorID);
                // Give stopping sensors a chance to clean up anything possible
                activeSensors.remove(sensorID);
                s.gracefulShutdown();
                // Regardless if it succeeds or not, replace it with a new instance and start it up
                try {
                    s = (Sensor)Class.forName(sensorID).getConstructor().newInstance();
                    if(s.start(networkService.getProperties())) {
                        activeSensors.put(sensorID, s);
                    } else {
                        LOG.warning("Unable to hard restart sensor: "+sensorID);
                    }
                } catch (Exception e) {
                    LOG.warning("Unable to create new instance of sensor for hard restart: "+sensorID);
                }
                break;
            }
            default: LOG.warning("Sensor Status for sensor "+sensorID+" not being handled: "+sensorStatus.name());
        }
        // Now update the Service's status based on the this Sensor's status
        networkService.determineStatus(sensorStatus);
        // Now update listeners
        if(listeners.get(sensorID)!=null) {
            List<SensorStatusListener> sslList = listeners.get(sensorID);
            for(SensorStatusListener ssl : sslList) {
                ssl.statusUpdated(sensorStatus);
            }
        }
    }

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public void registerSensor(Sensor sensor) {
        registeredSensors.put(sensor.getClass().getName(), sensor);
    }

    public Map<String, Sensor> getRegisteredSensors() {
        return registeredSensors;
    }

    public Map<String, Sensor> getActiveSensors() {
        return activeSensors;
    }

    public Map<String, Sensor> getBlockedSensors(){
        return blockedSensors;
    }

    public SensorStatus getSensorStatus(String sensor) {
        Sensor s = activeSensors.get(sensor);
        if(s == null) {
            return SensorStatus.UNREGISTERED;
        } else {
            return s.getStatus();
        }
    }

    public Sensor getRegisteredSensor(String sensorName) {
        return registeredSensors.get(sensorName);
    }

    public boolean isActive(String sensorName) {
        return activeSensors.containsKey(sensorName);
    }

    public File getSensorDirectory(String sensorName) {
        return new File(networkService.getSensorsDirectory(), sensorName);
    }

    public boolean handleNetworkOpPacket(Packet packet, NetworkOp op) {
        return networkService.handlePacket(packet, op);
    }

    public boolean sendToBus(Envelope envelope) {
        return networkService.sendToBus(envelope);
    }

    public void suspend(Envelope envelope) {
        networkService.suspend(envelope);
    }

    public boolean registerSensorStatusListener(String sensorId, SensorStatusListener listener) {
        listeners.putIfAbsent(sensorId, new ArrayList<>());
        if(!listeners.get(sensorId).contains(listener)) {
            listeners.get(sensorId).add(listener);
        }
        return true;
    }

    public boolean unregisterSensorStatusListener(String sensorId, SensorStatusListener listener) {
        if(listeners.get(sensorId)!=null) {
            listeners.get(sensorId).remove(listener);
        }
        return true;
    }

    public boolean shutdown() {
        // TODO: Add loop with checks
        Collection<Sensor> sensors = activeSensors.values();
        for(final Sensor s : sensors) {
            LOG.info("Beginning Shutdown of sensor "+s.getClass().getName());
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    s.shutdown();
                    activeSensors.remove(s.getClass().getName());
                }
            }).start();
        }
        return true;
    }
}
