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
import io.onemfive.network.Request;
import io.onemfive.network.Response;
import io.onemfive.neo4j.GraphUtil;
import io.onemfive.neo4j.Neo4jDB;
import io.onemfive.network.*;
import io.onemfive.util.FileUtil;
import io.onemfive.util.RandomUtil;
import io.onemfive.util.tasks.TaskRunner;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static io.onemfive.data.ServiceMessage.NO_ERROR;

/**
 * Peer Manager's responsibility is to manage the Peer Graph serving all requests for information from it
 * including finding various paths among peers.
 * The Peer Manager delegates all Peer Discovery to each Sensor whom it turn reports back discovery information.
 */
public class PeerManager implements Runnable {

    private static final Logger LOG = Logger.getLogger(PeerManager.class.getName());

    public static final Label PEER_LABEL = Label.label(NetworkPeer.class.getSimpleName());

    public static final String DBNAME = "peers";

    private Properties properties;

    protected NetworkService service;
    protected NetworkNode localNode = new NetworkNode();
    protected TaskRunner taskRunner;

    private static Integer MaxPeersTracked = 100000;
    private static Integer MaxPeersShared = 5;
    private static Integer MaxAcksTracked = 50;
    // Is Reliable
    private static Integer MinAcksReliablePeer = 100;
    private static Integer MaxAvgLatencyReliablePeer = 6000;
    private static Integer MaxMedLatencyReliablePeer = 6000;
    // Is Super Reliable
    private static Integer MinAcksSuperReliablePeer = 10000;
    private static Integer MaxAvgLatencySuperReliablePeer = 4000;
    private static Integer MaxMedLatencySuperReliablePeer = 4000;

    private Neo4jDB db;

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
        db = new Neo4jDB();
        String baseDir = properties.getProperty("1m5.network.peers.dir");
        if(baseDir==null) {
            try {
                baseDir = service.getServiceDirectory().getCanonicalPath();
            } catch (IOException e) {
                LOG.warning("IOException caught retrieving NetworkService's service directory.");
                return false;
            }
        }
        db.setLocation(baseDir);
        db.setName(DBNAME);
        if ("true".equals(properties.getProperty("1m5.peers.db.cleanOnRestart"))) {
            FileUtil.rmdir(db.getLocation(), false);
            LOG.info("Cleaned " + DBNAME);
        }
        db.init(properties);

        // Initialize indexes
        LOG.info("Verifying Content Indexes are present...");
        try (Transaction tx = db.getGraphDb().beginTx()) {
            Iterable<IndexDefinition> definitions = db.getGraphDb().schema().getIndexes(PEER_LABEL);
            if(definitions==null || ((List)definitions).size() == 0) {
                LOG.info("1M5 Indexes not found; creating...");
                // No Indexes...set them up
                db.getGraphDb().schema().indexFor(PEER_LABEL).withName("NetworkPeer.id").on("id").create();
                db.getGraphDb().schema().indexFor(PEER_LABEL).withName("NetworkPeer.address").on("address").create();
                db.getGraphDb().schema().indexFor(PEER_LABEL).withName("NetworkPeer.network").on("network").create();
                LOG.info("1M5 Indexes created.");
            }
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        return true;
    }

