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

import io.onemfive.core.Config;
import io.onemfive.data.*;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.network.sensors.radio.tasks.TaskRunner;
import io.onemfive.network.sensors.radio.technologies.RadioDetection;
import io.onemfive.network.*;
import io.onemfive.util.DLC;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import static io.onemfive.network.sensors.SensorStatus.NETWORK_CONNECTED;

/**
 * Manages communications across the full radio electromagnetic spectrum
 * using Software Defined Radio (SDR). Defaults to GNU Radio.
 */
public class RadioSensor extends BaseSensor implements RadioSessionListener {

    private static final Logger LOG = Logger.getLogger(RadioSensor.class.getName());
    private static String LOCAL_NODE_FILE_NAME = "sdr";
    private static String SIGNALS_FILE_NAME = "signals.json";

    private Properties properties;
    private TaskRunner taskRunner;
    private NetworkPeer localNode;
    private File localNodeFile;
    private Map<String, Radio> radios = new HashMap<>();

    public RadioSensor() {
        super();
    }

    public RadioSensor(SensorManager sensorManager, Sensitivity sensitivity, Integer priority) {
        super(sensorManager, sensitivity, priority);
    }

    @Override
    public String[] getOperationEndsWith() {
        return new String[]{".sdr"};
    }

    @Override
    public String[] getURLBeginsWith() {
        return new String[]{"sdr"};
    }

    @Override
    public String[] getURLEndsWith() {
        return new String[]{".sdr"};
    }



    /**
     * Sends UTF-8 content to a Radio Peer using Software Defined Radio (SDR).
     * @param packet Envelope containing SensorRequest as data.
     *                 To DID must contain base64 encoded Radio destination key.
     * @return boolean was successful
     */
    @Override
    public boolean sendOut(Packet packet) {
        LOG.info("Sending Radio Message...");
//        Envelope envelope = packet.getEnvelope();
//        NetworkRequest request = (NetworkRequest) DLC.getData(NetworkRequest.class,envelope);
//        if(request == null){
//            LOG.warning("No SensorRequest in Envelope.");
//            request.statusCode = ServiceMessage.REQUEST_REQUIRED;
//            return false;
//        }
//        NetworkPeer toPeer = request.destination.getPeer(Network.SDR.name());
//        if(toPeer == null) {
//            LOG.warning("No Peer for Radio found in toDID while sending to Radio.");
//            request.statusCode = NetworkRequest.DESTINATION_PEER_REQUIRED;
//            return false;
//        }
//        if(!Network.SDR.name().equals((toPeer.getNetwork()))) {
//            LOG.warning("Radio requires an SDR Peer.");
//            request.statusCode = NetworkRequest.DESTINATION_PEER_WRONG_NETWORK;
//            return false;
//        }
//        NetworkPeer fromPeer = request.origination.getPeer(Network.SDR.name());
//        LOG.info("Content to send: "+request.content);
//        if(request.content == null) {
//            LOG.warning("No content found in Envelope while sending to Radio.");
//            request.statusCode = NetworkRequest.NO_CONTENT;
//            return false;
//        }

//        Radio radio = RadioSelector.determineBestRadio(toRPeer);
//        if(radio==null) {
//            LOG.warning("Unhandled issue #1 here.");
//            return false;
//        }
//        RadioSession session = radio.establishSession(toRPeer, true);
//        if(session==null) {
//            LOG.warning("Unhandled issue #2 here.");
//            return false;
//        }
//        RadioDatagram datagram = session.toRadioDatagram(request);
//        Properties options = new Properties();
//        if(session.sendDatagram(datagram)) {
//            LOG.info("Radio Message sent.");
//            return true;
//        } else {
//            LOG.warning("Radio Message sending failed.");
//            request.statusCode = NetworkRequest.SENDING_FAILED;
//            return false;
//        }
        return true;
    }

    @Override
    public boolean replyOut(Packet packet) {
        return false;
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        return super.sendIn(envelope);
    }

    @Override
    public boolean replyIn(Envelope envelope) {
        return super.replyIn(envelope);
    }

