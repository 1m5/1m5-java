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
package io.onemfive.network.ops;

import io.onemfive.network.sensors.SensorManager;

import java.util.logging.Logger;

/**
 * Handle a ping request
 */
public class PingRequestOp extends NetworkOpBase {

    private static final Logger LOG = Logger.getLogger(PingResponseOp.class.getName());

    public PingRequestOp(SensorManager sensorManager) {
        super(sensorManager);
    }

    public OpsPacket operate(OpsPacket packet) {
//        LOG.info("Received PeerStatus request...");
//        peerManager.reliablesFromRemotePeer(request.getFromPeer(), request.getReliablePeers());
//        request.setResponding(true);
//        request.setReliablePeers(peerManager.getReliablesToShare(peerManager.getLocalPeer()));
//        LOG.info("Sending response to PeerStatus request...");
//        routeOut(new ResponsePacket(request, peerManager.getLocalPeer(), request.getFromPeer(), StatusCode.OK, request.getId()));
        return packet;
    }
}
