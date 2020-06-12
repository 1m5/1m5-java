package io.onemfive.network.peers;

import io.onemfive.data.NetworkPeer;
import io.onemfive.network.NetworkTask;
import io.onemfive.network.ops.PingRequestOp;
import io.onemfive.network.sensors.Sensor;
import io.onemfive.network.sensors.SensorSession;
import io.onemfive.util.RandomUtil;
import io.onemfive.util.tasks.TaskRunner;

import java.util.logging.Logger;

public class NetworkPeerDiscovery extends NetworkTask  {

    private Logger LOG = Logger.getLogger(NetworkPeerDiscovery.class.getName());

    public NetworkPeerDiscovery(TaskRunner taskRunner, Sensor sensor) {
        super(sensor.getNetwork().name()+"NetworkPeerDiscovery", taskRunner, sensor);
        periodicity = getPeriodicity();
    }

    @Override
    public Long getPeriodicity() {
        for(NetworkPeer sp : sensor.getNetworkState().seeds) {
            // Do we have at least one reliable Peer for this Network?
            if(sp.getNetwork()==sensor.getNetwork() && peerManager.isReliable(sp)) {
                return sensor.getNetworkState().UpdateInterval * 1000L;
            }
        }
        return sensor.getNetworkState().UpdateIntervalHyper * 1000L;
    }

    @Override
    public Boolean execute() {
        LOG.info("Running Network Peer Discovery...");
        running = true;
        long totalKnown = peerManager.totalPeersByNetwork(sensor.getNetwork());
        if(totalKnown==0) {
            LOG.info("No known peers for this network yet, probably too early.");
        } else if(totalKnown==1) {
            LOG.info("Only local peer known.");
        } else if(totalKnown < sensor.getNetworkState().MaxPT) {
            LOG.info(totalKnown+" known peers less than Maximum Peers Tracked of "+ sensor.getNetworkState().MaxPT+"; continuing peer discovery...");
            NetworkPeer p = peerManager.getRandomPeer(sensor.getNetwork());
            LOG.info("Sending Peer Status Request to Known Peer...");
            if(sendPingRequest(p.getDid().getPublicKey().getAddress(), p.getPort())) {
                LOG.info("Sent Peer Status Request to Known Peer.");
            } else {
                LOG.warning("A problem occurred attempting to send out Peer Status Request.");
            }
            LOG.info("Sent Peer Status Request to Known Peer.");
        } else {
            LOG.info("Maximum Peers Tracked of "+ sensor.getNetworkState().MaxPT+" reached. No need to look for more.");
        }
        running = false;
        return true;
    }

    protected boolean sendPingRequest(String toAddress, Integer toPort) {
        PingRequestOp requestOp = new PingRequestOp();
        requestOp.id = RandomUtil.nextRandomInteger();
        requestOp.fromId = localNode.getNetworkPeer().getId();
        requestOp.fromAddress = localNode.getNetworkPeer().getDid().getPublicKey().getAddress();
        requestOp.fromNetwork = sensor.getNetwork();
        requestOp.fromNetworkFingerprint = localNode.getNetworkPeer(sensor.getNetwork()).getDid().getPublicKey().getFingerprint();
        requestOp.fromNetworkAddress = localNode.getNetworkPeer(sensor.getNetwork()).getDid().getPublicKey().getAddress();
        requestOp.toNetworkAddress = toAddress;
        requestOp.toNetworkPort = toPort;
        SensorSession session = sensor.establishSession(toAddress, true);
        session.send(requestOp);
        return true;
    }
}
