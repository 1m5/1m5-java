package onemfive.routing;

import onemfive.ManCon;
import onemfive.ManConStatus;
import ra.bluetooth.BluetoothService;
import ra.common.*;
import ra.common.messaging.MessageProducer;
import ra.common.network.Network;
import ra.common.network.NetworkPeer;
import ra.common.network.NetworkState;
import ra.common.network.NetworkStatus;
import ra.common.route.Route;
import ra.common.service.ServiceStatusObserver;
import ra.networkmanager.NetworkManagerService;
import ra.networkmanager.PeerDB;
import ra.tor.TORClientService;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Censorship-Resistant Network Manager Service
 *
 * Responsibilities:
 * 1) Maximize network availability.
 * 2) Determine what ManCon can be supported at any given time.
 * 3) Select which network should be used per envelope.
 *
 * General ManCon to Network mappings:
 *
 * LOW:
 *   Web: I2P for .i2p addresses and Tor for the rest; if not response consider the site down
 *   P2P: I2P, Tor as Tunnel when local I2P blocked, non-internet escalation (Bluetooth, WiFi, Satellite, FS Radio, ECCM, LiFi)
 * MEDIUM:
 *   Web: Same as LOW except use peers to assist if no response
 *   P2P: Same as LOW
 * HIGH:
 *   Web: I2P to Tor, non-internet to I2P/Tor escalation
 *   P2P: Same as Medium
 * VERYHIGH:
 *   Web: I2P with random delays to Tor Peer at a lower ManCon, non-internet escalation
 *   P2P: I2P with random delays, Tor as tunnel when I2P blocked, non-internet escalation
 * EXTREME:
 *   Web: non-internet to Tor peer
 *   P2P: non-internet to I2P peer
 * NEO:
 *   Web: non-internet to I2P peer with high delays to Tor peer
 *   P2P: non-internet only to random number/combination of peers at random delays up to 3 months.
 *       A random number of copies (6 min/12 max) sent out with only 12 word mnemonic passphrase as key.
 *
 */
public final class CRNetworkManagerService extends NetworkManagerService {

    private static Logger LOG = Logger.getLogger(CRNetworkManagerService.class.getName());

    private Long manCon0TestLastSucceeded = 0L; // NEO
    private Long manCon1TestLastSucceeded = 0L; // Extreme
    private Long manCon2TestLastSucceeded = 0L; // Very High
    private Long manCon3TestLastSucceeded = 0L; // High
    private Long manCon4TestLastSucceeded = 0L; // Medium
    private Long manCon5TestLastSucceeded = 0L; // Low

    public CRNetworkManagerService() {
        super();
    }

    public CRNetworkManagerService(MessageProducer producer, ServiceStatusObserver observer) {
        super(producer, observer);
    }

    public CRNetworkManagerService(MessageProducer producer, ServiceStatusObserver observer, PeerDB peerDB) {
        super(producer, observer, peerDB);
    }