    public NetworkNode getLocalNode() {
        return localNode;
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
                localPeer.setId(r.identityPublicKey.getAddress());
                localPeer.getDid().getPublicKey().setAddress(r.identityPublicKey.getAddress());
            }
            if(r.identityPublicKey.getFingerprint()!=null)
                localPeer.getDid().getPublicKey().setFingerprint(r.identityPublicKey.getFingerprint());
            localPeer.getDid().getPublicKey().isIdentityKey(true);
            localPeer.setLocal(true);
            localPeer.getDid().setAuthenticated(true);
            localPeer.getDid().setVerified(true);
            LOG.info("Updating Local Peer: \n\t: "+localPeer);
            savePeer(localPeer, true);
        } else {
            LOG.warning("Error returned from AuthNRequest: " + r.statusCode);
        }
    }

    public void updateLocalNode(NetworkPeer np) {
        if(localNode==null) {
            localNode = new NetworkNode();
        }
        if(localNode.getNetworkPeer().getId()!=null) {
            np.setId(localNode.getNetworkPeer().getId());
        }
        np.setLocal(true);
        localNode.addNetworkPeer(np);
        savePeer(np, true);
        LOG.info("Add to Local Node: "+np);
    }

    public List<NetworkPeer> findLeastHopsPath(NetworkPeer fromPeer, NetworkPeer toPeer) {
        List<NetworkPeer> leastHopsPath = new ArrayList<>();
        try (Transaction tx = db.getGraphDb().beginTx()) {
            PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection(P2PRelationship.RelType.IMS, Direction.OUTGOING), 15);
            Node startNode = findPeerNodeByAddress(fromPeer);
            Node endNode = findPeerNodeByAddress(toPeer);
            if (startNode != null && endNode != null) {
                Path p = finder.findSinglePath(startNode, endNode);
                NetworkPeer np;
                for(Node n : p.nodes()) {
                    np = toPeer(n);
                    leastHopsPath.add(np);
                }
            }
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return leastHopsPath;
    }

    public List<NetworkPeer> findLowestLatencyPath(NetworkPeer fromPeer, NetworkPeer toPeer) {
        List<NetworkPeer> lowestLatencyPath = new ArrayList<>();
        try (Transaction tx = db.getGraphDb().beginTx()) {
            PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.forTypeAndDirection(P2PRelationship.RelType.IMS, Direction.OUTGOING), P2PRelationship.AVG_ACK_LATENCY_MS);
            Node startNode = findPeerNodeByAddress(fromPeer);
            Node endNode = findPeerNodeByAddress(toPeer);
            if (startNode != null && endNode != null) {
                WeightedPath path = finder.findSinglePath(startNode, endNode);
                double weight = path.weight();
                LOG.info("weight="+weight);
                NetworkPeer np;
                for(Node n : path.nodes()) {
                    np = toPeer(n);
                    lowestLatencyPath.add(np);
                }
            }
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return lowestLatencyPath;
    }

    /**
     * Find lowest latency path between two nodes outgoing taking into consideration:
     *      + sensors/networks supported by each node (Relationship type)
     *      + average latency (of the last n acks) of that network relationship
     *      + median latency (of the last n acks) of that network relationship
     *      + number of total acks of that network relationship (reliability)
     *      + number of nacks of that network relationship
     *
     *
     * @param fromPeer
     * @param toPeer
     * @param networks
     * @return
     */
    public List<NetworkPeer> findLowestLatencyPathFiltered(NetworkPeer fromPeer, NetworkPeer toPeer, Network[] networks) {
        CostEvaluator<Double> costEvaluator = new CostEvaluator<Double>() {
            @Override
            public Double getCost(Relationship relationship, Direction direction) {
                Long avgAckLatencyMS = (Long) relationship.getProperty(P2PRelationship.AVG_ACK_LATENCY_MS);
                Long medAckLatencyMS = (Long) relationship.getProperty(P2PRelationship.MEDIAN_ACK_LATENCY_MS);
                Long totalAcks = (Long) relationship.getProperty(P2PRelationship.TOTAL_ACKS);
                Long lastAckTime = (Long) relationship.getProperty(P2PRelationship.LAST_ACK_TIME);
//                Long costE = (Long) relationship.getEndNode().getProperty("cost");
                // Weigh median heavier than average yet ensure average is included to take into account extremes which are still important
                Double score = (medAckLatencyMS * .75) + (avgAckLatencyMS * .25);
                relationship.setProperty("score", score);
                return score;
            }
        };
        List<NetworkPeer> lowestLatencyPath = new ArrayList<>();
        try (Transaction tx = db.getGraphDb().beginTx()) {
            PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.forTypeAndDirection(P2PRelationship.RelType.IMS, Direction.OUTGOING), P2PRelationship.AVG_ACK_LATENCY_MS);
//            PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanderBuilder.allTypes(Direction.OUTGOING).)
            Node startNode = findPeerNodeByAddress(fromPeer);
            Node endNode = findPeerNodeByAddress(toPeer);
            if (startNode != null && endNode != null) {
                WeightedPath path = finder.findSinglePath(startNode, endNode);
                double weight = path.weight();
                LOG.info("Path weight: " + path.weight());
                NetworkPeer np;
                for(Node n : path.nodes()) {
                    np = toPeer(n);
                    lowestLatencyPath.add(np);
//                    LOG.info("Node: " + n.getId() + "-" +  n.getProperty("cost"));
                }
                for (Relationship r: path.relationships()) {
                    LOG.info("Relationship: " + r.getId() + "-" + r.getProperty("score") );
                }
            }
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return lowestLatencyPath;
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

    public Boolean savePeer(NetworkPeer p, Boolean autocreate) {
        LOG.info("Saving NetworkPeer...");
        if(p.getDid().getPublicKey().getAddress()==null || p.getDid().getPublicKey().getAddress().isEmpty() || p.getDid().getPublicKey().getAddress().equals("null")) {
            LOG.info("NetworkPeer to save has no Address. Skipping.");
            return false;
        }
        if(p.getId()==null || p.getId().isEmpty()) {
            if(p.getLocal() && localNode.getNetworkPeer()!=null) {
                p.setId(localNode.getNetworkPeer().getId());
            } else {
                LOG.warning("NetworkPeer.id is empty. Must have an id for remote Network Peers to save.");
                return false;
            }
        }
        boolean updated = false;
        try {
            updated = updatePeer(p);
        } catch (Exception e) {
            return false;
        }
        if(updated)
            return true;
        else if(autocreate) {
            LOG.info("Creating NetworkPeer in graph...");
            NetworkPeer localNP = localNode.getNetworkPeer(p.getNetwork());
            if(localNP==null) {
                // No Local NP for this NP's network
                if(p.getLocal()) {
                    // This will be the local NP for its network
                    localNP = p;
                }
            }
            long numberPeers = 0;
            if(localNP!=null) {
                numberPeers = totalPeersByRelationship(localNP, P2PRelationship.RelType.IMS);
            }
            // TODO: Sensors configuration be network-specific
            if(numberPeers <= MaxPeersTracked) {
                try (Transaction tx = db.getGraphDb().beginTx()) {
                    Node n = db.getGraphDb().createNode(PEER_LABEL);
                    toNode(p,n);
                    tx.success();
                    LOG.info("NetworkPeer saved to graph.");
                } catch (Exception e) {
                    LOG.warning(e.getLocalizedMessage());
                }
                if(!isLocal(p) && !isRelated(p, P2PRelationship.RelType.IMS)) {
                    LOG.info("Peer not known: relating as known.");
                    relatePeers(localNode.getNetworkPeer(), p, P2PRelationship.RelType.IMS);
                    relatePeers(localNode.getNetworkPeer(p.getNetwork()), p, P2PRelationship.networkToRelationship(p.getNetwork()));
                    LOG.info("Peers related as known.");
                }
            } else {
                LOG.info("Not adding peer; max number of peers reached: "+ MaxPeersTracked);
            }
        } else {
            LOG.info("New Peer but autocreate is false, unable to save peer.");
        }
        return true;
    }

    public NetworkNode loadNetworkNode(String id) {
        NetworkNode node = new NetworkNode();
        try (Transaction tx = db.getGraphDb().beginTx()) {
            ResourceIterator<Node> nodes = db.getGraphDb().findNodes(PEER_LABEL, "id", id);
            while (nodes.hasNext()) {
                node.addNetworkPeer(toPeer(toMap(nodes.next())));
            }
            tx.success();
        }
        return node;
    }

    /**
     * Requires to be used within a Transaction
     * @param p
     * @return
     * @throws Exception
     */
    private Node findPeerNodeByAddress(NetworkPeer p) {
        return db.getGraphDb().findNode(PEER_LABEL, "address", p.getDid().getPublicKey().getAddress());
    }

    public NetworkPeer loadPeer(NetworkPeer p) {
        if(p==null) return null;
        NetworkPeer loaded = null;
        try (Transaction tx = db.getGraphDb().beginTx()) {
            Node n = findPeerNodeByAddress(p);
            loaded = toPeer(n);
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return loaded;
    }

    public Boolean isKnown(NetworkPeer peer) {
        return loadPeer(peer)!=null;
    }

    private boolean updatePeer(NetworkPeer p) throws Exception {
        LOG.info("Find and Update Peer Node...");
        boolean updated = false;
        LOG.info("Looking up Node by Address (network="+p.getNetwork().name()+"): "+p.getDid().getPublicKey().getAddress());
        try (Transaction tx = db.getGraphDb().beginTx()) {
            Node n = db.getGraphDb().findNode(PEER_LABEL, "address", p.getDid().getPublicKey().getAddress());
            if(n!=null) {
                LOG.info("Found Node: updating...");
                toNode(p, n);
                updated = true;
            }
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
            throw e;
        }
        return updated;
    }

    public Boolean verifyPeer(NetworkPeer peer) {
        if(findPeerByNetworkedAddress(peer.getNetwork(), peer.getDid().getPublicKey().getAddress())==null) {
            return savePeer(peer, true);
        }
        return true;
    }

    public List<NetworkPeer> getAllPeers(NetworkPeer fromPeer, int pageSize, int beginIndex) {
        LOG.info("Get All Peers...");
        List<NetworkPeer> peers = new ArrayList<>();
        int numPeers = 0;
        try(Transaction tx = db.getGraphDb().beginTx()){
            ResourceIterator<Node> i = db.getGraphDb().findNodes(PEER_LABEL);
            while (i.hasNext() && numPeers++ < pageSize) {
                peers.add(toPeer(toMap(i.next())));
            }
            tx.success();
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return peers;
    }

    public NetworkPeer getRandomKnownPeer() {
        return getRandomKnownPeer(localNode.getNetworkPeer());
    }

    public NetworkPeer getRandomKnownPeer(NetworkPeer p) {
        return getRandomPeerByRelationship(p, P2PRelationship.RelType.IMS);
    }

    public NetworkPeer getRandomPeerByRelationship(NetworkPeer p, P2PRelationship.RelType relType) {
        LOG.info("Get Random Peer...");
        NetworkPeer peer = null;
        long numberPeers = totalPeersByRelationship(p, relType);
        if(numberPeers > 0) {
            long randomIndex = (long)(Math.random() * numberPeers);
            List<NetworkPeer> peers = getPeersByIndexRange(p, randomIndex, 1);
            peer = peers.get(0);
            LOG.info("Random peer selected: "+peer+" of peer: "+p);
        }
        return peer;
    }

    public Boolean isReliable(NetworkPeer leftPeer, NetworkPeer rightPeer) {
        Boolean reliable = false;
        try (Transaction tx = db.getGraphDb().beginTx()) {
            Node lpn = db.getGraphDb().findNode(PEER_LABEL, "address", leftPeer.getDid().getPublicKey().getAddress());
            Node rpn = db.getGraphDb().findNode(PEER_LABEL, "address", rightPeer.getDid().getPublicKey().getAddress());
            Iterator<Relationship> i = lpn.getRelationships(P2PRelationship.RelType.IMS, Direction.OUTGOING).iterator();
            while(i.hasNext()) {
                Relationship r = i.next();
                Node rN = r.getNodes()[1];
                if(rN.equals(rpn)) {
                    P2PRelationship rep = new P2PRelationship();
                    rep.fromMap(toMap(rN));
                    return rep.getTotalAcks() > MinAcksReliablePeer
                            && rep.getAvgAckLatencyMS() < MaxAvgLatencyReliablePeer
                            && rep.getMedAckLatencyMS() < MaxMedLatencyReliablePeer;
                }
            }
            tx.success();
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return reliable;
    }

    public List<NetworkPeer> getPeersByNetwork(Network network) {
        List<NetworkPeer> peers = new ArrayList<>();
        NetworkPeer p = getLocalNode().getNetworkPeer(network);
        try (Transaction tx = db.getGraphDb().beginTx()) {
            String cql = "MATCH (n {address: '"+p.getDid().getPublicKey().getAddress()+"'})-[:" + P2PRelationship.networkToRelationship(network).name() + "]->(x)" +
                    " RETURN x";
            Result r = db.getGraphDb().execute(cql);
            while (r.hasNext()) {
                peers.add(toPeer((Node)r.next().get("x")));
            }
            tx.success();
            LOG.info(peers.size() + " peers for local "+network.name()+ " peer");
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return peers;
    }

    // TODO: Move this to file system to speed up
    public Long totalPeersByRelationship(NetworkPeer p, P2PRelationship.RelType relType) {
        long count = -1;
        try (Transaction tx = db.getGraphDb().beginTx()) {
            String cql = "MATCH (n {address: '"+p.getDid().getPublicKey().getAddress()+"'})-[:" + relType.name() + "]->()" +
                    " RETURN count(*) as total";
            Result r = db.getGraphDb().execute(cql);
            if (r.hasNext()) {
                Map<String,Object> row = r.next();
                for (Map.Entry<String,Object> column : row.entrySet()) {
                    count = (long)column.getValue();
                    break;
                }
            }
            tx.success();
            LOG.info(count + " peers for peer: "+p.getDid().getPublicKey().getAddress());
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return count;
    }

    public Boolean isLocal(NetworkPeer r) {
        return localNode.getNetworkPeer(r.getNetwork())!=null
                && localNode.getNetworkPeer(r.getNetwork()).getDid().getPublicKey().getAddress().equals(r.getDid().getPublicKey().getAddress());
    }

    public NetworkPeer findPeerByAddress(String address) {
        NetworkPeer p = null;
        if(address!=null) {
            try (Transaction tx = db.getGraphDb().beginTx()) {
                Node n = db.getGraphDb().findNode(PEER_LABEL, "address", address);
                p = toPeer(n);
                tx.success();
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        return p;
    }

    public NetworkPeer findPeerByNetworkedAddress(Network network, String address) {
        NetworkPeer p = null;
        if(address!=null && network!=null) {
            try (Transaction tx = db.getGraphDb().beginTx()) {
                Node n = db.getGraphDb().findNode(PEER_LABEL, "address", address);
                if(n!=null) {
                    p = toPeer(n);
                }
                tx.success();
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        return p;
    }

    public NetworkPeer findPeerByAddressAllNetworks(String address) {
        NetworkPeer p = null;
        if(address!=null) {
            p = findPeerByAddress(address);
            if(p!=null) {
                return p;
            }
            for (Network network : Network.values()) {
                try (Transaction tx = db.getGraphDb().beginTx()) {
                    Node n = db.getGraphDb().findNode(PEER_LABEL, network.name() + "Address", address);
                    if(n!=null) {
                        p = toPeer(n);
                    }
                    tx.success();
                } catch (Exception e) {
                    LOG.warning(e.getLocalizedMessage());
                }
                if(p!=null) {
                    return p;
                }
            }
        }
        return null;
    }

    public boolean isKnown(String address) {
        boolean isKnown = findPeerByAddressAllNetworks(address) != null;
        LOG.info("Peer\n\taddress: "+address+"\n\tis known: "+isKnown);
        return isKnown;
    }

    public boolean isRelated(NetworkPeer peer, P2PRelationship.RelType relType) {
        boolean isRelated = hasRelationship(localNode.getNetworkPeer(peer.getNetwork()), peer, relType);
        LOG.info("Are Peers Related?:\n\tLocal Peer Address: "+localNode.getNetworkPeer(peer.getNetwork()).getDid().getPublicKey().getAddress()
                +"\n\tRemote Peer Address: "+peer.getDid().getPublicKey().getAddress()+"\n\t is related: "+isRelated);
        return isRelated;
    }

    public P2PRelationship relatePeers(NetworkPeer leftPeer, NetworkPeer rightPeer, P2PRelationship.RelType relType) {
        if(leftPeer==null || leftPeer.getDid().getPublicKey().getAddress()==null) {
            LOG.info("Relating peer not provided or doesn't have address, skipping.");
            return null;
        }
        if(rightPeer==null || rightPeer.getDid().getPublicKey().getAddress()==null) {
            LOG.info("Peer to relate with not provided or doesn't have address, skipping.");
            return null;
        }
        if(leftPeer.getDid().getPublicKey().getAddress()!=null
                && rightPeer.getDid().getPublicKey().getAddress()!=null
                && leftPeer.getDid().getPublicKey().getAddress().equals(rightPeer.getDid().getPublicKey().getAddress())) {
            LOG.info("Both peers are the same, skipping.");
            return null;
        }
        P2PRelationship rt = null;
        try (Transaction tx = db.getGraphDb().beginTx()) {
            Node lpn = db.getGraphDb().findNode(PEER_LABEL, "address", leftPeer.getDid().getPublicKey().getAddress());
            Node rpn = db.getGraphDb().findNode(PEER_LABEL, "address", rightPeer.getDid().getPublicKey().getAddress());
            Iterator<Relationship> i = lpn.getRelationships(relType, Direction.OUTGOING).iterator();
            while(i.hasNext()) {
                Relationship r = i.next();
                if(r.getNodes()[1].equals(rpn)) {
                    // load
                    rt = initP2PRel(r);
                    LOG.info("Found P2P Relationship; no need to create.");
                    break;
                }
            }
            if(rt==null) {
                // create
                Relationship r = lpn.createRelationshipTo(rpn, relType);
                rt = initP2PRel(r);
                LOG.info(rightPeer+" is now a "+ relType.name()+" peer of "+leftPeer);
            }
            tx.success();
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return rt;
    }

    public P2PRelationship getRelationship(NetworkPeer leftPeer, NetworkPeer rightPeer, P2PRelationship.RelType relType) {
        P2PRelationship rt = null;
        try (Transaction tx = db.getGraphDb().beginTx()) {
            Node lpn = db.getGraphDb().findNode(PEER_LABEL, "address", leftPeer.getDid().getPublicKey().getAddress());
            Node rpn = db.getGraphDb().findNode(PEER_LABEL, "address", rightPeer.getDid().getPublicKey().getAddress());
            Iterator<Relationship> i = lpn.getRelationships(relType, Direction.OUTGOING).iterator();
            while(i.hasNext()) {
                Relationship r = i.next();
                if(r.getNodes()[1].equals(rpn)) {
                    // load
                    rt = initP2PRel(r);
                    LOG.info("Found P2P Relationship");
                    break;
                }
            }
            tx.success();
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return rt;
    }

    public long countByRelType(NetworkPeer p, P2PRelationship.RelType relType) {
        long count = -1;
        try (Transaction tx = db.getGraphDb().beginTx()) {
            String cql = "MATCH (n {address: '"+p.getDid().getPublicKey().getAddress()+"'})-[:" + relType.name() + "]->()" +
                    " RETURN count(*) as total";
            Result r = db.getGraphDb().execute(cql);
            if (r.hasNext()) {
                Map<String,Object> row = r.next();
                for (Map.Entry<String,Object> column : row.entrySet()) {
                    count = (long)column.getValue();
                    break;
                }
            }
            tx.success();
            LOG.info(count+" "+ relType.name());
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return count;
    }

    /**
     * Remove relationship
     * @param startPeer
     */
    public boolean removeRelationship(NetworkPeer startPeer, NetworkPeer endPeer, P2PRelationship.RelType relType) {
        try (Transaction tx = db.getGraphDb().beginTx()) {
            String cql = "MATCH (n {address: '"+startPeer.getDid().getPublicKey().getAddress()+"'})-[r:" + relType.name() + "]->( e {address: '"+endPeer.getDid().getPublicKey().getAddress()+"'})" +
                    " DELETE r;";
            db.getGraphDb().execute(cql);
            tx.success();
            LOG.info(relType.name() + " relationship of "+endPeer+" removed from "+startPeer);
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    public List<NetworkPeer> getPeersByIndexRange(NetworkPeer lp, long startIndex, long limit) {
        LOG.info("Looking up peers starting at index["+startIndex+"] and limited to "+limit+" peers....");
        List<NetworkPeer> peers = new ArrayList<>();
        try (Transaction tx = db.getGraphDb().beginTx()) {
            String cql = "START rp=node(*) MATCH (lp:"+PEER_LABEL+" {address: '"+lp.getDid().getPublicKey().getAddress()+"'})->(rp:"+PEER_LABEL+")" +
                    " RETURN rp" +
                    " SKIP " + startIndex +
                    " LIMIT " + limit +";";
            Result r = db.getGraphDb().execute(cql);
            while (r.hasNext()) {
                peers.add(toPeer((Node)r.next().get("rp")));
            }
            tx.success();
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return peers;
    }

    public void reliablesFromRemotePeer(NetworkPeer remotePeer, List<NetworkPeer> remoteKnown) {
        LOG.info("Number of known by remote peer sent: "+remoteKnown.size());
        int saved = 0;
        LOG.info("Saving Remote Peer...");
        if(savePeer(remotePeer, true)) {
            LOG.info("Remote Peer saved.");
            saved++;
            relatePeers(localNode.getNetworkPeer(remotePeer.getNetwork()), remotePeer, P2PRelationship.networkToRelationship(remotePeer.getNetwork()));
            LOG.info("Remote Peer related as known to local peer.");
            long numberKnown = totalPeersByRelationship(localNode.getNetworkPeer(remotePeer.getNetwork()), P2PRelationship.networkToRelationship(remotePeer.getNetwork()));
            for (NetworkPeer known : remoteKnown) {
                if (numberKnown + saved > MaxPeersTracked)
                    break;
                LOG.info("Saving Remote Known...");
                savePeer(known, true);
                relatePeers(localNode.getNetworkPeer(remotePeer.getNetwork()), known, P2PRelationship.networkToRelationship(remotePeer.getNetwork()));
                LOG.info("Remote Peer saved and related as Known to local peer.");
                relatePeers(remotePeer, known, P2PRelationship.networkToRelationship(remotePeer.getNetwork()));
                LOG.info("Remote Known Peer related as Known to Remote Peer.");
                if (++saved >= MaxPeersShared) {
                    LOG.info("No longer taking reliables from this peer. Max reliables to receive reached: " + MaxPeersShared);
                    break; // Ensure we do not update beyond the max sent to help fight a form of DDOS
                }
            }
        }
    }

    // TODO: Should Node share known peers of the same network and/or include peers from other networks to speed up cross-network discovery?
    public List<NetworkPeer> getReliablesToShare(NetworkPeer p) {
        List<NetworkPeer> peers = new ArrayList<>();
        try (Transaction tx = db.getGraphDb().beginTx()) {
            String cql = "MATCH (n {address: '"+p.getDid().getPublicKey().getAddress()+"'})-[:" + P2PRelationship.networkToRelationship(p.getNetwork()).name() + "]->(x)" +
                    " RETURN x" +
                    " LIMIT "+ MaxPeersShared+";";
            Result result = db.getGraphDb().execute(cql);
            while (result.hasNext()) {
                peers.add(toPeer((Node)result.next().get("x")));
            }
            tx.success();
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return peers;
    }

    /**
     * Saves Peer Request status results.
     * Determine if results change Reliable Peers list.
     * Reliable Peers are defined as peers known by given peer who have displayed
     * a minimum number of acks (SensorsConfig.mr) and minimum avg response time (<= SensorsConfig.lmc)
     *
     * @param startPeer NetworkPeer originator
     * @param endPeer NetworkPeer destination
     * @param timeSent
     * @param timeAcknowledged
     */
    public Boolean savePeerStatusTimes(NetworkPeer startPeer, NetworkPeer endPeer, Long timeSent, Long timeAcknowledged) {
        boolean addedAsReliable = false;

        boolean hasRelationship;

        String startPeerAddress = startPeer.getDid().getPublicKey().getAddress();
        String endPeerAddress = endPeer.getDid().getPublicKey().getAddress();
        String relType = P2PRelationship.networkToRelationship(endPeer.getNetwork()).name();

        hasRelationship = hasRelationship(startPeer, endPeer, P2PRelationship.networkToRelationship(endPeer.getNetwork()));
        if(hasRelationship) {
            P2PRelationship knownRel = new P2PRelationship();
            String cql = "MATCH (n {address: '" + startPeerAddress + "'})-[r:" + relType + "]->(e {address: '" + endPeerAddress + "'})" +
                    "return r;";
            try (Transaction tx = db.getGraphDb().beginTx()) {
                Result result = db.getGraphDb().execute(cql);
                if (result.hasNext()) {
                    Relationship r = (Relationship)result.next().get("r");
                    knownRel.fromMap(r.getAllProperties());
                }
                tx.success();
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
            // Update stats
            knownRel.setLastAckTime(timeAcknowledged);
            knownRel.addAckTimeTracked(timeAcknowledged - timeSent, MaxAcksTracked);
            cql = "MATCH (n {address: '" + startPeerAddress + "'})-[r:" + relType + "]->(e {address: '" + endPeerAddress + "'})" +
                    " SET r.totalAcks = " + knownRel.getTotalAcks() + "," +
                    " r.lastAckTime = " + knownRel.getLastAckTime() + "," +
                    " r.avgAckLatencyMS = " + knownRel.getAvgAckLatencyMS() + "," +
                    " r.medAckLatencyMS = " + knownRel.getMedAckLatencyMS() + "," +
                    " r.ackTimesTracked = '" + knownRel.getAckTimesTracked() + "';";
            try (Transaction tx = db.getGraphDb().beginTx()) {
                db.getGraphDb().execute(cql);
                tx.success();
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }

            LOG.info("Peer status times: {\n" +
                    "\tack received by local peer in: "+(timeAcknowledged-timeSent)+"ms\n"+
                    "\ttotal acks: "+knownRel.getLastAckTime()+"\n"+
                    "\tmed round trip latency: "+knownRel.getMedAckLatencyMS()+
                    "\tavg round trip latency: "+knownRel.getAvgAckLatencyMS()+"ms\n} of remote peer "+endPeer+" with start peer "+startPeer);

        } else if(totalPeersByRelationship(startPeer, P2PRelationship.networkToRelationship(startPeer.getNetwork())) <= MaxPeersTracked) {
            relatePeers(startPeer, endPeer, P2PRelationship.networkToRelationship(endPeer.getNetwork()));
            LOG.info("New relationship ("+ P2PRelationship.networkToRelationship(endPeer.getNetwork())+") with peer: "+endPeer);
        } else {
            LOG.info("Max peers tracked: "+ MaxPeersTracked);
        }
        return addedAsReliable;
    }

    public boolean hasRelationship(NetworkPeer startPeer, NetworkPeer endPeer, RelationshipType relType) {
        boolean hasRel = false;
        String cql = "MATCH (n {address: '" + startPeer.getDid().getPublicKey().getAddress() + "'})-[r:" + relType.name() + "]->(e {address: '" + endPeer.getDid().getPublicKey().getAddress() + "'})" +
                " RETURN r;";
        try (Transaction tx = db.getGraphDb().beginTx()) {
            Result result = db.getGraphDb().execute(cql);
            if (result.hasNext()) {
                hasRel = true;
                LOG.info(endPeer + " is "+relType.name()+" peer to " + startPeer);
            }
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return hasRel;
    }

    public void report(NetworkPeer networkPeer) {
        LOG.info("NetworkPeer reported: "+networkPeer);
    }

    public void report(List<NetworkPeer> networkPeers) {
        for(NetworkPeer networkPeer : networkPeers){
            report(networkPeer);
        }
    }

    private Map<String,Object> toMap(PropertyContainer n) {
        return GraphUtil.getAttributes(n);
    }

    private NetworkPeer toPeer(PropertyContainer n) {
        NetworkPeer p = new NetworkPeer();
        p.fromMap(toMap(n));
        return p;
    }

    private NetworkPeer toPeer(Map<String,Object> m) {
        NetworkPeer p = new NetworkPeer();
        p.fromMap(m);
        return p;
    }

    private void toNode(NetworkPeer p, PropertyContainer n) {
        GraphUtil.updateProperties(n, p.toMap());
    }

    private P2PRelationship initP2PRel(Relationship r) {
        P2PRelationship p2PR = new P2PRelationship();
        p2PR.fromMap(toMap(r));
        return p2PR;
    }

}
