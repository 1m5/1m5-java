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

import io.onemfive.core.keyring.AuthNRequest;
import io.onemfive.neo4j.GraphUtil;
import io.onemfive.neo4j.Neo4jDB;
import io.onemfive.network.*;
import io.onemfive.network.NetworkConfig;
import io.onemfive.util.FileUtil;
import io.onemfive.util.RandomUtil;
import io.onemfive.util.tasks.TaskRunner;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static io.onemfive.data.ServiceMessage.NO_ERROR;

public class PeerManager implements Runnable {

    private static final Logger LOG = Logger.getLogger(PeerManager.class.getName());

    public static final Label PEER_LABEL = Label.label(NetworkPeer.class.getSimpleName());
    public static final String PEER_LOCAL = "localPeer";

    // Peer-to-Peer Relationship
    public static final String AVG_ACK_LATENCY_MS = "avgAckLatencyMS";

    public static final String DBNAME = "1m5_peers_db";

    private Properties properties;

    protected NetworkService service;
    protected NetworkNode localNode = new NetworkNode();
    protected TaskRunner taskRunner;

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
        if ("true".equals(properties.getProperty("onemfive.network.db.cleanOnRestart"))) {
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
                // No Address Indexes...set them up
                db.getGraphDb().schema().indexFor(PEER_LABEL).withName("NetworkPeer.address").on("address").create();
                db.getGraphDb().schema().indexFor(PEER_LABEL).withName("NetworkPeer.network").on("network").create();
                LOG.info("1M5 Indexes created.");
            }
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        LOG.info("Relationship configurations:" +
                "\n\tonemfive.sensors.MinPT="+ NetworkConfig.MinPT+": Min Peers Tracked - the point at which Discovery process goes into 'hyper' mode." +
                "\n\tonemfive.sensors.MaxPT="+ NetworkConfig.MaxPT+": Max Peers Tracked - the total number of Peers to attempt to maintain knowledge of." +
                "\n\tonemfive.sensors.MaxPS="+ NetworkConfig.MaxPS+": Max Peers Sent - Maximum number of peers to send in a peer list (the bigger a datagram, the less chance of it getting through)." +
                "\n\tonemfive.sensors.MaxAT="+ NetworkConfig.MaxAT+": Max Acknowledgments Tracked" +
                "\n\tonemfive.sensors.UI="+ NetworkConfig.UI+": Update Interval - the minutes between Discovery process" +
                "\n\tonemfive.sensors.MinAckRP="+ NetworkConfig.MinAckRP+": Reliable Peer Min Acks" +
                "\n\tonemfive.sensors.MinAckSRP="+ NetworkConfig.MinAckSRP+": Super Reliable Peer Min Acks");

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
            LOG.info("Updating Local Peer: \n\taddress: "+r.identityPublicKey.getAddress()+"\n\tfingerprint: "+r.identityPublicKey.getFingerprint());
            NetworkPeer localPeer = new NetworkPeer();
            if(r.identityPublicKey.getAddress()!=null)
                localPeer.getDid().getPublicKey().setAddress(r.identityPublicKey.getAddress());
            if(r.identityPublicKey.getFingerprint()!=null)
                localPeer.getDid().getPublicKey().setFingerprint(r.identityPublicKey.getFingerprint());
            localPeer.getDid().getPublicKey().isIdentityKey(true);
            localPeer.setLocal(true);
            localPeer.getDid().setAuthenticated(true);
            localPeer.getDid().setVerified(true);
            savePeer(localPeer, true);
            LOG.info("Added returned public key to local Peer:"+localPeer);
        } else {
            LOG.warning("Error returned from AuthNRequest: " + r.statusCode);
        }
    }

    public void updateLocalPeer(NetworkPeer np) {
        localNode.addLocalNetworkPeer(np);
        savePeer(np, true);
        LOG.info("Update local Peer: "+np);
    }

    public List<NetworkPeer> findLeastHopsPath(NetworkPeer fromPeer, NetworkPeer toPeer) {
        List<NetworkPeer> leastHopsPath = new ArrayList<>();
        try (Transaction tx = db.getGraphDb().beginTx()) {
            PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection(P2PRelationship.RelType.Known, Direction.OUTGOING), 15);
            Node startNode = findPeerNode(fromPeer);
            Node endNode = findPeerNode(toPeer);
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
            PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.forTypeAndDirection(P2PRelationship.RelType.Known, Direction.OUTGOING), AVG_ACK_LATENCY_MS);
            Node startNode = findPeerNode(fromPeer);
            Node endNode = findPeerNode(toPeer);
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
            long numberPeers = totalPeersByRelationship(localNode.getLocalNetworkPeer(p.getNetwork()), P2PRelationship.RelType.Known);
            // TODO: Sensors configuration be network-specific
            if(numberPeers <= NetworkConfig.MaxPT) {
                try (Transaction tx = db.getGraphDb().beginTx()) {
                    Node n = db.getGraphDb().createNode(PEER_LABEL);
                    toNode(p,n);
                    tx.success();
                    LOG.info("NetworkPeer saved to graph.");
                } catch (Exception e) {
                    LOG.warning(e.getLocalizedMessage());
                }
            } else {
                LOG.info("Not adding peer; max number of peers reached: "+ NetworkConfig.MaxPT);
            }
        } else {
            LOG.info("New Peer but autocreate is false, unable to save peer.");
        }
        if(!isRemoteLocal(p) && !isRelated(p, P2PRelationship.RelType.Known)) {
            LOG.info("Peer not known: relating as known.");
            relatePeers(localNode.getLocalNetworkPeer(p.getNetwork()), p, P2PRelationship.RelType.Known);
            LOG.info("Peers related as known.");
        }
        return true;
    }

    /**
     * Requires to be used within a Transaction
     * @param p
     * @return
     * @throws Exception
     */
    private Node findPeerNode(NetworkPeer p) {
        return db.getGraphDb().findNode(PEER_LABEL, "address", p.getDid().getPublicKey().getAddress());
    }

    public NetworkPeer loadPeer(NetworkPeer p) {
        if(p==null) return null;
        NetworkPeer loaded = null;
        try (Transaction tx = db.getGraphDb().beginTx()) {
            Node n = findPeerNode(p);
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
        LOG.info("Looking up Node by Address: "+p.getDid().getPublicKey().getAddress());
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
        return getRandomKnownPeer(localNode.getLocalNetworkPeer());
    }

    public NetworkPeer getRandomKnownPeer(NetworkPeer p) {
        return getRandomPeerByRelationship(p, P2PRelationship.RelType.Known);
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
            Iterator<Relationship> i = lpn.getRelationships(P2PRelationship.RelType.Reliable, Direction.OUTGOING).iterator();
            while(i.hasNext()) {
                Relationship r = i.next();
                if(r.getNodes()[1].equals(rpn)) {
                    reliable = true;
                    break;
                }
            }
            tx.success();
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return reliable;
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

    public Boolean isRemoteLocal(NetworkPeer r) {
        return localNode.getLocalNetworkPeer(r.getNetwork())!=null
                && localNode.getLocalNetworkPeer(r.getNetwork()).getDid().getPublicKey().getAddress().equals(r.getDid().getPublicKey().getAddress());
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
        boolean isRelated = hasRelationship(localNode.getLocalNetworkPeer(peer.getNetwork()), peer, relType);
        LOG.info("Are Peers Related?:\n\tLocal Peer Address: "+localNode.getLocalNetworkPeer(peer.getNetwork()).getDid().getPublicKey().getAddress()
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
            relatePeers(localNode.getLocalNetworkPeer(remotePeer.getNetwork()), remotePeer, P2PRelationship.RelType.Known);
            LOG.info("Remote Peer related as known to local peer.");
            long numberKnown = totalPeersByRelationship(localNode.getLocalNetworkPeer(remotePeer.getNetwork()), P2PRelationship.RelType.Known);
            NetworkPeer remoteRelP;
            for (NetworkPeer known : remoteKnown) {
                if (numberKnown + saved > NetworkConfig.MaxPT)
                    break;
                LOG.info("Saving Remote Known...");
                savePeer(known, true);
                relatePeers(localNode.getLocalNetworkPeer(remotePeer.getNetwork()), known, P2PRelationship.RelType.Known);
                LOG.info("Remote Peer saved and related as Known to local peer.");
                relatePeers(remotePeer, known, P2PRelationship.RelType.Known);
                LOG.info("Remote Known Peer related as Known to Remote Peer.");
                if (++saved >= NetworkConfig.MaxPS) {
                    LOG.info("No longer taking reliables from this peer. Max reliables to receive reached: " + NetworkConfig.MaxPS);
                    break; // Ensure we do not update beyond the max sent to help fight a form of DDOS
                }
            }
        }
    }

    public List<NetworkPeer> getReliablesToShare(NetworkPeer p) {
        List<NetworkPeer> peers = new ArrayList<>();
        try (Transaction tx = db.getGraphDb().beginTx()) {
            String cql = "MATCH (n {address: '"+p.getDid().getPublicKey().getAddress()+"'})-[:" + P2PRelationship.RelType.Known.name() + "]->(x)" +
                    " RETURN x" +
                    " LIMIT "+ NetworkConfig.MaxPS+";";
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
     * @param network used
     */
    public Boolean savePeerStatusTimes(NetworkPeer startPeer, NetworkPeer endPeer, Long timeSent, Long timeAcknowledged, Network network) {
        boolean addedAsReliable = false;

        boolean hasRelationship;
        boolean isReliable;
        boolean isSuperReliable;

        long totalAcks = 0;
        long avgAckLatency;

        hasRelationship = hasRelationship(startPeer, endPeer, P2PRelationship.RelType.Known);
        if(hasRelationship) {
            P2PRelationship knownRel = new P2PRelationship();
            String cql = "MATCH (n {address: '" + startPeer.getDid().getPublicKey().getAddress() + "'})-[r:" + P2PRelationship.RelType.Known.name() + "]->(e {address: '" + endPeer.getDid().getPublicKey().getAddress() + "'})" +
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
            knownRel.addAckTimeTracked(timeAcknowledged - timeSent);
            avgAckLatency = knownRel.getAvgAckLatencyMS();
            cql = "MATCH (n {address: '" + startPeer.getDid().getPublicKey().getAddress() + "'})-[r:" + P2PRelationship.RelType.Known.name() + "]->(e {address: '" + endPeer.getDid().getPublicKey().getAddress() + "'})" +
                    " SET r.totalAcks = " + knownRel.getTotalAcks() + "," +
                    " r.lastAckTime = " + knownRel.getLastAckTime() + "," +
                    " r.avgAckLatencyMS = " + avgAckLatency + "," +
                    " r.ackTimesTracked = '" + knownRel.getAckTimesTracked() + "';";
            try (Transaction tx = db.getGraphDb().beginTx()) {
                db.getGraphDb().execute(cql);
                tx.success();
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
            // Ensure total acks updated and persisted
            cql = "MATCH (n {address: '" + startPeer.getDid().getPublicKey().getAddress() + "'})-[r:" + P2PRelationship.RelType.Known.name() + "]->(e {address: '" + endPeer.getDid().getPublicKey().getAddress() + "'})" +
                    "return r;";
            P2PRelationship k = new P2PRelationship();
            try (Transaction tx = db.getGraphDb().beginTx()) {
                Result result = db.getGraphDb().execute(cql);
                if (result.hasNext()) {
                    Relationship r = (Relationship)result.next().get("r");
                    k.fromMap(r.getAllProperties());
                    totalAcks = k.getTotalAcks();
                }
                tx.success();
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }

            // Update relationship
            isReliable = hasRelationship(startPeer, endPeer, P2PRelationship.RelType.Reliable);
            if(isReliable) {
                isSuperReliable = hasRelationship(startPeer, endPeer, P2PRelationship.RelType.SuperReliable);
                if(!isSuperReliable && knownRel.getTotalAcks() >= NetworkConfig.MinAckSRP) {
                    relatePeers(startPeer, endPeer, P2PRelationship.RelType.SuperReliable);
                    LOG.info("Now super reliable peer: "+endPeer);
                }
            } else if(knownRel.getTotalAcks() >= NetworkConfig.MinAckRP) {
                // Reliable relationship
                relatePeers(startPeer, endPeer, P2PRelationship.RelType.Reliable);
                addedAsReliable = true;
                LOG.info("Now reliable peer: "+endPeer);
            }

            LOG.info("Peer status times: {\n" +
                    "\tack received by local peer in: "+(timeAcknowledged-timeSent)+"ms\n"+
                    "\ttotal acks: "+totalAcks+"\n"+
                    "\tavg round trip latency: "+avgAckLatency+"ms\n} of remote peer "+endPeer+" with start peer "+startPeer);

        } else if(totalPeersByRelationship(startPeer, P2PRelationship.RelType.Known) <= NetworkConfig.MaxPT) {
            relatePeers(startPeer, endPeer, P2PRelationship.RelType.Known);
            LOG.info("New relationship ("+ P2PRelationship.RelType.Known.name()+") with peer: "+endPeer);
        } else {
            LOG.info("Max peers tracked: "+ NetworkConfig.MaxPT);
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

    public static void main(String[] args) {
        /**
         * For example, Peer A wishes to send a message to Peer C at lowest latency path:
         *
         * Using only I2P:
         *
         *     Peer A to Peer C avg latency with I2P is 10 seconds
         *     Peer A to Peer B avg latency with I2P is 2 seconds
         *     Peer B to Peer C avg latency with I2P is 4 seconds
         *
         * In this case Peer A will use Peer B to get to Peer C with a likely latency result of 6 seconds.
         *
         * But if Tor was used:
         *
         *     Peer A to Peer C avg latency with Tor is 5 seconds
         *     Peer A to Peer B avg latency with Tor is 4 seconds
         *     Peer B to Peer C avg latency with Tor is 6 seconds
         *
         * In this case Peer A will send directly to Peer C with Tor at a likely latency of 5 seconds
         *
         * And Using Bluetooth:
         *
         *     Peer A to Peer C avg latency with Bluetooth is 1/2 second (they are physically next to each other)
         *     Peer A to Peer B avg latency with Bluetooth is 30 seconds (many hops)
         *     Peer B to Peer C avg latency with Bluetooth is 30 seconds
         *
         * Peer A easily sends directly to Peer C with Bluetooth at a likely latency of 1/2 second
         *
         * If we use all networks to determine, Bluetooth will be selected using path A -> C.
         *
         * If then Peer C turns off Bluetooth and all networks are evaluated, Tor will be selected A -> C (5 seconds).
         *
         * But say Peer C's Tor access gets blocked, then I2P with path A -> B -> C will be selected.
         *
         * But say Peer B shows up near Peer A and turns on their Bluetooth with a result in avg latency
         * with Bluetooth A -> B of 1/2 second, now the path to C will be A -> B using Bluetooth and B -> C using I2P
         * with an expected latency of 4.5 seconds.
         */
        Properties p = new Properties();
        p.setProperty("1m5.network.peers.dir","/home/objectorange/Projects/1m5/1m5/src/test/resources");
        PeerManager mgr = new PeerManager();
        mgr.init(p);

        // Node A
        NetworkPeer pA = mgr.localNode.getLocalNetworkPeer();
        pA.setLocal(true);
        pA.getDid().setUsername("Alice");
        pA.getDid().getPublicKey().setAddress("1m5-A");
        mgr.savePeer(pA, true);
        NetworkPeer pAI2P = new NetworkPeer(Network.I2P);
        pAI2P.getDid().getPublicKey().setAddress("i2p-A");
        mgr.savePeer(pAI2P, true);
        NetworkPeer pATor = new NetworkPeer(Network.TOR);
        pATor.getDid().getPublicKey().setAddress("tor-A");
        mgr.savePeer(pATor, true);
        NetworkPeer pABT = new NetworkPeer(Network.RADIO_BLUETOOTH);
        pABT.getDid().getPublicKey().setAddress("bt-A");
        mgr.savePeer(pABT, true);

        // Node B
        NetworkPeer pB = new NetworkPeer();
        pB.getDid().setUsername("Bob");
        pB.getDid().getPublicKey().setAddress("1m5-B");
        mgr.savePeer(pB, true);
        NetworkPeer pBI2P = new NetworkPeer(Network.I2P);
        pBI2P.getDid().getPublicKey().setAddress("i2p-B");
        mgr.savePeer(pBI2P, true);
        NetworkPeer pBTor = new NetworkPeer(Network.TOR);
        pBTor.getDid().getPublicKey().setAddress("tor-B");
        mgr.savePeer(pBTor, true);
        NetworkPeer pBBT = new NetworkPeer(Network.RADIO_BLUETOOTH);
        pBBT.getDid().getPublicKey().setAddress("bt-B");
        mgr.savePeer(pBBT, true);

        // Node C
        NetworkPeer pC = new NetworkPeer();
        pC.getDid().setUsername("Charlie");
        pC.getDid().getPublicKey().setAddress("1m5-C");
        mgr.savePeer(pC, true);
        NetworkPeer pCI2P = new NetworkPeer(Network.I2P);
        pCI2P.getDid().getPublicKey().setAddress("i2p-C");
        mgr.savePeer(pCI2P, true);
        NetworkPeer pCTor = new NetworkPeer(Network.TOR);
        pCTor.getDid().getPublicKey().setAddress("tor-C");
        mgr.savePeer(pCTor, true);
        NetworkPeer pCBT = new NetworkPeer(Network.RADIO_BLUETOOTH);
        pCBT.getDid().getPublicKey().setAddress("bt-C");
        mgr.savePeer(pCBT, true);

        long numPeers = mgr.totalPeersByRelationship(mgr.localNode.getLocalNetworkPeer(), P2PRelationship.RelType.Known);
        LOG.info("num peers: "+numPeers);

        // Relate B->C
        mgr.relatePeers(pB, pC, P2PRelationship.RelType.Known);

        long sent = 10 *60*1000;
        long ack;

        // I2P latencies
        // A -> C
        for(int i=0; i<2; i++) {
            ack = sent + 10000;
            mgr.savePeerStatusTimes(pAI2P, pCI2P, sent, ack, Network.I2P);
            sent = ack;
        }
        // A -> B
        for(int i=0; i<2; i++) {
            ack = sent + 2000;
            mgr.savePeerStatusTimes(pAI2P, pBI2P, sent, ack, Network.I2P);
            sent = ack;
        }
        // B -> C
        for(int i=0; i<2; i++) {
            ack = sent + 40000;
            mgr.savePeerStatusTimes(pBI2P, pCI2P, sent, ack, Network.I2P);
            sent = ack;
        }

        LOG.info("Lowest Latency Path on I2P A -> C: ");
        List<NetworkPeer> llPath = mgr.findLowestLatencyPath(pAI2P, pCI2P);
        for(NetworkPeer np : llPath) {
            LOG.info(np.toString());
        }

        // Tor latencies
        // A -> C
        for(int i=0; i<2; i++) {
            ack = sent + 500;
            mgr.savePeerStatusTimes(pATor, pCTor, sent, ack, Network.TOR);
            sent = ack;
        }
        // A -> B
        for(int i=0; i<2; i++) {
            ack = sent + 30000;
            mgr.savePeerStatusTimes(pATor, pBTor, sent, ack, Network.TOR);
            sent = ack;
        }
        // B -> C
        for(int i=0; i<2; i++) {
            ack = sent + 30000;
            mgr.savePeerStatusTimes(pBTor, pCTor, sent, ack, Network.TOR);
            sent = ack;
        }

        LOG.info("Lowest Latency Path on Tor A -> C: ");
        llPath = mgr.findLowestLatencyPath(pATor, pCTor);
        for(NetworkPeer np : llPath) {
            LOG.info(np.toString());
        }

        // Bluetooth latencies
        // A -> C
        for(int i=0; i<2; i++) {
            ack = sent + 5000;
            mgr.savePeerStatusTimes(pABT, pCBT, sent, ack, Network.RADIO_BLUETOOTH);
            sent = ack;
        }
        // A -> B
        for(int i=0; i<2; i++) {
            ack = sent + 4000;
            mgr.savePeerStatusTimes(pABT, pBBT, sent, ack, Network.RADIO_BLUETOOTH);
            sent = ack;
        }
        // B -> C
        for(int i=0; i<2; i++) {
            ack = sent + 6000;
            mgr.savePeerStatusTimes(pBBT, pCBT, sent, ack, Network.RADIO_BLUETOOTH);
            sent = ack;
        }

        LOG.info("Lowest Latency Path on BT A -> C: ");
        llPath = mgr.findLowestLatencyPath(pABT, pCBT);
        for(NetworkPeer np : llPath) {
            LOG.info(np.toString());
        }

//        LOG.info("Lowest Latency Path on All Networks A -> C: ");
//        llPath = mgr.findLowestLatencyPathAllNetworks(pA, pC);
//        for(NetworkPeer np : llPath) {
//            LOG.info(np.toString());
//        }
//
//        LOG.info("Lowest Latency Path with only Tor and I2P A -> C: ");
//        llPath = mgr.findLowestLatencyPathSpecifiedNetworks(pA, pC, Arrays.asList(Network.TOR, Network.I2P));
//        for(NetworkPeer np : llPath) {
//            LOG.info(np.toString());
//        }

    }
}
