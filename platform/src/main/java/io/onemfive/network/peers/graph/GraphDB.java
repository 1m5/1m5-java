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
package io.onemfive.network.peers.graph;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.peers.P2PRelationship;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static io.onemfive.network.peers.PeerManager.MaxAcksTracked;
import static io.onemfive.network.peers.PeerManager.MaxPeersTracked;

public class GraphDB {

    private static final Logger LOG = Logger.getLogger(GraphDB.class.getName());

    private boolean initialized = false;
    private String location;
    private String name;
    private Properties properties;
    private GraphDatabaseService graphDb;
    private Label label = Label.label("P2P");

    public GraphDB() {
        super();
    }

    public GraphDatabaseService getGraphDb() {
        return graphDb;
    }

    public List<NetworkPeer> findLeastHopsPath(String fromPeerId, String toPeerId) {
        List<NetworkPeer> leastHopsPath = new ArrayList<>();
        try (Transaction tx = graphDb.beginTx()) {
            PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection(P2PRelationship.RelType.IMS, Direction.OUTGOING), 15);
            Node startNode = findPeerNode(fromPeerId);
            Node endNode = findPeerNode(toPeerId);
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

    public List<NetworkPeer> findLowestLatencyPath(String fromPeerId, String toPeerId) {
        List<NetworkPeer> lowestLatencyPath = new ArrayList<>();
        try (Transaction tx = graphDb.beginTx()) {
            PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.forTypeAndDirection(P2PRelationship.RelType.IMS, Direction.OUTGOING), P2PRelationship.AVG_ACK_LATENCY_MS);
            Node startNode = findPeerNode(fromPeerId);
            Node endNode = findPeerNode(toPeerId);
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
     * @param fromPeerId
     * @param toPeerId
     * @param networks
     * @return
     */
    public List<NetworkPeer> findLowestLatencyPathFiltered(String fromPeerId, String toPeerId, Network[] networks) {
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
        try (Transaction tx = graphDb.beginTx()) {
            PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.forTypeAndDirection(P2PRelationship.RelType.IMS, Direction.OUTGOING), P2PRelationship.AVG_ACK_LATENCY_MS);
//            PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanderBuilder.allTypes(Direction.OUTGOING).)
            Node startNode = findPeerNode(fromPeerId);
            Node endNode = findPeerNode(toPeerId);
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

    /**
     * Requires to be used within a Transaction
     * @param id Id of NetworkPeer
     * @return
     * @throws Exception
     */
    private Node findPeerNode(String id) {
        return graphDb.findNode(label, "id", id);
    }

    public boolean isRelatedByNetwork(String startPeerId, Network network, String endPeerId) {
        boolean hasRel = false;
        P2PRelationship.RelType relType = P2PRelationship.networkToRelationship(network);
        String cql = "MATCH (n {id: '" + startPeerId + "'})-[r:" + relType.name() + "]->(e {id: '" + endPeerId + "'})" +
                " RETURN r;";
        try (Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute(cql);
            if (result.hasNext()) {
                hasRel = true;
                LOG.info(endPeerId + " is "+relType.name()+" peer to " + startPeerId);
            }
            tx.success();
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return hasRel;
    }

    public P2PRelationship relateByNetwork(String idLeftPeer, Network network, String idRightPeer) {
        if(idLeftPeer==null || idRightPeer==null) {
            LOG.warning("Must provide ids for both peers when relating them.");
            return null;
        }
        if(idLeftPeer.equals(idRightPeer)) {
            LOG.info("Both peers are the same, skipping.");
            return null;
        }
        P2PRelationship.RelType relType = P2PRelationship.networkToRelationship(network);
        P2PRelationship rt = null;
        try (Transaction tx = graphDb.beginTx()) {
            Node lpn = findPeerNode(idLeftPeer);
            Node rpn = findPeerNode(idRightPeer);
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
                LOG.info(idRightPeer+" is now a "+ relType.name()+" peer of "+idLeftPeer);
            }
            tx.success();
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return rt;
    }

    public P2PRelationship getNetworkRelationship(String idLeftPeer, Network network, String idRightPeer) {
        P2PRelationship.RelType relType = P2PRelationship.networkToRelationship(network);
        P2PRelationship rt = null;
        try (Transaction tx = graphDb.beginTx()) {
            Node lpn = findPeerNode(idLeftPeer);
            Node rpn = findPeerNode(idRightPeer);
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

    public int numberPeersByNetwork(String id, Network network) {
        P2PRelationship.RelType relType = P2PRelationship.networkToRelationship(network);
        int count = -1;
        try (Transaction tx = graphDb.beginTx()) {
            String cql = "MATCH (n {id: '"+id+"'})-[:" + relType.name() + "]->()" +
                    " RETURN count(*) as total";
            Result r = graphDb.execute(cql);
            if (r.hasNext()) {
                Map<String,Object> row = r.next();
                for (Map.Entry<String,Object> column : row.entrySet()) {
                    count = (int)column.getValue();
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
     */
    public boolean removeNetworkRelationship(String startPeerId, Network network, String endPeerId) {
        P2PRelationship.RelType relType = P2PRelationship.networkToRelationship(network);
        try (Transaction tx = graphDb.beginTx()) {
            String cql = "MATCH (n {id: '"+startPeerId+"'})-[r:" + relType.name() + "]->( e {id: '"+endPeerId+"'})" +
                    " DELETE r;";
            graphDb.execute(cql);
            tx.success();
            LOG.info(relType.name() + " relationship of "+endPeerId+" removed from "+startPeerId);
        } catch(Exception e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Saves Peer Request status results.
     * Determine if results change Reliable Peers list.
     * Reliable Peers are defined as peers known by given peer who have displayed
     * a minimum number of acks (SensorsConfig.mr) and minimum avg response time (<= SensorsConfig.lmc)
     *
     * @param startPeerId originator
     * @param network Network
     * @param endPeerId destination
     * @param timeSent time sent in milliseconds since epoch
     * @param timeAcknowledged time acknowledged in milliseconds since epoch
     */
    public Boolean savePeerStatusTimes(String startPeerId, Network network, String endPeerId, Long timeSent, Long timeAcknowledged) {
        boolean addedAsReliable = false;
        P2PRelationship.RelType relType = P2PRelationship.networkToRelationship(network);
        P2PRelationship networkRel = getNetworkRelationship(startPeerId, network, endPeerId);
        if(networkRel!=null) {
            // Update stats
            networkRel.setLastAckTime(timeAcknowledged);
            networkRel.addAckTimeTracked(timeAcknowledged - timeSent, MaxAcksTracked);
            String cql = "MATCH (n {id: '" + startPeerId + "'})-[r:" + relType + "]->(e {id: '" + endPeerId + "'})" +
                    " SET r.totalAcks = " + networkRel.getTotalAcks() + "," +
                    " r.lastAckTime = " + networkRel.getLastAckTime() + "," +
                    " r.avgAckLatencyMS = " + networkRel.getAvgAckLatencyMS() + "," +
                    " r.medAckLatencyMS = " + networkRel.getMedAckLatencyMS() + "," +
                    " r.ackTimesTracked = '" + networkRel.getAckTimesTracked() + "';";
            try (Transaction tx = graphDb.beginTx()) {
                graphDb.execute(cql);
                tx.success();
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }

            LOG.info("Peer status times: {\n" +
                    "\tack received by local peer in: "+(timeAcknowledged-timeSent)+"ms\n"+
                    "\ttotal acks: "+networkRel.getLastAckTime()+"\n"+
                    "\tmed round trip latency: "+networkRel.getMedAckLatencyMS()+
                    "\tavg round trip latency: "+networkRel.getAvgAckLatencyMS()+"ms\n} of remote peer "+endPeerId+" with start peer "+startPeerId);

        } else if(numberPeersByNetwork(startPeerId, network) <= MaxPeersTracked) {
            relateByNetwork(startPeerId, network, endPeerId);
            LOG.info("New relationship ("+ network.name()+") with peer: "+endPeerId);
        } else {
            LOG.info("Max peers tracked: "+ MaxPeersTracked);
        }
        return addedAsReliable;
    }

    private P2PRelationship initP2PRel(Relationship r) {
        P2PRelationship p2PR = new P2PRelationship();
        p2PR.fromMap(toMap(r));
        return p2PR;
    }

    private NetworkPeer toPeer(PropertyContainer n) {
        NetworkPeer networkPeer = new NetworkPeer();
        networkPeer.setId((String)n.getProperty("id"));
        return networkPeer;
    }

    private Map<String,Object> toMap(PropertyContainer n) {
        return GraphUtil.getAttributes(n);
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean init(Properties properties) {
        if(location==null) {
            LOG.warning("Neo4J DB location required. Please provide.");
            return false;
        }
        if(name==null) {
            LOG.warning("Neo4J DB name required. Please provide.");
            return false;
        }
        if(!initialized) {
            this.properties = properties;
            File dbDir = new File(location+"/"+name);
            if(!dbDir.exists() && !dbDir.mkdir()) {
                LOG.warning("Unable to create graph db directory at: "+location+"/"+name);
                return false;
            }

//            graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbDir);
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbDir)
                    .setConfig(GraphDatabaseSettings.allow_upgrade,"true")
                    .newGraphDatabase();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    teardown();
                }
            } );
            initialized = true;
        }
        return true;
    }

    public boolean teardown() {
        LOG.info("Tearing down...");
        graphDb.shutdown();
        LOG.info("Torn down.");
        return true;
    }
}