    @Override
    public boolean send(Envelope envelope) {
        // Evaluate what to do based on:
        // Desired network: No Network selected | Network selected
        // Envelope Sensitivity: 0-10, 0 = no sensitivity and 10 = highest sensitivity
        // Network status': Which Networks are currently available and connected
        // Network Peer: What peers are available
        // Network Paths Available: What networks are available of each peer
        // Maximum ManCon currently available
        // Minimum ManCon required
        // Maximum ManCon supported

        // In General:
        // 1. If I'm being blocked on Tor, use I2P to get to a peer with Tor not blocked.
        // 2. If I'm being blocked on I2P, use Tor to get to a peer with I2P not blocked.
        // 3. If I'm being blocked on both Tor and I2P or the local cell tower is down, use Bluetooth Mesh to get to a peer with Tor/I2P to reach destination.
        // 4. If Bluetooth Mesh is not available but WiFi-Direct is, use it instead.
        // 5. If Bluetooth and/or WiFi-Direct are not available (e.g. both off or being locally jammed), and a LiFi receiver is available, use it to get out.
        // 6. If no LiFi receiver is available, use Full-Spectrum Radio to attempt to reach a 1M5 node with Full-Spectrum Radio active.
        // 7. If no available options exist, prompt user to determine if message should be persisted. If so, persist and try again at a later time otherwise erase it.

        SituationalAwareness sitAware = new SituationalAwareness();
        Route r = envelope.getDynamicRoutingSlip().peekAtNextRoute();
        if(r!=null) {
            String serviceRequested = r.getService();
            sitAware.desiredNetwork = getNetworkFromService(serviceRequested);
            sitAware.desiredNetworkConnected = isNetworkReady(sitAware.desiredNetwork);
        }
        sitAware.envelopeSensitivity = envelope.getSensitivity();
        sitAware.envelopeManCon = ManCon.fromSensitivity(envelope.getSensitivity());
        sitAware.envelopeSensitivityWithinMaxAvailableManCon = sitAware.envelopeManCon.ordinal() <= ManConStatus.MAX_AVAILABLE_MANCON.ordinal();
        sitAware.envelopeSensitivityWithinMinRequiredManCon = sitAware.envelopeManCon.ordinal() >= ManConStatus.MIN_REQUIRED_MANCON.ordinal();
        if(!sitAware.envelopeSensitivityWithinMaxAvailableManCon) {
            sitAware.selectedManCon = ManConStatus.MAX_AVAILABLE_MANCON;
        } else if(!sitAware.envelopeSensitivityWithinMinRequiredManCon) {
            sitAware.selectedManCon = ManConStatus.MIN_REQUIRED_MANCON;
        } else {
            sitAware.selectedManCon = sitAware.envelopeManCon;
        }
        URL url = envelope.getURL();
        sitAware.isWebRequest = url != null && url.toString().startsWith("http");
        boolean pathResolved = false;
        if(sitAware.isWebRequest) {
            // Web Request
            switch (sitAware.selectedManCon) {
                case NONE: {}
                case LOW: {}
                case MEDIUM: {
                    //   Web: I2P for .i2p addresses and Tor for the rest
                    if (url.toString().endsWith(".i2p")) {
                        // I2P address so let us try to use I2P
                        if(isNetworkReady(Network.I2P)) {
                            // I2P network is connected
                            if(sitAware.desiredNetwork == null || sitAware.desiredNetwork == Network.I2P) {
                                // Envelope has I2P Service as the next route destination
                                if(sitAware.envelopeManCon == sitAware.selectedManCon) {
                                    // Min/Max ManCon supports using I2P for this Envelope...continue on with routing
                                    pathResolved = true;
                                } else {
                                    // Min/Max ManCon does not support using I2P for this Envelope.
                                    // Let us see if there is an escalated network available with a Peer with I2P available
                                    Network relayNetwork = firstAvailableNonInternetNetwork();
                                    NetworkPeer relayPeer = peerByFirstAvailableNonInternetNetworkWithAvailabilityOfSpecifiedNetwork(relayNetwork, Network.I2P);
                                    if(relayPeer == null) {
                                        // No relay possible at this time; let's hold onto this message and try again later
                                        if(!sendToMessageHold(envelope)) {
                                            LOG.warning("1-Failed to send envelope to hold: "+envelope.toJSON());
                                        }
                                    } else {
                                        // Found a relay peer; add as external route
                                        String relayService = getNetworkServiceFromNetwork(relayNetwork);
                                        LOG.info("Found Relay Service to meet Min/Max ManCon: "+relayService);
                                        envelope.addExternalRoute(relayService,
                                                "SEND",
                                                networkStates.get(relayNetwork.name()).localPeer,
                                                relayPeer);
                                        pathResolved = true;
                                    }
                                }
                            } else if(isNetworkReady(sitAware.desiredNetwork)) {
                                // I2P is ready yet they didn't request I2P - must be requesting a relay
                                String relayService = getNetworkServiceFromNetwork(sitAware.desiredNetwork);
                                NetworkPeer relayPeer = peerWithAvailabilityOfSpecifiedNetwork(sitAware.desiredNetwork, Network.I2P);
                                LOG.info("Found Relay Peer for desired relay Network: "+sitAware.desiredNetwork.name()+" to peer with I2P connected.");
                                envelope.addExternalRoute(relayService,
                                        "SEND",
                                        networkStates.get(sitAware.desiredNetwork.name()).localPeer,
                                        relayPeer);
                                pathResolved = true;
                            } else {
                                // Desired Network not yet connected so lets hold and retry later
                                if(!sendToMessageHold(envelope)) {
                                    LOG.warning("2-Failed to send envelope to hold: "+envelope.toJSON());
                                }
                            }
                        } else if(isNetworkReady(Network.Tor)) {
                            // I2P was not ready but Tor is so lets use Tor as a Relay to another peer that is connected to Tor
                            NetworkPeer relayPeer = peerWithAvailabilityOfSpecifiedNetwork(Network.Tor, Network.I2P);
                            LOG.info("Found Relay Peer for Tor to peer with I2P connected.");
                            envelope.addExternalRoute(TORClientService.class, "SEND", networkStates.get(Network.Tor.name()).localPeer, relayPeer);
                            pathResolved = true;
                        }
                    } else {
                        // Not a .i2p request...either .onion (Tor) or other web url - use Tor
                        if(isNetworkReady(Network.Tor)) {
                            // Connected to Tor so use Tor
                            if(sitAware.desiredNetwork==null || sitAware.desiredNetwork==Network.Tor) {

                                if(sitAware.envelopeManCon == sitAware.selectedManCon) {
                                    pathResolved = true;
                                } else {

                                }
                            } else {
                                // Tor is ready yet they didn't request Tor

                            }
                        } else if(isNetworkReady(Network.I2P)) {
                            // Tor not ready but I2P is. Use I2P to route to a peer with Tor to satisfy Http request

                        }
                    }
                    break;
                }
                case HIGH: {

                    break;
                }
                case VERYHIGH: {
                    // VERYHIGH:
                    //   Web: I2P with random delays to Tor Peer at a lower ManCon, non-internet escalation
                    //   Web: I2P for .i2p addresses and Tor for the rest

                    envelope.setDelayed(true);
                    envelope.setMinDelay(4 * 1000);
                    envelope.setMaxDelay(10 * 1000);
                    break;
                }
                case EXTREME: {
                    // EXTREME:
                    //   Web: non-internet to Tor peer

                    break;
                }
                case NEO: {
                    // NEO:
                    //   Web: non-internet to I2P peer with high delays to Tor peer

                    envelope.setDelayed(true);
                    envelope.setMinDelay(60 * 1000);
                    envelope.setMaxDelay(2 * 60 * 1000);
                    break;
                }
            }
        } else {
            // Peer-to-Peer
            switch (ManCon.fromSensitivity(envelope.getSensitivity())) {
                case LOW: {}
                case MEDIUM: {}
                case HIGH: {
                    // LOW|MEDIUM|HIGH:
                    //   P2P: I2P, Tor as Tunnel when I2P blocked, 1DN escalation

                    break;
                }
                case VERYHIGH: {
                    // VERYHIGH:
                    //   P2P: I2P with random delays, Tor as tunnel when I2P blocked, 1DN escalation\

                    envelope.setDelayed(true);
                    envelope.setMinDelay(4 * 1000);
                    envelope.setMaxDelay(10 * 1000);
                    break;
                }
                case EXTREME: {
                    // EXTREME:
                    //   P2P: 1DN to I2P peer

                    break;
                }
                case NEO: {
                    // NEO:
                    //   P2P: 1DN to random number/combination of 1DN/I2P peers at random delays up to 90 seconds for I2P layer and up to
                    //     3 months for 1M5 layer. A random number of copies (3 min/12 max) sent out with only 12 word mnemonic passphrase
                    //     as key.

                    envelope.setCopy(true);
                    envelope.setMinCopies(3);
                    envelope.setMaxCopies(12);
                    envelope.setDelayed(true);
                    envelope.setMinDelay(5 * 60 * 1000);
                    envelope.setMaxDelay(3 * 30 * 24 * 60 * 60 * 1000);
                    break;
                }
            }
        }

        return producer.send(envelope);
    }

