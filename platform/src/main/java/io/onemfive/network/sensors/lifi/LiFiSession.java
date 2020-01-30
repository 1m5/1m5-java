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
package io.onemfive.network.sensors.lifi;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.Packet;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.sensors.BaseSession;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LiFiSession extends BaseSession {

    private static final Logger LOG = Logger.getLogger(LiFiSession.class.getName());

    private List<LiFiSessionListener> sessionListeners = new ArrayList<>();

    public LiFiSession() {
        super();
    }

    public Boolean send(Packet packet) {
        LOG.warning("LiFISession.send(Packet) not implemented.");
        return false;
    }

    public Packet receive(int msgId) {
        return null;
    }

    @Override
    public boolean open(NetworkPeer peer) {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public void handleNetworkOpPacket(Packet packet, NetworkOp op) {

    }

    public boolean connect() {
        return false;
    }

    public void addSessionListener(LiFiSessionListener listener) {
        sessionListeners.add(listener);
    }

    public void removeSessionListener(LiFiSessionListener listener) {
        sessionListeners.remove(listener);
    }
}
