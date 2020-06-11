package io.onemfive.network.peers;

import io.onemfive.core.notification.NotificationService;
import io.onemfive.data.*;
import io.onemfive.network.peers.db.PeerDB;
import io.onemfive.network.peers.db.derby.DerbyPeerDB;
import io.onemfive.network.Request;
import io.onemfive.network.Response;
import io.onemfive.network.peers.graph.PeerGraph;
import io.onemfive.network.peers.graph.neo4j.Neo4JPeerGraph;
import io.onemfive.network.*;
import io.onemfive.network.peers.graph.neo4j.P2PRelationship;
import io.onemfive.util.*;
import io.onemfive.util.tasks.TaskRunner;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static io.onemfive.data.ServiceMessage.NO_ERROR;

/**
 * Peer Manager's responsibility is to manage the Peer DB and Graph serving all requests for information from it
 * including finding various paths among peers.
 * The Peer Manager delegates all Peer Discovery to each Sensor whom in turn reports back discovery information.
 * The Peer Database houses all Peer Information while the Peer Graph houses all Peer Relationship information.
 * Both are keyed off of the NetworkPeer.id attribute.
 * The Peer Graph uses embedded Neo4J while the Peer Database uses embedded Derby.
 */
public class PeerManager implements Runnable {

    private static final Logger LOG = Logger.getLogger(PeerManager.class.getName());

    public static final String PEER_GRAPH = "peerGraph";
    public static final String PEER_DB = "peerDB";

    private Properties properties;

    protected NetworkService service;

    private NetworkNode localNode = new NetworkNode();

    protected TaskRunner taskRunner;

    public static Integer MaxPeersTracked = 100000;
    public static Integer MaxPeersShared = 5;
    public static Integer MaxAcksTracked = 50;

    private PeerGraph peerGraph;
    private PeerDB peerDB;

    public PeerManager() {}

    public PeerManager(TaskRunner runner) {
        taskRunner = runner;
    }

    @Override
    public void run() {
        init(properties);
    }

    public Boolean init(Properties properties) {
        this.properties = properties;
        String baseDir = properties.getProperty("1m5.network.peers.dir");
        if (baseDir == null) {
            try {
                baseDir = service.getServiceDirectory().getCanonicalPath();
            } catch (IOException e) {
                LOG.warning("IOException caught retrieving NetworkService's service directory.");
                return false;
            }
        }
        if(!SystemVersion.isAndroid()) {
            LOG.info("Initializing databases...");
            peerGraph = new Neo4JPeerGraph();
            peerGraph.setLocation(baseDir);
            peerGraph.setName(PEER_GRAPH);
            if ("true".equals(properties.getProperty("1m5.peers.db.cleanOnRestart"))) {
                FileUtil.rmdir(peerGraph.getLocation() + "/" + PEER_GRAPH, false);
                LOG.info("Cleaned " + PEER_GRAPH);
            }
            if (!peerGraph.init(properties)) {
                LOG.severe("Neo4JDB failed to initialize.");
                return false;
            }

            peerDB = new DerbyPeerDB();
            peerDB.setLocation(baseDir);
            peerDB.setName(PEER_DB);
            if ("true".equals(properties.getProperty("1m5.peers.db.cleanOnRestart"))) {
                FileUtil.rmdir(peerDB.getLocation() + "/" + PEER_DB, false);
                LOG.info("Cleaned " + PEER_DB);
            }
            if (!peerDB.init(properties)) {
                LOG.severe("DerbyDB failed to initialize,");
                return false;
            }
        }
        return true;
    }

    public List<NetworkPeer> findLowestLatencyPath(NetworkPeer from, NetworkPeer to) {
        return peerGraph.findLowestLatencyPath(from.getId(), to.getId());
    }

    public NetworkNode getLocalNode() {
        return localNode;
    }

    public NetworkPeer getPeerByIdAndNetwork(NetworkPeer destinationPeer, Network network) {
        return peerDB.loadPeerByIdAndNetwork(destinationPeer.getId(), network);
    }

    public NetworkPeer getRandomPeer() {
        return getRandomPeer(Network.IMS);
    }

    public NetworkPeer getRandomPeer(Network network) {
        return peerDB.randomPeer(localNode.getNetworkPeer(network));
    }

    public NetworkPeer findPeerByAddress(String address) {
        return peerDB.loadPeerByAddress(address);
    }

    public Boolean savePeer(NetworkPeer networkPeer, boolean autoCreate) {
        if(networkPeer.getId()==null) {
            LOG.warning("Can not save Network Peer; id must be provided.");
            return false;
        }
        if(networkPeer.getNetwork()==null) {
            LOG.warning("Can not save Network Peer; network must be provided.");
            return false;
        }
        if(peerDB.savePeer(networkPeer, autoCreate) && peerGraph.savePeer(networkPeer, autoCreate)) {
            // Peer saved in DB and Graph; lets relate in graph
            NetworkPeer local1M5Peer = localNode.getNetworkPeer();
            while(local1M5Peer.getId()==null) {
                LOG.info("Local 1M5 Peer not yet set. Wait a second..");
                Wait.aSec(1);
            }
            if(local1M5Peer.getId().equals(networkPeer.getId())) {
                // This is a local peer
                localNode.addNetworkPeer(networkPeer);
                LOG.info("Local Node updated:\n\t" + localNode.toString());
                return true;
            }
            if(localNode.getNetworkPeer(networkPeer.getNetwork())==null) {
                NetworkPeer temp = peerDB.loadPeerByIdAndNetwork(local1M5Peer.getId(), networkPeer.getNetwork());
                if(temp!=null) {
                    localNode.addNetworkPeer(temp);
                } else {
                    LOG.info("Unable to relate yet, "+networkPeer.getNetwork().name()+" local peer not yet saved.");
                    return true;
                }
            }
            if(peerGraph.relateByNetwork(local1M5Peer.getId(), networkPeer.getNetwork(), networkPeer.getId()) != null) {
                LOG.info("Peers related.");
                return true;
            } else {
                LOG.warning("Unable to relate peers.");
                return false;
            }
        }
        return false;
    }

