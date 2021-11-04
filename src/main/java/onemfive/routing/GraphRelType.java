package onemfive.routing;

import org.neo4j.graphdb.RelationshipType;
import ra.common.network.Network;
import ra.networkmanager.RelType;

public class GraphRelType implements RelationshipType {

    private RelType relType;

    public GraphRelType(RelType relType) {
        this.relType = relType;
    }

    public static GraphRelType getInstance(RelType relType) {
        return new GraphRelType(relType);
    }

    public static GraphRelType getInstance(Network network) {
        return new GraphRelType(RelType.fromNetwork(network.name()));
    }

    @Override
    public String name() {
        return relType.name();
    }
}
