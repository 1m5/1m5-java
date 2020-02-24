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

import io.onemfive.core.notification.NotificationService;
import io.onemfive.data.*;
import io.onemfive.network.NetworkService;
import io.onemfive.network.NetworkPacket;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.network.sensors.bluetooth.BluetoothSensor;
import io.onemfive.network.sensors.clearnet.ClearnetSensor;
import io.onemfive.network.sensors.fullspectrum.FullSpectrumRadioSensor;
import io.onemfive.network.sensors.i2p.I2PSensor;
import io.onemfive.network.sensors.lifi.LiFiSensor;
import io.onemfive.network.sensors.satellite.SatelliteSensor;
import io.onemfive.network.sensors.tor.TORSensor;
import io.onemfive.network.sensors.wifidirect.WiFiDirectSensor;
import io.onemfive.util.AppThread;
import io.onemfive.util.DLC;
import io.onemfive.util.Wait;
import io.onemfive.util.tasks.TaskRunner;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * Sensor Manager's Responsibilities:
 * 1) Maximize sensor network availability.
 * 2) Determine what ManCon can be supported at any given time.
 * 3) Select which sensor should be used per packet.
 *
 * General ManCon to Network mappings:
 *
 * LOW:
 *   Web: I2P for .i2p addresses and Tor for the rest
 *   P2P: I2P, Tor as Tunnel when I2P blocked, 1DN escalation
 * MEDIUM:
 *   Web: Same as LOW except use peers to assist
 *   P2P: Same as LOW
 * HIGH:
 *   Web: I2P to Tor, 1DN to I2P/Tor escalation
 *   P2P: Same as Medium
 * VERYHIGH:
 *   Web: I2P with random delays to Tor Peer at a lower ManCon, 1DN escalation
 *   P2P: I2P with random delays, Tor as tunnel when I2P blocked, 1DN escalation
 * EXTREME:
 *   Web: 1DN to Tor peer
 *   P2P: 1DN to I2P peer
 * NEO:
 *   Web: 1DN to I2P peer with high delays to Tor peer
 *   P2P: 1DN to random number/combination of 1DN/I2P peers at random delays up to 90 seconds for I2P layer and up to
 *     3 months for 1M5 layer. A random number of copies (3 min/12 max) sent out with only 12 word mnemonic passphrase
 *     as key.
 *
 * TODO: Don't directly map ManCon's to Networks. Define each ManCon by threats, conditions to be observed to identify them, and how to mitigate them.
 *
 * @author objectorange
 */
public final class SensorManager {

    private static Logger LOG = Logger.getLogger(SensorManager.class.getName());

    private final Map<String, Sensor> registeredSensors = new HashMap<>();
    private final Map<String, Sensor> activeSensors = new HashMap<>();
    private final Map<String, Sensor> blockedSensors = new HashMap<>();

    private static final List<ManConStatusListener> manConStatusListeners = new ArrayList<>();

    private Properties properties;
    private TaskRunner taskRunner;

    private PeerManager peerManager;

    private NetworkService networkService;

    private VerifySensorsRunning verifySensorsRunningTask;

    private Long manCon0TestLastSucceeded = 0L;
    private Long manCon1TestLastSucceeded = 0L;
    private Long manCon2TestLastSucceeded = 0L;
    private Long manCon3TestLastSucceeded = 0L;
    private Long manCon4TestLastSucceeded = 0L;
    private Long manCon5TestLastSucceeded = 0L;

    private final long MAX_BLOCK_TIME_BETWEEN_RESTARTS = 10 * 60 * 1000; // 10 minutes
    private Map<String,Long> sensorBlocks = new HashMap<>();

    public static List<Network> torSensorEscalation = Arrays.asList(
            Network.TOR,
            Network.I2P,
            Network.Bluetooth,
            Network.WiFiDirect,
            Network.Satellite,
            Network.FSRadio,
            Network.LiFi
    );

    public static List<Network> i2pSensorEscalation = Arrays.asList(
            Network.I2P,
            Network.TOR,
            Network.Bluetooth,
            Network.WiFiDirect,
            Network.Satellite,
            Network.FSRadio,
            Network.LiFi
    );

    public static List<Network> idnSensorEscalation = Arrays.asList(
            Network.Bluetooth,
            Network.WiFiDirect,
            Network.Satellite,
            Network.FSRadio,
            Network.LiFi
    );

    public SensorManager(TaskRunner runner) {
        taskRunner = runner;
    }

