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

public class PingResponseOp extends NetworkOpBase {

    private static final Logger LOG = Logger.getLogger(PingResponseOp.class.getName());

    public PingResponseOp(SensorManager sensorManager) {
        super(sensorManager);
    }

    public Boolean operate(OpsPacket res) {
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
//            default: {
//                LOG.warning("Unhandled ResponsePacket due to unhandled Status Code: " + res.getStatusCode().name());
//                return false;
//            }
//        }
        return true;
    }
}
