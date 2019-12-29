package io.onemfive.network.ops;

import io.onemfive.core.RequestReply;
import io.onemfive.data.Request;
import io.onemfive.data.Response;
import io.onemfive.network.sensors.SensorManager;

import java.util.logging.Logger;

/**
 * Handle a ping request
 */
public class PingRequestOp extends NetworkOpBase implements RequestReply {

    private static final Logger LOG = Logger.getLogger(PingResponseOp.class.getName());

    public PingRequestOp(SensorManager sensorManager) {
        super(sensorManager);
    }

    @Override
    public Response operate(Request request) {
        Response response = new Response(request.getId());
//        LOG.info("Received PeerStatus request...");
//        peerManager.reliablesFromRemotePeer(request.getFromPeer(), request.getReliablePeers());
//        request.setResponding(true);
//        request.setReliablePeers(peerManager.getReliablesToShare(peerManager.getLocalPeer()));
//        LOG.info("Sending response to PeerStatus request...");
//        routeOut(new ResponsePacket(request, peerManager.getLocalPeer(), request.getFromPeer(), StatusCode.OK, request.getId()));
        return response;
    }
}