    @Override
    protected Network selectNetwork(NetworkPeer np, Network preferredNetwork) {

        return Network.I2P;
    }

    protected NetworkPeer peerByFirstAvailableNonInternetNetwork() {
        if(getNetworkStatus(Network.Bluetooth)==NetworkStatus.CONNECTED
                && peerDB.peerWithInternetAccessAvailable(Network.Bluetooth)!=null)
            return peerDB.peerWithInternetAccessAvailable(Network.Bluetooth);
        else if(getNetworkStatus(Network.WiFi)==NetworkStatus.CONNECTED
                && peerDB.peerWithInternetAccessAvailable(Network.WiFi)!=null)
            return peerDB.peerWithInternetAccessAvailable(Network.WiFi);
        else if(getNetworkStatus(Network.Satellite)==NetworkStatus.CONNECTED
                && peerDB.peerWithInternetAccessAvailable(Network.Satellite)!=null)
            return peerDB.peerWithInternetAccessAvailable(Network.Satellite);
        else if(getNetworkStatus(Network.FSRadio)==NetworkStatus.CONNECTED
                && peerDB.peerWithInternetAccessAvailable(Network.FSRadio)!=null)
            return peerDB.peerWithInternetAccessAvailable(Network.FSRadio);
        else if(getNetworkStatus(Network.LiFi)==NetworkStatus.CONNECTED
                && peerDB.peerWithInternetAccessAvailable(Network.LiFi)!=null)
            return peerDB.peerWithInternetAccessAvailable(Network.LiFi);
        else
            return null;
    }