    /**
     * Will be called only if you register via addSessionListener().
     *
     * After this is called, the client should call receiveMessage(msgId).
     * There is currently no method for the client to reject the message.
     * If the client does not call receiveMessage() within a timeout period
     * (currently 30 seconds), the session will delete the message and
     * log an error.
     *
     * @param session session to notify
     * @param port message port available
     */
    @Override
    public void messageAvailable(RadioSession session, Integer port) {
//        RadioDatagram d = session.receiveDatagram(port);
//        LOG.info("Received Radio Message:\n\tFrom: " + d.from.getSDRAddress());
//        Envelope e = Envelope.eventFactory(EventMessage.Type.TEXT);
//        DID did = new DID();
//        did.addPeer(d.from);
//        e.setDID(did);
//        EventMessage m = (EventMessage) e.getMessage();
//        m.setName(d.from.getSDRFingerprint());
//        m.setMessage(d);
//        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
//        LOG.info("Sending Event Message to Notification Service...");
//        sendIn(e);
    }

    @Override
    public void connected(RadioSession session) {
        LOG.info("Radio Session reporting connection.");
        updateStatus(NETWORK_CONNECTED);
        routerStatusChanged();
    }

    /**
     * Notify the service that the session has been terminated.
     * All registered listeners will be called.
     *
     * @param session session to report disconnect to
     */
    @Override
    public void disconnected(RadioSession session) {
        LOG.info("Radio Session reporting disconnection.");
//        if(session.getRadio().disconnected())){
//            updateStatus(NETWORK_STOPPED);
//        }
//        routerStatusChanged();
    }

    /**
     * Notify the client that some throwable occurred.
     * All registered listeners will be called.
     *
     * @param session session to report error occurred
     * @param message message received describing error
     * @param throwable throwable thrown during error
     */
    @Override
    public void errorOccurred(RadioSession session, String message, Throwable throwable) {
        LOG.warning("Router says: "+message+": "+throwable.getLocalizedMessage());
        routerStatusChanged();
    }

    public void checkRouterStats() {
        LOG.info("RadioSensor status:\n\t"+getStatus().name());
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

    public NetworkPeer getLocalNode() {
        return localNode;
    }

    private boolean loadLocalNode() {
        // read the local node from its file if it exists
        if(localNodeFile==null) {
            localNodeFile = new File(getDirectory(), LOCAL_NODE_FILE_NAME);
        }
        if(!localNodeFile.exists()) {
            try {
                if(!localNodeFile.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
                return false;
            }
        }
        if(localNodeFile.length() > 0) {
//            String localNodeJSON = FileUtil.readTextFile(localNodeFile.getAbsolutePath(), 1000, true);
//            Map<String, Object> m = (Map<String, Object>) JSONParser.parse(localNodeJSON);
//            if(m!=null && m.size()>0) {
//                localNode = new RadioPeer();
//                localNode.fromMap(m);
//                LOG.info("RadioSensor Local Radio peer: " + localNode.toString());
//            }
        }
        if(localNode==null) {
            LOG.info("Requesting new RadioSensor local Radio Peer...");

        }
        return true;
    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Starting Radio Sensor...");
        updateStatus(SensorStatus.STARTING);
        try {
            this.properties = Config.loadFromClasspath("radio-sensor.config", p, false);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        radios = RadioDetection.radiosAvailable(sensorManager.getPeerReport());
        Collection<Radio> rl = radios.values();
        for(Radio r : rl) {
            if(!r.start(p)) {
                LOG.warning("Unable to start radio: "+r.getClass().getName());
            } else {
                radios.put(r.getClass().getName(), r);
            }
        }
        // TODO: for now just set as connected; we need to implement this within discovery
        updateStatus(NETWORK_CONNECTED);
        return radios.size() > 0;
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
        if(radios!=null && radios.size() > 0) {
            Collection<Radio> rl = radios.values();
            for (Radio r : rl) {
                if (!r.shutdown()) {
                    LOG.warning("Unable to shutdown radio: " + r.getClass().getName());
                } else {
                    radios.put(r.getClass().getName(), r);
                }
            }
        }
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        if(radios!=null && radios.size() > 0) {
            Collection<Radio> rl = radios.values();
            for (Radio r : rl) {
                if (!r.gracefulShutdown()) {
                    LOG.warning("Unable to gracefully shutdown radio: " + r.getClass().getName());
                } else {
                    radios.put(r.getClass().getName(), r);
                }
            }
        }
        return true;
    }
}
