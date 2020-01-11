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
package io.onemfive.network.sensors.radio.technologies.bluetooth;

import io.onemfive.data.Request;
import io.onemfive.data.content.JSON;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;
import io.onemfive.util.RandomUtil;
import io.onemfive.network.sensors.radio.BaseRadioSession;
import io.onemfive.network.sensors.radio.Radio;
import io.onemfive.network.sensors.radio.RadioDatagram;
import io.onemfive.network.sensors.radio.RadioPeer;

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
    public RadioDatagram toRadioDatagram(Request request) {
        RadioDatagram datagram = new RadioDatagram();
        datagram.content = new JSON(JSONPretty.toPretty(JSONParser.toString(request.toMap()), 4).getBytes());
        datagram.to = (RadioPeer)request.getToPeer();
        datagram.from = (RadioPeer)request.getFromPeer();
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
