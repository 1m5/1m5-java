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
package io.onemfive.network;

import io.onemfive.core.*;
import io.onemfive.data.AuthNRequest;
import io.onemfive.core.keyring.KeyRingService;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.notification.SubscriptionRequest;
import io.onemfive.data.route.ExternalRoute;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.util.*;
import io.onemfive.util.tasks.TaskRunner;
import io.onemfive.data.*;
import io.onemfive.data.route.Route;
import io.onemfive.data.AuthenticateDIDRequest;
import io.onemfive.data.DID;
import io.onemfive.did.DIDService;
import io.onemfive.network.sensors.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * This is the main entry point into the application by supported networks.
 * It registers all supported/configured Sensors and manages their lifecycle.
 * All Sensors' status has an effect on the NetworkService status which is
 * monitored by the ServiceBus.
 *
 *  @author objectorange
 */
public class NetworkService extends BaseService {

    private static final Logger LOG = Logger.getLogger(NetworkService.class.getName());

    public static final String OPERATION_SEND = "SEND";
    public static final String OPERATION_REPLY = "REPLY";
    public static final String OPERATION_UPDATE_LOCAL_PEER = "updateLocalDID";
    public static final String OPERATION_RECEIVE_LOCAL_AUTHN_PEER = "receiveLocalPeer";

    private SensorManager sensorManager;
    private PeerManager peerManager;
    private File sensorsDirectory;
    private Properties properties;
    private TaskRunner taskRunner;

    private ManCon manCon = ManCon.HIGH; // Default

    public NetworkService() {
        super();
    }

    public NetworkService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public void handleDocument(Envelope envelope) {
        handleAll(envelope);
    }

    @Override
    public void handleEvent(Envelope envelope) {
        handleAll(envelope);
    }

    @Override
    public void handleHeaders(Envelope envelope) {
        handleAll(envelope);
    }

    private void handleAll(Envelope e) {
        // Incoming from internal Service requesting external Service
        Route r = e.getRoute();
        switch (r.getOperation()) {
            case OPERATION_SEND : {
                // A desire to send a packet to another Peer from an internal service
                Request request = null;
                Sensor sensor = null;
                if(r instanceof ExternalRoute) {
                    ExternalRoute exRoute = (ExternalRoute)r;
                    request = peerManager.buildRequest(exRoute.getOrigination(), exRoute.getDestination());
                } else {
                    // Could be synchronous request for a resource (Tor/HTTPS)
                    request = new Request();
                }
                // Wrap Envelope with Request Packet
                request.setEnvelope(e);
                // Select the Sensor and To Peer to make the request
                sensor = sensorManager.selectSensorAndToPeer(request);
                if(sensor != null) {
                    LOG.info("Sending Request to selected Sensor...");
                    if (!sensor.sendOut(request)) {
                        Message m = e.getMessage();
                        boolean reroute = false;
                        if (m != null && m.getErrorMessages() != null && m.getErrorMessages().size() > 0) {
                            for (String err : m.getErrorMessages()) {
                                LOG.warning(err);
                                if ("BLOCKED".equals(err)) {
                                    if (e.getManCon() == ManCon.LOW) {
                                        LOG.info("Low security required. Assuming block means the site is down.");
                                        // TODO: Return 404 if HTTPS, TOR, or I2P and URL is present
                                    } else {
                                        LOG.info("Some level of security required. Re-routing through another peer.");
                                        reroute = true;
                                    }
                                }
                            }
                        }
                        // TODO: Needs re-visited
                        int maxRetries = 10;
                        int retries = 1;
                        while ((reroute || sensor.getStatus() == SensorStatus.NETWORK_BLOCKED) && retries <= maxRetries) {
                            LOG.info("Can we reroute?");
                            String fromNetwork = sensor.getClass().getName();
                            sensor = sensorManager.selectSensorAndToPeer(request);
                            if (sensor != null) {
                                // Send through escalated network
                                if(!sensor.sendOut(request)) {
                                    retries++;
                                    m = e.getMessage();
                                    reroute = false;
                                    if (m != null && m.getErrorMessages() != null && m.getErrorMessages().size() > 0) {
                                        for (String err : m.getErrorMessages()) {
                                            LOG.warning(err);
                                            if ("BLOCKED".equals(err)) {
                                                LOG.info("Some level of security required. Re-routing through another peer.");
                                                reroute = true;
                                            }
                                        }
                                    }
                                }
                            } else {
                                LOG.warning("Rerouting desired but no Sensor available for rerouting.");
                                deadLetter(e);
                                break;
                            }
                        }
                    }
                } else {
                    LOG.warning("No sensor available to send message. Dead lettering...");
                    deadLetter(e);
                }
                break;
            }
            case OPERATION_REPLY : {
                LOG.info("Replying with Envelope to requester...");
                Response response = (Response) DLC.getData(Response.class,e);
                if(response == null){
                    LOG.warning("Response required in envelope.");
                    return;
                }
                if (response.getDestinationPeer() == null) {
                    LOG.warning("Must provide a destination address when using a NetworkRequest.");
                    return;
                }
                Sensor sensor = sensorManager.selectSensorAndToPeer(response);
                sensor.sendOut(response);
                break;
            }
            case OPERATION_UPDATE_LOCAL_PEER: {
                LOG.info("Update local Peer...");
                peerManager.updateLocalNode((NetworkPeer)DLC.getData(NetworkPeer.class,e));break;
            }
            case OPERATION_RECEIVE_LOCAL_AUTHN_PEER: {
                LOG.info("Receive Local AuthN Peer...");
                peerManager.updateLocalAuthNPeer(((AuthNRequest) DLC.getData(AuthNRequest.class,e)));break;
            }
            default: {
                LOG.warning("Operation ("+r.getOperation()+") not supported. Sending to Dead Letter queue.");
                deadLetter(e);
            }
        }
    }