    protected NetworkPeer peerByFirstAvailableNonInternetNetworkWithAvailabilityOfSpecifiedNetwork(Network networkPeerAccessibleThrough, Network availableNetworkWithinPeer) {
        if(getNetworkStatus(Network.Bluetooth)==NetworkStatus.CONNECTED
                && peerDB.peerWithSpecificNetworkAvailable(Network.Bluetooth, availableNetworkWithinPeer)!=null)
            return peerDB.peerWithSpecificNetworkAvailable(Network.Bluetooth, availableNetworkWithinPeer);
        else if(getNetworkStatus(Network.WiFi)==NetworkStatus.CONNECTED
                && peerDB.peerWithSpecificNetworkAvailable(Network.WiFi, availableNetworkWithinPeer)!=null)
            return peerDB.peerWithSpecificNetworkAvailable(Network.WiFi, availableNetworkWithinPeer);
        else if(getNetworkStatus(Network.Satellite)==NetworkStatus.CONNECTED
                && peerDB.peerWithSpecificNetworkAvailable(Network.Satellite, availableNetworkWithinPeer)!=null)
            return peerDB.peerWithSpecificNetworkAvailable(Network.Satellite, availableNetworkWithinPeer);
        else if(getNetworkStatus(Network.FSRadio)==NetworkStatus.CONNECTED
                && peerDB.peerWithSpecificNetworkAvailable(Network.FSRadio, availableNetworkWithinPeer)!=null)
            return peerDB.peerWithSpecificNetworkAvailable(Network.FSRadio, availableNetworkWithinPeer);
        else if(getNetworkStatus(Network.LiFi)==NetworkStatus.CONNECTED
                && peerDB.peerWithSpecificNetworkAvailable(Network.LiFi, availableNetworkWithinPeer)!=null)
            return peerDB.peerWithSpecificNetworkAvailable(Network.LiFi, availableNetworkWithinPeer);
        else
            return null;
    }

