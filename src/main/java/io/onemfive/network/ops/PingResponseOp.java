package io.onemfive.network.ops;

import io.onemfive.core.HandleResponse;
import io.onemfive.data.Response;
import io.onemfive.network.sensors.SensorManager;

import java.util.logging.Logger;

public class PingResponseOp extends NetworkOpBase implements HandleResponse {

    private static final Logger LOG = Logger.getLogger(PingResponseOp.class.getName());

    public PingResponseOp(SensorManager sensorManager) {
        super(sensorManager);
    }

    @Override
    public Boolean operate(Response res) {
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
