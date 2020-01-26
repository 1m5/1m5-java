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
package io.onemfive.network.sensors.fullspectrum;

import io.onemfive.data.Envelope;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.Packet;
import io.onemfive.network.peers.P2PRelationship;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorSession;

import java.util.Properties;
import java.util.logging.Logger;

import static io.onemfive.network.sensors.SensorStatus.NETWORK_CONNECTED;


/**
 * Wrapper for GNU Radio.
 * https://www.gnuradio.org
 * GNU Radio is licensed under the GPLv3 as of August 2019.
 *
 * @author objectorange
 */
public class FullSpectrumRadioSensor extends BaseSensor {

    private Logger LOG = Logger.getLogger(FullSpectrumRadioSensor.class.getName());

    public FullSpectrumRadioSensor() {
        super(new NetworkPeer(Network.FSRadio));
    }

    public FullSpectrumRadioSensor(SensorManager sensorManager) {
        super(sensorManager, new NetworkPeer(Network.FSRadio));
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

    @Override
    public SensorSession establishSession(NetworkPeer peer, Boolean autoConnect) {
        return null;
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
    public boolean sendIn(Envelope envelope) {
        return super.sendIn(envelope);
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
     */
    public void messageAvailable(SensorSession session) {
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
    public boolean start(Properties properties) {
        updateStatus(NETWORK_CONNECTED);
        return true;
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
}