    public boolean sendToBus(Envelope envelope) {
        LOG.info("Sending Envelope to service bus from Network Service...");
        return producer.send(envelope);
    }

    public void suspend(Envelope envelope) {
        deadLetter(envelope);
    }

    private void routeIn(Envelope envelope) {
        LOG.info("Route In from Notification Service...");
        DID fromDid = envelope.getDID();
        LOG.info("From DID pulled from Envelope.");
        // -- Ensure saved ---
        NetworkPeer fromPeer = new NetworkPeer();
        fromPeer.setDid(fromDid);
        peerManager.savePeer(fromPeer,true);
        // ----------
        EventMessage m = (EventMessage)envelope.getMessage();
        Object msg = m.getMessage();
        if(msg instanceof NetworkPeer) {
            LOG.info("Route in NetworkPeer for update...");
            peerManager.updateLocalNode((NetworkPeer)msg);
            LOG.info("DID with I2P Addresses saved; Network Service ready for requests.");
        } else {
            LOG.warning("EnvelopeMessage message "+msg.getClass().getName()+" not handled.");
            deadLetter(envelope);
        }
    }

    /**
     * Request from an external NetworkPeer to see if this NetworkPeer is online.
     * Reply with known reliable peer addresses.
     */
//    public void pingIn(PeerStatusRequest request) {
//        LOG.info("Received PeerStatus request...");
//        peerManager.reliablesFromRemotePeer(request.getFromPeer(), request.getReliablePeers());
//        request.setResponding(true);
//        request.setReliablePeers(peerManager.getReliablesToShare(peerManager.getLocalPeer()));
//        LOG.info("Sending response to PeerStatus request...");
//        routeOut(new ResponsePacket(request, peerManager.getLocalPeer(), request.getFromPeer(), StatusCode.OK, request.getId()));
//    }

