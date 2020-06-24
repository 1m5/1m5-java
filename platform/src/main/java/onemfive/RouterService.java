package onemfive;

import org.neo4j.kernel.api.exceptions.Status;
import ra.common.*;
import ra.common.messaging.MessageProducer;
import ra.common.network.NetworkPacket;
import ra.common.network.NetworkPeer;
import ra.common.network.NetworkState;
import ra.common.network.NetworkStatus;
import ra.common.service.BaseService;
import ra.common.service.NetworkService;
import ra.common.service.ServiceStatusListener;

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
 */
public final class RouterService extends BaseService {

    private static Logger LOG = Logger.getLogger(RouterService.class.getName());

    private Map<String, NetworkState> networks = new HashMap<>();

    private Long manCon0TestLastSucceeded = 0L; // NEO
    private Long manCon1TestLastSucceeded = 0L; // Extreme
    private Long manCon2TestLastSucceeded = 0L; // Very High
    private Long manCon3TestLastSucceeded = 0L; // High
    private Long manCon4TestLastSucceeded = 0L; // Medium
    private Long manCon5TestLastSucceeded = 0L; // Low

    public RouterService() {}

    public RouterService(MessageProducer producer, ServiceStatusListener listener) {
        super(producer, listener);
    }

    public Tuple2<String,NetworkPeer> selectNetworkAndToPeer(NetworkPacket packet) {
        Tuple2<String, NetworkPeer> selected = null;
        URL url = packet.getEnvelope().getURL();
        boolean isWebRequest = url != null && url.toString().startsWith("http");
        if(isWebRequest) {
            switch (ManCon.fromSensitivity(packet.getSensitivity())) {
                case LOW: {}
                case MEDIUM: {}
                case HIGH: {
                    //   Web: I2P for .i2p addresses and Tor for the rest
//                    if (url.toString().endsWith(".i2p")) {
//                        if(isNetworkReady(Network.I2P))
//                            selected = activeNetworks.get(I2PSensor.class.getName());
//                        else
//                            selected = findRelay(packet);
//                    } else {
//                        if(isNetworkReady(Network.TOR))
//                            selected = activeNetworks.get(TORSensor.class.getName());
//                        else
//                            selected = findRelay(packet);
//                    }
                    break;
                }
                case VERYHIGH: {
                    // VERYHIGH:
                    //   Web: I2P with random delays to Tor Peer at a lower ManCon, 1DN escalation
                    //   Web: I2P for .i2p addresses and Tor for the rest
//                    if(isNetworkReady(Network.I2P))
//                        selected = activeNetworks.get(I2PSensor.class.getName());
//                    else
//                        selected = findRelay(packet);
                    packet.setDelayed(true);
                    packet.setMinDelay(4 * 1000L);
                    packet.setMaxDelay(10 * 1000L);
                    break;
                }
                case EXTREME: {
                    // EXTREME:
                    //   Web: 1DN to Tor peer
//                    selected = findRelay(packet);
                    break;
                }
                case NEO: {
                    // NEO:
                    //   Web: 1DN to I2P peer with high delays to Tor peer
//                    selected = findRelay(packet);
                    packet.setDelayed(true);
                    packet.setMinDelay(60 * 1000L);
                    packet.setMaxDelay(2 * 60 * 1000L);
                    break;
                }
            }
        } else {
            switch (ManCon.fromSensitivity(packet.getSensitivity())) {
                case LOW: {}
                case MEDIUM: {}
                case HIGH: {
                    // LOW|MEDIUM|HIGH:
                    //   P2P: I2P, Tor as Tunnel when I2P blocked, 1DN escalation
//                    if(isNetworkReady(Network.I2P))
//                        selected = activeNetworks.get(I2PSensor.class.getName());
//                    else
//                        selected = findRelay(packet);
                    break;
                }
                case VERYHIGH: {
                    // VERYHIGH:
                    //   P2P: I2P with random delays, Tor as tunnel when I2P blocked, 1DN escalation\
//                    if(isNetworkReady(Network.I2P))
//                        selected = activeNetworks.get(I2PSensor.class.getName());
//                    else
//                        selected = findRelay(packet);
                    packet.setDelayed(true);
                    packet.setMinDelay(4 * 1000L);
                    packet.setMaxDelay(10 * 1000L);
                    break;
                }
                case EXTREME: {
                    // EXTREME:
                    //   P2P: 1DN to I2P peer
//                    selected = findRelay(packet);
                    break;
                }
                case NEO: {
                    // NEO:
                    //   P2P: 1DN to random number/combination of 1DN/I2P peers at random delays up to 90 seconds for I2P layer and up to
                    //     3 months for 1M5 layer. A random number of copies (3 min/12 max) sent out with only 12 word mnemonic passphrase
                    //     as key.
//                    selected = findRelay(packet);
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
//        if(activeNetworks.get(BluetoothSensor.class.getName())!=null
//                && activeNetworks.get(BluetoothSensor.class.getName()).getStatus()== SensorStatus.NETWORK_CONNECTED) {
//            io.onemfive.data.ManConStatus.MAX_AVAILABLE_MANCON = io.onemfive.data.ManCon.EXTREME;
//        } else if(activeNetworks.get(I2PSensor.class.getName())!=null
//                && activeNetworks.get(I2PSensor.class.getName()).getStatus()==SensorStatus.NETWORK_CONNECTED) {
//            io.onemfive.data.ManConStatus.MAX_AVAILABLE_MANCON = io.onemfive.data.ManCon.HIGH;
//        } else if(activeNetworks.get(TORSensor.class.getName())!=null
//                && activeNetworks.get(TORSensor.class.getName()).getStatus()==SensorStatus.NETWORK_CONNECTED) {
//            io.onemfive.data.ManConStatus.MAX_AVAILABLE_MANCON = io.onemfive.data.ManCon.LOW;
//        } else {
//            ManConStatus.MAX_AVAILABLE_MANCON = io.onemfive.data.ManCon.NONE;
//        }
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
//        for(ManConStatusListener listener : manConStatusListeners) {
//            listener.statusUpdated();
//        }
    }

//    public NetworkService findRelay(NetworkPacket packet) {
//        String service = null;
//        ManCon minManCon = ManCon.fromSensitivity(packet.getSensitivity());
//        List<String> escalation = (minManCon == io.onemfive.data.ManCon.LOW) || (minManCon == ManCon.MEDIUM) ? torSensorEscalation
//                : (minManCon == ManCon.HIGH) || (minManCon == io.onemfive.data.ManCon.VERYHIGH) ? i2pSensorEscalation : idnSensorEscalation;
//        for(String network : escalation) {
//            if(isPathAvailable(network)) {
//                packet.setToPeer(getPath(network));
//                sensor = getSensor(network);
//                break;
//            }
//        }
//        return sensor;
//    }

//    public Sensor getConnected1DNSensor() {
//        Sensor sensor = null;
//        if(isNetworkReady(Network.Bluetooth))
//            sensor = activeNetworks.get(BluetoothSensor.class.getName());
//        else if(isNetworkReady(Network.WiFiDirect))
//            sensor = activeNetworks.get(WiFiDirectSensor.class.getName());
//        else if(isNetworkReady(Network.Satellite))
//            sensor = activeNetworks.get(SatelliteSensor.class.getName());
//        else if(isNetworkReady(Network.FSRadio))
//            sensor = activeNetworks.get(FullSpectrumRadioSensor.class.getName());
//        else if(isNetworkReady(Network.LiFi))
//            sensor = activeNetworks.get(LiFiSensor.class.getName());
//        return sensor;
//    }

    /**
     * Is there at least one sensor connected for the provided ManCon level?
     * @param minManCon
     * @return
     */
//    public Boolean availableSensorConnected(ManCon minManCon) {
//        switch (minManCon) {
//            case LOW: {}
//            case MEDIUM: {}
//            case HIGH: {
//                if(isNetworkReady(Network.TOR)
//                        || isNetworkReady(Network.I2P)
//                        || isNetworkReady(Network.Bluetooth)
//                        || isNetworkReady(Network.WiFiDirect)
//                        || isNetworkReady(Network.Satellite)
//                        || isNetworkReady(Network.FSRadio)
//                        || isNetworkReady(Network.LiFi))
//                    return true;
//            }
//            case VERYHIGH: {}
//            case EXTREME: {}
//            case NEO: {
//                if(isNetworkReady(Network.Bluetooth)
//                        || isNetworkReady(Network.WiFiDirect)
//                        || isNetworkReady(Network.Satellite)
//                        || isNetworkReady(Network.FSRadio)
//                        || isNetworkReady(Network.LiFi))
//                    return true;
//            }
//        }
//        return false;
//    }

    public void updateNetworkStatus(NetworkStatus networkStatus) {
        switch (networkStatus) {
            case INITIALIZING: {
                LOG.info(this.getClass().getName() + " reporting initializing....");
                break;
            }
            case STARTING: {
                LOG.info(this.getClass().getName() + " reporting starting up....");
                break;
            }
            case WAITING: {
                LOG.info(this.getClass().getName() + " reporting waiting....");
                break;
            }
            case NETWORK_UNAVAILABLE: {
                LOG.info(this.getClass().getName() + " reporting network unavailable....");
                break;
            }
            case NETWORK_WARMUP: {
                LOG.info(this.getClass().getName() + " reporting network warming up....");
                break;
            }
            case NETWORK_PORT_CONFLICT: {
                LOG.info(this.getClass().getName() + " reporting port conflict....");
                break;
            }
            case NETWORK_CONNECTING: {
                LOG.info(this.getClass().getName() + " reporting connecting....");
                break;
            }
            case NETWORK_CONNECTED: {
                LOG.info(this.getClass().getName() + " reporting connected.");
//                if(sensorBlocks.get(sensorID)!=null) {
//                    sensorBlocks.remove(sensorID);
//                }
                break;
            }
            case NETWORK_STOPPING: {
                LOG.info(this.getClass().getName() + " reporting stopping....");
                break;
            }
            case NETWORK_STOPPED: {
                LOG.info(this.getClass().getName() + " reporting stopped.");
//                if(activeNetworks.containsKey(sensorID)) {
//                    // Active Sensor Stopped, attempt to restart
//                    Sensor sensor = activeNetworks.get(sensorID);
//                    if(sensor.restart()) {
//                        LOG.info(sensorID+" restarted after disconnection.");
//                    }
//                }
                break;
            }
            case NETWORK_BLOCKED: {
                LOG.info(this.getClass().getName() + " reporting blocked.");
                long now = System.currentTimeMillis();
//                sensorBlocks.putIfAbsent(sensorID, now);
//                if((now - sensorBlocks.get(sensorID)) > MAX_BLOCK_TIME_BETWEEN_RESTARTS) {
//                    LOG.warning(sensorID + " reporting blocked longer than "+(MAX_BLOCK_TIME_BETWEEN_RESTARTS/60000)+" minutes. Restarting...");
//                    // Active Sensor Blocked, attempt to restart
//                    activeNetworks.get(sensorID).restart();
//                    // Reset blocked start time
//                    sensorBlocks.put(sensorID, now);
//                }
                break;
            }
            case NETWORK_ERROR: {
                LOG.info(this.getClass().getName() + " reporting network error.");
                break;
            }
            case PAUSING: {
                LOG.info(this.getClass().getName() + " reporting pausing....");
                // TODO: Persist messages to this sensor until unpaused then replay in order.
                break;
            }
            case PAUSED: {
                LOG.info(this.getClass().getName() + " reporting paused....");
                break;
            }
            case UNPAUSING: {
                LOG.info(this.getClass().getName() + " reporting unpausing....");
                // TODO: Replay any paused messages in order while resuming normal operations
                break;
            }
            case SHUTTING_DOWN: {
                LOG.info(this.getClass().getName() + " reporting shutting down....");
//                activeNetworks.remove(sensorID);
                break;
            }
            case GRACEFULLY_SHUTTING_DOWN: {
                LOG.info(this.getClass().getName() + " reporting gracefully shutting down....");
//                activeNetworks.remove(sensorID);
                break;
            }
            case SHUTDOWN: {
                LOG.info(this.getClass().getName() + " reporting shutdown.");
                break;
            }
            case GRACEFULLY_SHUTDOWN: {
                LOG.info(this.getClass().getName() + " reporting gracefully shutdown.");
                break;
            }
            case RESTARTING: {
                LOG.info(this.getClass().getName() + " reporting restarting....");
                break;
            }
            case ERROR: {
                LOG.info(this.getClass().getName() + " reporting error. Initiating hard restart...");
//                Sensor s = activeNetworks.get(sensorID);
//                // Give stopping sensors a chance to clean up anything possible
//                activeNetworks.remove(sensorID);
//                s.gracefulShutdown();
                // Regardless if it succeeds or not, replace it with a new instance and start it up
//                try {
//                    s = (Sensor)Class.forName(sensorID).getConstructor().newInstance();
//                    if(s.start(networkService.getProperties())) {
//                        activeNetworks.put(sensorID, s);
//                    } else {
//                        LOG.warning("Unable to hard restart sensor: "+sensorID);
//                    }
//                } catch (Exception e) {
//                    LOG.warning("Unable to create new instance of sensor for hard restart: "+sensorID);
//                }
                break;
            }
            default: LOG.warning("Sensor Status for sensor "+this.getClass().getName()+" not being handled: "+networkStatus.name());
        }
        // Now update the Service's status based on the this Sensor's status
//        networkService.determineStatus(networkStatus);

        // Now update Man Con status
        determineManConAvailability();
    }

//    public void registerNetwork(Sensor sensor) {
//        registeredNetworks.put(sensor.getClass().getName(), sensor);
//    }
//
//    public Map<String, Sensor> getRegisteredNetworks() {
//        return registeredNetworks;
//    }
//
//    public Map<String, Sensor> getActiveNetworks() {
//        return activeNetworks;
//    }
//
//    public Map<String, Sensor> getBlockedNetworks(){
//        return blockedNetworks;
//    }

//    public boolean isNetworkReady(String network) {
//        switch (network) {
//            case "HTTPS": return SensorStatus.NETWORK_CONNECTED == getNetworkStatus(ClearnetSensor.class.getName());
//            case "TOR": return SensorStatus.NETWORK_CONNECTED == getNetworkStatus(TORSensor.class.getName());
//            case "I2P": return SensorStatus.NETWORK_CONNECTED == getNetworkStatus(I2PSensor.class.getName());
//            case "BT": return SensorStatus.NETWORK_CONNECTED == getNetworkStatus(BluetoothSensor.class.getName());
//            case "WD": return SensorStatus.NETWORK_CONNECTED == getNetworkStatus(WiFiDirectSensor.class.getName());
//            case "Sat": return SensorStatus.NETWORK_CONNECTED == getNetworkStatus(SatelliteSensor.class.getName());
//            case "Rad": return SensorStatus.NETWORK_CONNECTED == getNetworkStatus(FullSpectrumRadioSensor.class.getName());
//            case "LF": return SensorStatus.NETWORK_CONNECTED == getNetworkStatus(LiFiSensor.class.getName());
//            default: return false;
//        }
//    }
//
//    public boolean isPathAvailable(String network) {
//        return isNetworkReady(network) && peerManager.getRandomPeer(network)!=null;
//    }
//
//    public NetworkPeer getPath(String network) {
//        return peerManager.getRandomPeer(network);
//    }
//
//    public ServiceStatus getNetworkStatus(String network) {
//        NetworkState s = networks.get(network);
//        if(s == null) {
//            return ServiceStatus.
//        } else {
//            return s.getStatus();
//        }
//    }
//
//    public boolean startSensor(String sensorName) {
//        Sensor registeredSensor = registeredNetworks.get(sensorName);
//        if(registeredSensor==null) {
//            LOG.warning(sensorName+" not registered.");
//            return false;
//        }
//        Sensor activeSensor = activeNetworks.get(sensorName);
//        if(activeSensor==null) {
//            if(registeredSensor.start(properties)) {
//                activeNetworks.put(sensorName, registeredSensor);
//                return true;
//            }
//        } else if(activeSensor.getStatus()==SensorStatus.NOT_INITIALIZED
//                || activeSensor.getStatus()==SensorStatus.SHUTDOWN
//                || activeSensor.getStatus()==SensorStatus.GRACEFULLY_SHUTDOWN) {
//            return activeSensor.start(properties);
//        } else {
//            LOG.warning(sensorName+" not ready for starting.");
//        }
//        return false;
//    }
//
//    public boolean stopSensor(String sensorName, boolean hardStop) {
//        Sensor activeSensor = activeNetworks.get(sensorName);
//        if(activeSensor==null) {
//            LOG.warning(sensorName+" not registered.");
//            return true;
//        }
//        if(activeSensor.getStatus()==SensorStatus.SHUTTING_DOWN
//                || activeSensor.getStatus()==SensorStatus.GRACEFULLY_SHUTTING_DOWN) {
//            return true;
//        }
//        if(hardStop) {
//            if (activeSensor.shutdown()) {
//                activeNetworks.remove(sensorName);
//                LOG.info(sensorName + " stopped and deactivated.");
//                return true;
//            }
//        } else {
//            if (activeSensor.gracefulShutdown()) {
//                activeNetworks.remove(sensorName);
//                LOG.info(sensorName + " gracefully stopped and deactivated.");
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean startBluetoothDiscovery() {
//        BluetoothSensor sensor = (BluetoothSensor) getActiveNetworks().get(BluetoothSensor.class.getName());
//        return sensor.startDeviceDiscovery();
//    }
//
//    public boolean stopBluetoothDiscovery() {
//        BluetoothSensor sensor = (BluetoothSensor) getActiveNetworks().get(BluetoothSensor.class.getName());
//        return sensor.stopDeviceDiscovery();
//    }
//
//    public boolean isActive(String sensorName) {
//        return activeNetworks.containsKey(sensorName);
//    }
//
//    public File getSensorDirectory(String sensorName) {
//        return new File(networkService.getSensorsDirectory(), sensorName);
//    }
//
//    public boolean sendToBus(Envelope envelope) {
//        return networkService.sendToBus(envelope);
//    }
//
//    public void suspend(Envelope envelope) {
//        networkService.suspend(envelope);
//    }
//
//    public static void registerManConStatusListener(ManConStatusListener listener) {
//        manConStatusListeners.add(listener);
//    }
//
//    public static void removeManConStatusListener(ManConStatusListener listener) {
//        manConStatusListeners.remove(listener);
//    }
//
//    public boolean shutdown() {
//        // TODO: Add loop with checks
//        Collection<Sensor> sensors = activeNetworks.values();
//        for(final Sensor s : sensors) {
//            LOG.info("Beginning Shutdown of sensor "+s.getClass().getName());
//            new AppThread(new Runnable() {
//                @Override
//                public void run() {
//                    s.shutdown();
//                    activeNetworks.remove(s.getClass().getName());
//                }
//            }).start();
//        }
//        return true;
//    }
}
