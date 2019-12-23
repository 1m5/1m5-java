package io.onemfive.network.sensors.radio.technologies.bluetooth;

import io.onemfive.data.NetworkPeer;
import io.onemfive.data.content.Text;
import io.onemfive.data.util.RandomUtil;
import io.onemfive.network.NetworkRequest;

import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class BluetoothSession extends BaseRadioSession {

    private static final Logger LOG = Logger.getLogger(BluetoothSession.class.getName());

    private BluetoothPeer peer;
    private ClientSession clientSession;
    private boolean connected = false;

    public BluetoothSession(Radio radio) {
        super(radio);
    }

    @Override
    public RadioDatagram toRadioDatagram(NetworkRequest request) {
        RadioDatagram datagram = new RadioDatagram();
        datagram.content = new Text(request.content.getBytes());
        if(request.content!=null) {
            datagram.to = (RadioPeer)request.destination.getPeer(NetworkPeer.Network.SDR.name());
            datagram.from = (RadioPeer)request.origination.getPeer(NetworkPeer.Network.SDR.name());
        } else if(request.requestContent!=null) {
            datagram.to = (RadioPeer)request.toPeer;
            datagram.from = (RadioPeer)request.fromPeer;
            datagram.destination = (RadioPeer)request.destinationPeer;
        } else {
            LOG.warning("Must set SensorRequest.content or SensorRequest.requestContent");
            return null;
        }
        return datagram;
    }

    @Override
    public Boolean sendDatagram(RadioDatagram datagram) {
        HeaderSet hsOperation = clientSession.createHeaderSet();
        hsOperation.setHeader(HeaderSet.NAME, "1m5-msg-"+ RandomUtil.nextRandomInteger() +".txt");
        hsOperation.setHeader(HeaderSet.TYPE, "text");

        //Create PUT Operation
        Operation putOperation = null;
        OutputStream os = null;
        try {
            putOperation = clientSession.put(hsOperation);
            os = putOperation.openOutputStream();
            os.write(datagram.content.getBody());
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        } finally {
            try {
                if(os!=null)
                    os.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
            try {
                if(putOperation!=null)
                    putOperation.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        return true;
    }

    @Override
    public RadioDatagram receiveDatagram(Integer port) {

        return null;
    }

    @Override
    public boolean connect(RadioPeer radioPeer) {
        if(!(radioPeer instanceof BluetoothPeer)) {
            LOG.warning("Not BluetoothPeer.");
            return false;
        }
        connected = false;
        peer = (BluetoothPeer)radioPeer;
        try {
            clientSession = (ClientSession) Connector.open(peer.getUrl());
            HeaderSet hsConnectReply = clientSession.connect(null);
            if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
                LOG.info("Not connected.");
                return false;
            }
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        }
        connected = true;
        return true;
    }

    @Override
    public boolean disconnect() {
        if(clientSession!=null) {
            try {
                clientSession.disconnect(null);
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        return true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean close() {
        if(clientSession!=null) {
            try {
                clientSession.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }
}