    public boolean init(final Properties properties) {
        this.properties = properties;
        registeredSensors.put(ClearnetSensor.class.getName(), new ClearnetSensor(this));
        registeredSensors.put(TORSensor.class.getName(), new TORSensor(this));
        registeredSensors.put(I2PSensor.class.getName(), new I2PSensor(this));
        registeredSensors.put(BluetoothSensor.class.getName(), new BluetoothSensor(this));
//        registeredSensors.put(WiFiDirectSensor.class.getName(), new WiFiDirectSensor(this));
//        registeredSensors.put(SatelliteSensor.class.getName(), new SatelliteSensor(this));
//        registeredSensors.put(FullSpectrumRadioSensor.class.getName(), new FullSpectrumRadioSensor(this));
//        registeredSensors.put(LiFiSensor.class.getName(), new LiFiSensor(this));
        Collection<Sensor> sensors = registeredSensors.values();
        for(final Sensor s : sensors) {
            LOG.info("Launching sensor "+s.getClass().getName());
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    if(s.start(properties))
                        activeSensors.put(s.getClass().getName(),s);
                    else
                        LOG.warning(s.getClass().getName()+" failed to start.");
                }
            }).start();
        }
        verifySensorsRunningTask = new VerifySensorsRunning(taskRunner, this);
        verifySensorsRunningTask.setDelayed(true);
        verifySensorsRunningTask.setDelayTimeMS(10 * 1000L);
        verifySensorsRunningTask.setPeriodicity(60 * 1000L);
        taskRunner.addTask(verifySensorsRunningTask);
        return true;
    }

    public Sensor selectSensorAndToPeer(NetworkPacket packet) {
        Sensor selected = null;
        URL url = packet.getEnvelope().getURL();
        boolean isWebRequest = url != null && url.toString().startsWith("http");
        if(isWebRequest) {
            switch (packet.getEnvelope().getManCon()) {
                case LOW: {}
                case MEDIUM: {}
                case HIGH: {
                    //   Web: I2P for .i2p addresses and Tor for the rest
                    if (url.toString().endsWith(".i2p")) {
                        if(isNetworkReady(Network.I2P))
                            selected = activeSensors.get(I2PSensor.class.getName());
                        else
                            selected = findRelay(packet);
                    } else {
                        if(isNetworkReady(Network.TOR))
                            selected = activeSensors.get(TORSensor.class.getName());
                        else
                            selected = findRelay(packet);
                    }
                    break;
                }
                case VERYHIGH: {
                    // VERYHIGH:
                    //   Web: I2P with random delays to Tor Peer at a lower ManCon, 1DN escalation
                    //   Web: I2P for .i2p addresses and Tor for the rest
                    if(isNetworkReady(Network.I2P))
                        selected = activeSensors.get(I2PSensor.class.getName());
                    else
                        selected = findRelay(packet);
                    packet.setDelayed(true);
                    packet.setMinDelay(4 * 1000L);
                    packet.setMaxDelay(10 * 1000L);
                    break;
                }
                case EXTREME: {
                    // EXTREME:
                    //   Web: 1DN to Tor peer
                    selected = findRelay(packet);
                    break;
                }
                case NEO: {
                    // NEO:
                    //   Web: 1DN to I2P peer with high delays to Tor peer
                    selected = findRelay(packet);
                    packet.setDelayed(true);
                    packet.setMinDelay(60 * 1000L);
                    packet.setMaxDelay(2 * 60 * 1000L);
                    break;
                }
            }
        } else {
            switch (packet.getEnvelope().getManCon()) {
                case LOW: {}
                case MEDIUM: {}
                case HIGH: {
                    // LOW|MEDIUM|HIGH:
                    //   P2P: I2P, Tor as Tunnel when I2P blocked, 1DN escalation
                    if(isNetworkReady(Network.I2P))
                        selected = activeSensors.get(I2PSensor.class.getName());
                    else
                        selected = findRelay(packet);
                    break;
                }
                case VERYHIGH: {
                    // VERYHIGH:
                    //   P2P: I2P with random delays, Tor as tunnel when I2P blocked, 1DN escalation\
                    if(isNetworkReady(Network.I2P))
                        selected = activeSensors.get(I2PSensor.class.getName());
                    else
                        selected = findRelay(packet);
                    packet.setDelayed(true);
                    packet.setMinDelay(4 * 1000L);
                    packet.setMaxDelay(10 * 1000L);
                    break;
                }
                case EXTREME: {
                    // EXTREME:
                    //   P2P: 1DN to I2P peer
                    selected = findRelay(packet);
                    break;
                }
                case NEO: {
                    // NEO:
                    //   P2P: 1DN to random number/combination of 1DN/I2P peers at random delays up to 90 seconds for I2P layer and up to
                    //     3 months for 1M5 layer. A random number of copies (3 min/12 max) sent out with only 12 word mnemonic passphrase
                    //     as key.
                    selected = findRelay(packet);
                    packet.setCopy(true);
                    packet.setMinCopies(3);
                    packet.setMaxCopies(12);
                    packet.setDelayed(true);
                    packet.setMinDelay(5 * 60 * 1000L);
                    packet.setMaxDelay(3 * 30 * 24 * 60 * 60 * 1000L);
                    break;
                }
            }
        }

        return selected;
    }

    public void determineManConAvailability() {
        // This is a temporary simple test.
        if(activeSensors.get(BluetoothSensor.class.getName())!=null
                && activeSensors.get(BluetoothSensor.class.getName()).getStatus()==SensorStatus.NETWORK_CONNECTED) {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.EXTREME;
        } else if(activeSensors.get(I2PSensor.class.getName())!=null
                && activeSensors.get(I2PSensor.class.getName()).getStatus()==SensorStatus.NETWORK_CONNECTED) {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.HIGH;
        } else if(activeSensors.get(TORSensor.class.getName())!=null
                && activeSensors.get(TORSensor.class.getName()).getStatus()==SensorStatus.NETWORK_CONNECTED) {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.LOW;
        } else {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.NONE;
        }
        // TODO: This is the real implementation but needs network ops completed
//        long now = System.currentTimeMillis();
//        long maxGap = 5 * 60 * 1000; // 5 minutes
//        if(now - manCon0TestLastSucceeded < maxGap) {
//            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.NEO;
//        } else if(now - manCon1TestLastSucceeded < maxGap) {
//            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.EXTREME;
//        } else if(now - manCon2TestLastSucceeded < maxGap) {
//            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.VERYHIGH;
//        } else if(now - manCon3TestLastSucceeded < maxGap) {
//            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.HIGH;
//        } else if(now - manCon4TestLastSucceeded < maxGap) {
//            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.MEDIUM;
//        } else if(now - manCon5TestLastSucceeded < maxGap) {
//            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.LOW;
//        } else {
//            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.NONE;
//        }
        for(ManConStatusListener listener : manConStatusListeners) {
            listener.statusUpdated();
        }
    }

    public Sensor findRelay(NetworkPacket packet) {
        Sensor sensor = null;
        ManCon minManCon = packet.getEnvelope().getManCon();
        List<Network> escalation = (minManCon == ManCon.LOW) || (minManCon == ManCon.MEDIUM) ? torSensorEscalation
                : (minManCon == ManCon.HIGH) || (minManCon == ManCon.VERYHIGH) ? i2pSensorEscalation : idnSensorEscalation;
        for(Network network : escalation) {
            if(isPathAvailable(network)) {
                packet.setToPeer(getPath(network));
                sensor = getSensor(network);
                break;
            }
        }
        return sensor;
    }

    public Sensor getSensor(Network network) {
        switch (network) {
            case HTTPS: return activeSensors.get(ClearnetSensor.class.getName());
            case TOR: return activeSensors.get(TORSensor.class.getName());
            case I2P: return activeSensors.get(I2PSensor.class.getName());
            case Bluetooth: return activeSensors.get(BluetoothSensor.class.getName());
            case WiFiDirect: return activeSensors.get(WiFiDirectSensor.class.getName());
            case Satellite: return activeSensors.get(SatelliteSensor.class.getName());
            case FSRadio: return activeSensors.get(FullSpectrumRadioSensor.class.getName());
            case LiFi: return activeSensors.get(LiFiSensor.class.getName());
            default: return null;
        }
    }

    public Sensor getConnected1DNSensor() {
        Sensor sensor = null;
        if(isNetworkReady(Network.Bluetooth))
            sensor = activeSensors.get(BluetoothSensor.class.getName());
        else if(isNetworkReady(Network.WiFiDirect))
            sensor = activeSensors.get(WiFiDirectSensor.class.getName());
        else if(isNetworkReady(Network.Satellite))
            sensor = activeSensors.get(SatelliteSensor.class.getName());
        else if(isNetworkReady(Network.FSRadio))
            sensor = activeSensors.get(FullSpectrumRadioSensor.class.getName());
        else if(isNetworkReady(Network.LiFi))
            sensor = activeSensors.get(LiFiSensor.class.getName());
        return sensor;
    }

    /**
     * Is there at least one sensor connected for the provided ManCon level?
     * @param minManCon
     * @return
     */
    public Boolean availableSensorConnected(ManCon minManCon) {
        switch (minManCon) {
            case LOW: {}
            case MEDIUM: {}
            case HIGH: {
                if(isNetworkReady(Network.TOR)
                        || isNetworkReady(Network.I2P)
                        || isNetworkReady(Network.Bluetooth)
                        || isNetworkReady(Network.WiFiDirect)
                        || isNetworkReady(Network.Satellite)
                        || isNetworkReady(Network.FSRadio)
                        || isNetworkReady(Network.LiFi))
                    return true;
            }
            case VERYHIGH: {}
            case EXTREME: {}
            case NEO: {
                if(isNetworkReady(Network.Bluetooth)
                        || isNetworkReady(Network.WiFiDirect)
                        || isNetworkReady(Network.Satellite)
                        || isNetworkReady(Network.FSRadio)
                        || isNetworkReady(Network.LiFi))
                    return true;
            }
        }
        return false;
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
            case NETWORK_UNAVAILABLE: {
                LOG.info(sensorID + " reporting network unavailable....");
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

        // Now update Man Con status
        determineManConAvailability();
    }

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    Properties getProperties() {
        return properties;
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

    public boolean isNetworkReady(Network network) {
        switch (network) {
            case HTTPS: return SensorStatus.NETWORK_CONNECTED == getSensorStatus(ClearnetSensor.class.getName());
            case TOR: return SensorStatus.NETWORK_CONNECTED == getSensorStatus(TORSensor.class.getName());
            case I2P: return SensorStatus.NETWORK_CONNECTED == getSensorStatus(I2PSensor.class.getName());
            case Bluetooth: return SensorStatus.NETWORK_CONNECTED == getSensorStatus(BluetoothSensor.class.getName());
            case WiFiDirect: return SensorStatus.NETWORK_CONNECTED == getSensorStatus(WiFiDirectSensor.class.getName());
            case Satellite: return SensorStatus.NETWORK_CONNECTED == getSensorStatus(SatelliteSensor.class.getName());
            case FSRadio: return SensorStatus.NETWORK_CONNECTED == getSensorStatus(FullSpectrumRadioSensor.class.getName());
            case LiFi: return SensorStatus.NETWORK_CONNECTED == getSensorStatus(LiFiSensor.class.getName());
            default: return false;
        }
    }

    public boolean isPathAvailable(Network network) {
        return isNetworkReady(network) && peerManager.getRandomPeer(network)!=null;
    }

    public NetworkPeer getPath(Network network) {
        return peerManager.getRandomPeer(network);
    }

    public SensorStatus getSensorStatus(String sensor) {
        Sensor s = activeSensors.get(sensor);
        if(s == null) {
            return SensorStatus.UNREGISTERED;
        } else {
            return s.getStatus();
        }
    }

    public boolean startSensor(String sensorName) {
        Sensor registeredSensor = registeredSensors.get(sensorName);
        if(registeredSensor==null) {
            LOG.warning(sensorName+" not registered.");
            return false;
        }
        Sensor activeSensor = activeSensors.get(sensorName);
        if(activeSensor==null) {
            if(registeredSensor.start(properties)) {
                activeSensors.put(sensorName, registeredSensor);
                return true;
            }
        } else if(activeSensor.getStatus()==SensorStatus.NOT_INITIALIZED
                || activeSensor.getStatus()==SensorStatus.SHUTDOWN
                || activeSensor.getStatus()==SensorStatus.GRACEFULLY_SHUTDOWN) {
            return activeSensor.start(properties);
        } else {
            LOG.warning(sensorName+" not ready for starting.");
        }
        return false;
    }

    public boolean stopSensor(String sensorName, boolean hardStop) {
        Sensor activeSensor = activeSensors.get(sensorName);
        if(activeSensor==null) {
            LOG.warning(sensorName+" not registered.");
            return true;
        }
        if(activeSensor.getStatus()==SensorStatus.SHUTTING_DOWN
                || activeSensor.getStatus()==SensorStatus.GRACEFULLY_SHUTTING_DOWN) {
            return true;
        }
        if(hardStop) {
            if (activeSensor.shutdown()) {
                activeSensors.remove(sensorName);
                LOG.info(sensorName + " stopped and deactivated.");
                return true;
            }
        } else {
            if (activeSensor.gracefulShutdown()) {
                activeSensors.remove(sensorName);
                LOG.info(sensorName + " gracefully stopped and deactivated.");
                return true;
            }
        }
        return false;
    }

    public boolean startBluetoothDiscovery() {
        BluetoothSensor sensor = (BluetoothSensor)getActiveSensors().get(BluetoothSensor.class.getName());
        return sensor.startDeviceDiscovery();
    }

    public boolean stopBluetoothDiscovery() {
        BluetoothSensor sensor = (BluetoothSensor)getActiveSensors().get(BluetoothSensor.class.getName());
        return sensor.stopDeviceDiscovery();
    }

    public boolean isActive(String sensorName) {
        return activeSensors.containsKey(sensorName);
    }

    public File getSensorDirectory(String sensorName) {
        return new File(networkService.getSensorsDirectory(), sensorName);
    }

    public boolean sendToBus(Envelope envelope) {
        return networkService.sendToBus(envelope);
    }

    public void suspend(Envelope envelope) {
        networkService.suspend(envelope);
    }

    public static void registerManConStatusListener(ManConStatusListener listener) {
        manConStatusListeners.add(listener);
    }

    public static void removeManConStatusListener(ManConStatusListener listener) {
        manConStatusListeners.remove(listener);
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