    protected NetworkPeer peerWithAvailabilityOfSpecifiedNetwork(Network networkPeerAccessibleThrough, Network availableNetworkWithinPeer) {
        if(getNetworkStatus(networkPeerAccessibleThrough)==NetworkStatus.CONNECTED
                && peerDB.peerWithSpecificNetworkAvailable(networkPeerAccessibleThrough, availableNetworkWithinPeer)!=null) {
            return peerDB.peerWithSpecificNetworkAvailable(networkPeerAccessibleThrough, availableNetworkWithinPeer);
        }
        return null;
    }

    public void determineManConAvailability() {
        long now = System.currentTimeMillis();
        long maxGap = 5 * 60 * 1000; // 5 minutes
        if(now - manCon0TestLastSucceeded < maxGap) {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.NEO;
        } else if(now - manCon1TestLastSucceeded < maxGap) {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.EXTREME;
        } else if(now - manCon2TestLastSucceeded < maxGap) {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.VERYHIGH;
        } else if(now - manCon3TestLastSucceeded < maxGap) {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.HIGH;
        } else if(now - manCon4TestLastSucceeded < maxGap) {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.MEDIUM;
        } else if(now - manCon5TestLastSucceeded < maxGap) {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.LOW;
        } else {
            ManConStatus.MAX_AVAILABLE_MANCON = ManCon.NONE;
        }
    }

    public String getEscalatedConnectedNetworkService() {
        if(isNetworkReady(Network.Bluetooth))
            return BluetoothService.class.getName();
//        else if(isNetworkReady(Network.WiFi))
//            return WiFiService.class.getName();
//        else if(isNetworkReady(Network.Satellite))
//            return SatelliteService.class.getName();
//        else if(isNetworkReady(Network.LiFi))
//            return LiFiService.class.getName();
//        else if(isNetworkReady(Network.FSRadio))
//            return FullSpectrumRadioService.class.getName();
        return null;
    }

    /**
     * Is there at least one network connected for the provided ManCon level?
     * @param minManCon ManCon
     * @return
     */
    public Boolean availableNetworkConnected(ManCon minManCon) {
        switch (minManCon) {
            case LOW: {}
            case MEDIUM: {}
            case HIGH: {
                if(isNetworkReady(Network.Tor)
                        || isNetworkReady(Network.I2P)
                        || isNetworkReady(Network.Bluetooth)
                        || isNetworkReady(Network.WiFi)
                        || isNetworkReady(Network.Satellite)
                        || isNetworkReady(Network.FSRadio)
                        || isNetworkReady(Network.LiFi))
                    return true;
            }
            case VERYHIGH: {}
            case EXTREME: {}
            case NEO: {
                if(isNetworkReady(Network.Bluetooth)
                        || isNetworkReady(Network.WiFi)
                        || isNetworkReady(Network.Satellite)
                        || isNetworkReady(Network.FSRadio)
                        || isNetworkReady(Network.LiFi))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void updateNetworkState(Envelope envelope) {
        super.updateNetworkState(envelope);
        NetworkState networkState = (NetworkState)envelope.getContent();
        switch (networkState.networkStatus) {
            case NOT_INSTALLED: {

                break;
            }
            case WAITING: {

                break;
            }
            case FAILED: {

                break;
            }
            case HANGING: {

                break;
            }
            case PORT_CONFLICT: {

                break;
            }
            case CONNECTING: {

                break;
            }
            case CONNECTED: {

                break;
            }
            case DISCONNECTED: {

                break;
            }
            case VERIFIED: {

                break;
            }
            case BLOCKED: {

                break;
            }
            case ERROR: {

                break;
            }
            default: {

            }
        }
    }

}