    public NetworkPeer loadPeer(String id) {
        return peerDB.loadPeerById(id);
    }

    public NetworkPeer loadPeerByIdAndNetwork(String id, Network network) {
        return peerDB.loadPeerByIdAndNetwork(id, network);
    }

    public long totalPeersByNetwork(Network network) {
        return peerDB.numberPeersByNetwork(network);
    }

    public Boolean isLocal(NetworkPeer p) {
        if(localNode.getNetworkPeer().getId()!=null && localNode.getNetworkPeer().getId().equals(p.getId()))
            return true;
        else
            return false;
    }

    public Boolean isReliable(NetworkPeer p) {
        P2PRelationship r = peerGraph.getNetworkRelationship(localNode.getNetworkPeer().getId(), p.getNetwork(), p.getId());
        return r != null && r.isReliable();
    }

    public void savePeerStatusTimes(String startPeerId, Network network, String endPeerId, Long timeSent, Long timeAcknowledged) {
        peerGraph.savePeerStatusTimes(startPeerId, network, endPeerId, timeSent, timeAcknowledged);
    }

    public void setNetworkService(NetworkService service) {
        this.service = service;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void updateLocalAuthNPeer(AuthNRequest r) {
        if (r.statusCode == NO_ERROR) {
            NetworkPeer localPeer = localNode.getNetworkPeer();
            if(r.identityPublicKey.getAddress()!=null) {
                localPeer.getDid().getPublicKey().setAddress(r.identityPublicKey.getAddress());
            }
            if(r.identityPublicKey.getFingerprint()!=null) {
                localPeer.getDid().getPublicKey().setFingerprint(r.identityPublicKey.getFingerprint());
                localPeer.setId(r.identityPublicKey.getFingerprint());
            }
            localPeer.getDid().getPublicKey().isIdentityKey(true);
            localPeer.getDid().setAuthenticated(true);
            localPeer.getDid().setVerified(true);
            localPeer.getDid().setUsername(r.alias);
            localPeer.getDid().getPublicKey().setAlias(r.identityPublicKey.getAlias());
            LOG.info("Updating Local Peer: \n\t: "+localPeer);
            try {
                if(peerDB.savePeer(localPeer, true) && peerGraph.savePeer(localPeer, true)) {
                    LOG.info("Local Peer updated.");
                    // Update Network Service Network State and publish to Model Listeners
                    service.getNetworkState().localPeer = localPeer;
                    service.updateModelListeners();
                    // Publish to Notification Service for Peer Listeners
                    // TODO: Do we still need to publish this
                    Envelope e = Envelope.eventFactory(EventMessage.Type.PEER_STATUS);
                    EventMessage em = (EventMessage)e.getMessage();
                    em.setName(Network.IMS.name());
                    em.setMessage(localPeer);
                    DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
                    service.sendToBus(e);
                }
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
        } else {
            LOG.warning("Error returned from AuthNRequest: " + r.statusCode);
        }
    }

//    public void updateLocalNode(NetworkPeer np) {
//        if(localNode==null) {
//            // First call is by local node authentication
//            localNode = peerDB.loadNode(np.getId());
//        }
//        if(localNode.getNetworkPeer().getId()!=null) {
//            // 1M5 Local Node already set so update all incoming local peers to its id
//            np.setId(localNode.getNetworkPeer().getId());
//        }
//        try {
//            if(peerDB.savePeer(np, true) && graphDB.savePeer(np, true)) {
//                localNode.addNetworkPeer(np);
//                LOG.info("Added to Local Node: " + np);
//                // Publish to Notification Service
//                Envelope e = Envelope.eventFactory(EventMessage.Type.PEER_STATUS);
//                EventMessage em = (EventMessage)e.getMessage();
//                em.setName(PeerManager.class.getName());
//                em.setMessage(np);
//                DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
//                service.sendToBus(e);
//            }
//        } catch (Exception e) {
//            LOG.warning(e.getLocalizedMessage());
//        }
//    }

    public Request buildRequest(NetworkPeer origination, NetworkPeer destination) {
        Request p = new Request();
        p.setId(String.valueOf(RandomUtil.nextRandomLong()));
        p.setOriginationPeer(origination);
        p.setDestinationPeer(destination);
        return p;
    }

    public Response buildResponse(NetworkPeer origination, NetworkPeer destination, String requestId) {
        Response p = new Response(requestId);
        p.setOriginationPeer(origination);
        p.setDestinationPeer(destination);
        return p;
    }

    public void report(NetworkPeer networkPeer) {
        LOG.info("NetworkPeer reported: "+networkPeer);
    }

    public void report(List<NetworkPeer> networkPeers) {
        for(NetworkPeer networkPeer : networkPeers){
            report(networkPeer);
        }
    }

}
