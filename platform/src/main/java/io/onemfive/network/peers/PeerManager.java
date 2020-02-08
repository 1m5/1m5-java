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
package io.onemfive.network.peers;

import io.onemfive.data.AuthNRequest;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkNode;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.peers.db.PeerDB;
import io.onemfive.network.Request;
import io.onemfive.network.Response;
import io.onemfive.network.peers.graph.GraphDB;
import io.onemfive.network.*;
import io.onemfive.network.peers.graph.P2PRelationship;
import io.onemfive.util.FileUtil;
import io.onemfive.util.RandomUtil;
import io.onemfive.util.tasks.TaskRunner;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;

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

    public static final Label PEER_LABEL = Label.label("Peer");

    public static final String PEER_GRAPH = "peerGraph";
    public static final String PEER_DB = "peerDB";

    private Properties properties;

    protected NetworkService service;

    private NetworkNode localNode = new NetworkNode();

    protected TaskRunner taskRunner;

    public static Integer MaxPeersTracked = 100000;
    public static Integer MaxPeersShared = 5;
    public static Integer MaxAcksTracked = 50;

    private GraphDB graphDB;
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
        graphDB = new GraphDB();
        String baseDir = properties.getProperty("1m5.network.peers.dir");
        if(baseDir==null) {
            try {
                baseDir = service.getServiceDirectory().getCanonicalPath();
            } catch (IOException e) {
                LOG.warning("IOException caught retrieving NetworkService's service directory.");
                return false;
            }
        }
        graphDB.setLocation(baseDir);
        graphDB.setName(PEER_GRAPH);
        if ("true".equals(properties.getProperty("1m5.peers.db.cleanOnRestart"))) {
            FileUtil.rmdir(graphDB.getLocation()+"/"+ PEER_GRAPH, false);
            LOG.info("Cleaned " + PEER_GRAPH);
        }
        if(!graphDB.init(properties)) {
            LOG.severe("Neo4JDB failed to initialize.");
            return false;
        }

        // Initialize indexes
        LOG.info("Verifying Content Indexes are present...");
        try (Transaction tx = graphDB.getGraphDb().beginTx()) {
            Iterable<IndexDefinition> definitions = graphDB.getGraphDb().schema().getIndexes(PEER_LABEL);
            if(definitions==null || ((List)definitions).size() == 0) {
                LOG.info("Peer Graph Id Index not found; creating...");
                // No Indexes...set them up
                graphDB.getGraphDb().schema().indexFor(PEER_LABEL).withName("Peer.id").on("id").create();
//                neo4jDB.getGraphDb().schema().indexFor(PEER_LABEL).withName("Peer.address").on("address").create();
//                neo4jDB.getGraphDb().schema().indexFor(PEER_LABEL).withName("Peer.network").on("network").create();
                LOG.info("Peer Graph Id Index created.");
            }
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        peerDB = new PeerDB();
        peerDB.setLocation(baseDir);
        peerDB.setName(PEER_DB);
        if ("true".equals(properties.getProperty("1m5.peers.db.cleanOnRestart"))) {
            FileUtil.rmdir(peerDB.getLocation()+"/"+ PEER_DB, false);
            LOG.info("Cleaned " + PEER_DB);
        }
        if(!peerDB.init(properties)) {
            LOG.severe("DerbyDB failed to initialize,");
            return false;
        }
        return true;
    }

    public List<NetworkPeer> findLowestLatencyPath(NetworkPeer from, NetworkPeer to) {
        return graphDB.findLowestLatencyPath(from.getId(), to.getId());
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
        return peerDB.randomPeer(network);
    }

    public NetworkPeer findPeerByAddress(String address) {
        return peerDB.loadPeerByAddress(address);
    }

    public Boolean savePeer(NetworkPeer networkPeer, boolean autoCreate) {
        boolean successful = false;
        if(networkPeer.getId()==null) {
            LOG.warning("Can not save Network Peer; id must be provided.");
            return false;
        }
        if(peerDB.savePeer(networkPeer, autoCreate) && graphDB.savePeer(networkPeer, autoCreate)) {
            NetworkPeer local1M5Peer = localNode.getNetworkPeer();
            if(local1M5Peer.getId().equals(networkPeer.getId())) {
                // This is a local peer
                localNode.addNetworkPeer(networkPeer);
                LOG.info("Local Node updated:\n\t"+localNode.toString());
                successful = true;
            } else if(localNode.getNetworkPeer(networkPeer.getNetwork()) != null
                    || peerDB.loadPeerByIdAndNetwork(local1M5Peer.getId(), networkPeer.getNetwork()) != null) {
                successful = graphDB.relateByNetwork(local1M5Peer.getId(), networkPeer.getNetwork(), networkPeer.getId()) != null;
            } else {
                LOG.warning("No local peer for network="+networkPeer.getNetwork().name()+". Unable to relate.");
            }
        }
        return successful;
    }

    public NetworkPeer loadPeer(String id) {
        return peerDB.loadPeerById(id);
    }

    public Boolean isLocal(NetworkPeer p) {
        if(localNode.getNetworkPeer().getId()!=null && localNode.getNetworkPeer().getId().equals(p.getId()))
            return true;
        else
            return false;
    }

    public Boolean isReliable(NetworkPeer p) {
        P2PRelationship r = graphDB.getNetworkRelationship(localNode.getNetworkPeer().getId(), p.getNetwork(), p.getId());
        return r != null && r.isReliable();
    }

    public long totalPeersByNetwork(String id, Network network) {
        return graphDB.numberPeersByNetwork(id, network);
    }

    public void savePeerStatusTimes(String startPeerId, Network network, String endPeerId, Long timeSent, Long timeAcknowledged) {
        graphDB.savePeerStatusTimes(startPeerId, network, endPeerId, timeSent, timeAcknowledged);
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
                if(peerDB.savePeer(localPeer, true) && graphDB.savePeer(localPeer, true)) {
                    LOG.info("Local Peer updated.");
                }
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
        } else {
            LOG.warning("Error returned from AuthNRequest: " + r.statusCode);
        }
    }

    public void updateLocalNode(NetworkPeer np) {
        if(localNode==null) {
            // First call is by local node authentication
            localNode = peerDB.loadNode(np.getId());
        }
        if(localNode.getNetworkPeer().getId()!=null) {
            // 1M5 Local Node already set so update all incoming local peers to its id
            np.setId(localNode.getNetworkPeer().getId());
        }
        try {
            if(peerDB.savePeer(np, true) && graphDB.savePeer(np, true)) {
                localNode.addNetworkPeer(np);
                LOG.info("Added to Local Node: " + np);
            }
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
    }

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
