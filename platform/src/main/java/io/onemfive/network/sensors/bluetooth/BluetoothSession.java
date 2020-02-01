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
package io.onemfive.network.sensors.bluetooth;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.Packet;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.sensors.SensorSession;
import io.onemfive.util.RandomUtil;
import io.onemfive.network.sensors.BaseSession;

import javax.microedition.io.Connector;
import javax.obex.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

class BluetoothSession extends BaseSession {

    private static final Logger LOG = Logger.getLogger(BluetoothSession.class.getName());

    private BluetoothSensor sensor;
    private ClientSession clientSession;
    private SessionNotifier sessionNotifier;
    private RequestHandler handler;
    private Thread serverThread;
    private boolean connected = false;

    BluetoothSession(BluetoothSensor sensor) {
        this.sensor = sensor;
    }

    @Override
    public void handleNetworkOpPacket(Packet packet, NetworkOp op) {
        LOG.warning("Handling Network Op Packet no yet implemented.");
    }

    @Override
    public Boolean send(Packet packet) {
        LOG.info("Sending packet...");
        if(!connected) {
            connect();
        }
        HeaderSet hsOperation = clientSession.createHeaderSet();
        hsOperation.setHeader(HeaderSet.NAME, "1m5-msg-"+ RandomUtil.nextRandomInteger() +".txt");
        hsOperation.setHeader(HeaderSet.TYPE, "text");

        //Create PUT Operation
        Operation putOperation = null;
        OutputStream os = null;
        try {
            putOperation = clientSession.put(hsOperation);
            os = putOperation.openOutputStream();
            os.write(packet.toJSON().getBytes());
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
        LOG.info("Packet sent.");
        return true;
    }

    public Packet receive(Integer port) {
        LOG.warning("Not yet implemented.");
        return null;
    }

    @Override
    public boolean open(NetworkPeer peer) {
        LOG.info("Establishing client session...");
        try {
            clientSession = (ClientSession) Connector.open(peer.getDid().getPublicKey().getAddress());
        } catch (IOException e) {
            LOG.warning("Failed to open connection: "+e.getLocalizedMessage());
            return false;
        }
                try {
            String url = "btgoep://localhost:"+sensor.getSensorManager().getPeerManager().getLocalNode().getNetworkPeer(Network.Bluetooth).getDid().getPublicKey().getAttribute("uuid")+";name=1M5";
            LOG.info("Setting up listener on: "+url);
            sessionNotifier = (SessionNotifier) Connector.open(url);
            handler = new RequestHandler();
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        }
        serverThread = new Thread(() -> {
            while(getStatus()!= SensorSession.Status.STOPPING) {
                try {
                    sessionNotifier.acceptAndOpen(handler);
                } catch (IOException e) {
                    LOG.warning(e.getLocalizedMessage());
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        LOG.info("Session established.");
        return true;
    }

    @Override
    public boolean connect() {
        connected = false;
        if(clientSession==null) {
            if(!open(peer))
                return false;
        }
        try {
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
        if(sessionNotifier!=null) {
            try {
                sessionNotifier.close();
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        serverThread.interrupt();
        sensor.releaseSession(this);
        return true;
    }

    private static class RequestHandler extends ServerRequestHandler {

        public int onPut(Operation op) {
            LOG.info("Received operation: "+op.toString());
            try {
                HeaderSet hs = op.getReceivedHeaders();
                String name = (String) hs.getHeader(HeaderSet.NAME);
                if (name != null) {
                    LOG.info("put name:" + name);
                }

                InputStream is = op.openInputStream();

                StringBuffer buf = new StringBuffer();
                int data;
                while ((data = is.read()) != -1) {
                    buf.append((char) data);
                }

                LOG.info("got:" + buf.toString());

                op.close();
                return ResponseCodes.OBEX_HTTP_OK;
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
                return ResponseCodes.OBEX_HTTP_UNAVAILABLE;
            }
        }
    }
}
