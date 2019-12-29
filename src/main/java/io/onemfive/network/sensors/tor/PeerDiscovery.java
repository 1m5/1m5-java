package io.onemfive.network.sensors.tor;

import io.onemfive.core.util.tasks.TaskRunner;
import io.onemfive.data.Envelope;
import io.onemfive.data.Packet;
import io.onemfive.data.Sensitivity;
import io.onemfive.data.content.Text;
import io.onemfive.data.util.DLC;
import io.onemfive.network.NetworkService;
import io.onemfive.network.NetworkTask;
import io.onemfive.network.ops.PingRequestOp;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class PeerDiscovery extends NetworkTask {

    private static final Logger LOG = Logger.getLogger(PeerDiscovery.class.getName());

    private TorSensor sensor;
    private TorPeer localPeer;
    private Map<String, TorPeer> peers;

    public PeerDiscovery(TorPeer localPeer, Map<String, TorPeer> peers, TorSensor sensor, TaskRunner taskRunner, Properties properties, long periodicity) {
        super(PeerDiscovery.class.getName(), taskRunner, properties, periodicity);
        this.localPeer = localPeer;
        this.peers = peers;
        this.sensor = sensor;
    }

    @Override
    public Boolean execute() {
        started = true;
        if(peers != null && peers.size() > 0) {
            Collection<TorPeer> peersList = peers.values();
            for (TorPeer peer : peersList) {
                Envelope envelope = Envelope.documentFactory();
                envelope.setSensitivity(Sensitivity.MEDIUM);
                DLC.addExternalRoute(NetworkService.class, PingRequestOp.class.getName(), envelope, localPeer, peer);
                Packet packet = new Packet();
                packet.setOriginationPeer(localPeer);
                packet.setFromPeer(localPeer);
                packet.setDestinationPeer(peer);
                packet.setToPeer(peer);
                packet.setEnvelope(envelope);
                sensor.sendOut(packet);
            }
        }
        lastCompletionTime = System.currentTimeMillis();
        started = false;
        return true;
    }
}
