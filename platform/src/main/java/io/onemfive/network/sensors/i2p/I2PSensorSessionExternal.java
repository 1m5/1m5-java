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
package io.onemfive.network.sensors.i2p;

import io.onemfive.network.NetworkPacket;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.sensors.BaseSession;
import net.i2p.client.I2PSession;
import net.i2p.client.I2PSessionMuxedListener;

import java.util.logging.Logger;

public class I2PSensorSessionExternal extends BaseSession implements I2PSessionMuxedListener {

    private static final Logger LOG = Logger.getLogger(I2PSensorSessionExternal.class.getName());

    private I2PSensor sensor;
    private boolean connected = false;
    private String address;

    public I2PSensorSessionExternal(I2PSensor sensor) {
        super();
        this.sensor = sensor;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean open(String address) {
        this.address = address;
        LOG.warning("Not yet implemented.");
        return false;
    }

    @Override
    public boolean connect() {
        LOG.warning("Not yet implemented.");
        return false;
    }

    @Override
    public boolean disconnect() {
        LOG.warning("Not yet implemented.");
        return false;
    }

    @Override
    public boolean isConnected() {
        LOG.warning("Not yet implemented.");
        return false;
    }

    @Override
    public boolean close() {
        LOG.warning("Not yet implemented.");
        return false;
    }

    @Override
    public Boolean send(NetworkPacket packet) {
        LOG.warning("Not yet implemented.");
        return null;
    }

    @Override
    public Boolean send(NetworkOp op) {
        LOG.warning("Not yet implemented.");
        return null;
    }

    @Override
    public void messageAvailable(I2PSession i2PSession, int i, long l) {
        LOG.warning("Not yet implemented.");
    }

    @Override
    public void messageAvailable(I2PSession i2PSession, int i, long l, int i1, int i2, int i3) {
        LOG.warning("Not yet implemented.");
    }

    @Override
    public void reportAbuse(I2PSession i2PSession, int i) {
        LOG.warning("Not yet implemented.");
    }

    @Override
    public void disconnected(I2PSession i2PSession) {
        LOG.warning("Not yet implemented.");
    }

    @Override
    public void errorOccurred(I2PSession i2PSession, String s, Throwable throwable) {
        LOG.warning("Not yet implemented.");
    }
}