    /**
     * Response handling of ResponsePacket from earlier request.
     * @param res
     */
//    public void response(ResponsePacket res) {
//        res.setTimeReceived(System.currentTimeMillis());
//        CommunicationPacket req = res.getRequest();
//        switch (res.getStatusCode()) {
//            case OK: {
//                req.setTimeAcknowledged(System.currentTimeMillis());
//                LOG.info("Ok response received from request.");
//                if (req instanceof PeerStatusRequest) {
//                    LOG.info("PeerStatus response received from PeerStatus request.");
//                    LOG.info("Saving peer status times in graph...");
//                    if(peerManager.savePeerStatusTimes(req.getFromPeer(), req.getToPeer(), req.getTimeSent(), req.getTimeAcknowledged())) {
//                        LOG.info("Updating reliables in graph...");
//                        peerManager.reliablesFromRemotePeer(req.getToPeer(), ((PeerStatusRequest)req).getReliablePeers());
//                    }
//                } else {
//                    LOG.warning("Unsupported request type received in ResponsePacket: "+req.getClass().getName());
//                }
//                break;
//            }
//            case GENERAL_ERROR: {
//                LOG.warning("General error.");
//                break;
//            }
//            case INSUFFICIENT_HASHCASH: {
//                LOG.warning("Insufficient hashcash.");
//                break;
//            }
//            case INVALID_HASHCASH: {
//                LOG.warning("Invalid hashcash.");
//                break;
//            }
//            case INVALID_PACKET: {
//                LOG.warning("Invalid packed received by peer.");
//                break;
//            }
//            case NO_AVAILABLE_STORAGE: {
//                LOG.warning("No available storage on peer.");
//                break;
//            }
//            case NO_DATA_FOUND: {
//                LOG.warning("No data found by peer.");
//                break;
//            }
//            default:
//                LOG.warning("Unhandled ResponsePacket due to unhandled Status Code: " + res.getStatusCode().name());
//        }
//    }

    /**
     * Probe an external NetworkPeer to see if it is online sending it current reliable peers expecting to receive OK with their reliable peers (response).
     */
//    public void pingOut(NetworkPeer peerToProbe) {
//        LOG.info("Sending PeerStatus request out to peer...");
//        PeerStatusRequest ps = new PeerStatusRequest(peerManager.getLocalPeer(), peerToProbe);
//        ps.setReliablePeers(peerManager.getReliablesToShare(peerManager.getLocalPeer()));
//        routeOut(ps);
//    }

    /**
     * Send request out to peer
     * @param packet
     */
//    public void routeOut(CommunicationPacket packet) {
//        LOG.info("Routing out comm packet to Sensors Service...");
//        if(packet.getTimeSent() <= 0) {
//            // initial route out
//            packet.setTimeSent(System.currentTimeMillis());
//        }
//        String json = JSONParser.toString(packet.toMap());
//        LOG.info("Content to send: "+json);
//        Envelope e = Envelope.documentFactory();
//        // Setting Sensitivity to HIGH requests it to be routed through I2P
//        e.setSensitivity(Envelope.Sensitivity.HIGH);
//        NetworkRequest r = new NetworkRequest();
//        r.origination = packet.getFromPeer().getDid();
//        r.destination = packet.getToPeer().getDid();
//        r.content = json;
//        DLC.addData(NetworkRequest.class, r, e);
//        DLC.addRoute(NetworkService.class, NetworkService.OPERATION_SEND, e);
//        producer.send(e);
//        LOG.info("Comm packet sent.");
//    }

    /**
     * Based on supplied SensorStatus, set the SensorsService status.
     * @param sensorStatus
     */
    public void determineStatus(SensorStatus sensorStatus) {
        ServiceStatus currentServiceStatus = getServiceStatus();
        LOG.info("Current Sensors Service Status: "+currentServiceStatus+"; Inbound sensor status: "+sensorStatus.name());
        switch (sensorStatus) {
            case INITIALIZING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case STARTING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case WAITING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                else if(currentServiceStatus == ServiceStatus.STARTING)
                    updateStatus(ServiceStatus.WAITING);
                break;
            }
            case NETWORK_WARMUP: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case NETWORK_PORT_CONFLICT: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case NETWORK_CONNECTING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case NETWORK_CONNECTED: {
                if(allSensorsWithStatus(SensorStatus.NETWORK_CONNECTED)) {
                    LOG.info("All Sensors Connected to their networks, updating SensorService status to RUNNING.");
                    updateStatus(ServiceStatus.RUNNING);
                }
                break;
            }
            case NETWORK_STOPPING: {
                if(currentServiceStatus == ServiceStatus.RUNNING)
                    updateStatus(ServiceStatus.PARTIALLY_RUNNING);
                break;
            }
            case NETWORK_STOPPED: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case NETWORK_BLOCKED: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING
                        || currentServiceStatus == ServiceStatus.DEGRADED_RUNNING
                        && sensorManager.availableSensorConnected(manCon))
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                else
                    updateStatus(ServiceStatus.BLOCKED);
                break;
            }
            case NETWORK_UNAVAILABLE: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING
                        || currentServiceStatus == ServiceStatus.DEGRADED_RUNNING
                        && sensorManager.availableSensorConnected(manCon))
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                else
                    updateStatus(ServiceStatus.UNAVAILABLE);
                break;
            }
            case NETWORK_ERROR: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case PAUSING: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case PAUSED: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case UNPAUSING: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case SHUTTING_DOWN: {
                break; // Not handling
            }
            case GRACEFULLY_SHUTTING_DOWN: {
                break; // Not handling
            }
            case SHUTDOWN: {
                if(allSensorsWithStatus(SensorStatus.SHUTDOWN)) {
                    if(getServiceStatus() != ServiceStatus.RESTARTING) {
                        updateStatus(ServiceStatus.SHUTDOWN);
                    }
                }
                break;
            }
            case GRACEFULLY_SHUTDOWN: {
                if(allSensorsWithStatus(SensorStatus.GRACEFULLY_SHUTDOWN)) {
                    if(getServiceStatus() == ServiceStatus.RESTARTING) {
                        start(properties);
                    } else {
                        updateStatus(ServiceStatus.GRACEFULLY_SHUTDOWN);
                    }
                }
                break;
            }
            case RESTARTING: {
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            case ERROR: {
                if(allSensorsWithStatus(SensorStatus.ERROR)) {
                    // Major issues - all sensors error - flag for restart of Service
                    updateStatus(ServiceStatus.UNSTABLE);
                    break;
                }
                if(currentServiceStatus == ServiceStatus.RUNNING
                        || currentServiceStatus == ServiceStatus.PARTIALLY_RUNNING)
                    updateStatus(ServiceStatus.DEGRADED_RUNNING);
                break;
            }
            default: LOG.warning("Sensor Status not being handled: "+sensorStatus.name());
        }
    }

    private Boolean allSensorsWithStatus(SensorStatus sensorStatus) {
        LOG.info("Verifying all sensors with status: "+sensorStatus.name());
        Collection<Sensor> sensors = sensorManager.getRegisteredSensors().values();
        if(sensors.size() == 0) {
            return false;
        }
        for(Sensor s : sensors) {
            LOG.info(s.getClass().getName()+" status: "+s.getStatus().name());
            if(s.getStatus() != sensorStatus){
                return false;
            }
        }
        return true;
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public File getSensorsDirectory() {
        return sensorsDirectory;
    }

    @Override
    public boolean start(Properties p) {
        super.start(p);
        LOG.info("Starting Network Service...");
        updateStatus(ServiceStatus.STARTING);
        properties = p;

        // Directories
        try {
            sensorsDirectory = new File(getServiceDirectory(), "sensors");
            if(!sensorsDirectory.exists() && !sensorsDirectory.mkdir()) {
                LOG.warning("Unable to create sensors directory at: "+getServiceDirectory().getAbsolutePath()+"/sensors");
            } else {
                properties.setProperty("1m5.dir.sensors",sensorsDirectory.getCanonicalPath());
            }
        } catch (IOException e) {
            LOG.warning("IOException caught while building sensors directory: \n"+e.getLocalizedMessage());
        }

        taskRunner = new TaskRunner(4, 4);
        Thread taskRunnerThread = new Thread(taskRunner, "NetworkService-TaskRunnerThread");
        taskRunnerThread.setDaemon(true);
        taskRunnerThread.start();

        // Sensor Manager
        sensorManager = new SensorManager(taskRunner);
        sensorManager.setNetworkService(this);

        // Peer Manager
        peerManager = new PeerManager(taskRunner);
        peerManager.setNetworkService(this);
        sensorManager.setPeerManager(peerManager);
        if(peerManager.init(properties) && sensorManager.init(properties)) {
            // Local 1M5 Peer Credentials
            String username = "Alice";
            String passphrase = null;
            try {
                String credFileStr = getServiceDirectory().getAbsolutePath() + "/cred";
                File credFile = new File(credFileStr);
                if(!credFile.exists())
                    if(!credFile.createNewFile())
                        throw new Exception("Unable to create node credentials file at: "+credFileStr);

                properties.setProperty("onemfive.node.local.username",username);
                passphrase = FileUtil.readTextFile(credFileStr,1, true);

                if("".equals(passphrase) ||
                        (properties.getProperty("onemfive.node.local.rebuild")!=null && "true".equals(properties.getProperty("onemfive.node.local.rebuild")))) {
                    passphrase = HashUtil.generateHash(String.valueOf(System.currentTimeMillis()), Hash.Algorithm.SHA1).getHash();
                    if(!FileUtil.writeFile(passphrase.getBytes(), credFileStr)) {
                        LOG.warning("Unable to write local node Alice passphrase to file.");
                        return false;
                    }
                }
                properties.setProperty("onemfive.node.local.passphrase",passphrase);
            } catch (Exception ex) {
                LOG.warning(ex.getLocalizedMessage());
                return false;
            }

            // 3. Request local Peer
            Envelope e3 = Envelope.documentFactory();
            DLC.addRoute(NetworkService.class, NetworkService.OPERATION_RECEIVE_LOCAL_AUTHN_PEER, e3);
            // 2. Authenticate DID
            DID did = new DID();
            did.setUsername(username);
            did.setPassphrase(passphrase);
            AuthenticateDIDRequest adr = new AuthenticateDIDRequest();
            adr.did = did;
            adr.autogenerate = true;
            DLC.addData(AuthenticateDIDRequest.class,adr,e3);
            DLC.addRoute(DIDService.class, DIDService.OPERATION_AUTHENTICATE,e3);
            // 1. Load Public Key addresses for short and full addresses
            AuthNRequest ar = new AuthNRequest();
            ar.location = getServiceDirectory().getAbsolutePath();
            ar.keyRingUsername = username;
            ar.keyRingPassphrase = passphrase;
            ar.alias = username; // use username as default alias
            ar.aliasPassphrase = passphrase; // just use same passphrase
            ar.autoGenerate = true;

            DLC.addData(AuthNRequest.class, ar, e3);
            DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_AUTHN, e3);
            sendToBus(e3);
            updateStatus(ServiceStatus.WAITING);
            LOG.info("Network Service Started.");
        } else {
            LOG.severe("Unable to start up Network Service without Peer Manager and Sensor Manager.");
            return false;
        }
        return true;
    }

    @Override
    public boolean restart() {
        updateStatus(ServiceStatus.RESTARTING);
        gracefulShutdown();
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        if(getServiceStatus() != ServiceStatus.RESTARTING)
            updateStatus(ServiceStatus.SHUTTING_DOWN);
        sensorManager.shutdown();
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        // TODO: add wait/checks to ensure each sensor shutdowns
        return shutdown();
    }

}
