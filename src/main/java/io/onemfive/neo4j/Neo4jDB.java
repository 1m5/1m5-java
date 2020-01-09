package io.onemfive.neo4j;

import io.onemfive.core.infovault.BaseInfoVaultDB;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class Neo4jDB extends BaseInfoVaultDB {

    private static final Logger LOG = Logger.getLogger(Neo4jDB.class.getName());

    private boolean initialized = false;
    private Properties properties;
    private GraphDatabaseService graphDb;

    public Neo4jDB() {
        super();
    }

    public GraphDatabaseService getGraphDb() {
        return graphDb;
    }

    @Override
    public Status getStatus() {
        return null;
    }

    public void save(String label, String key, byte[] content, boolean autoCreate) throws FileNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node n = graphDb.findNode(Label.label(label),"name",key);
            if(n == null) {
                if(autoCreate) {
                    n = graphDb.createNode(Label.label(File.class.getName()));
                    n.setProperty("name", key);
                } else
                    throw new FileNotFoundException("Key not found and autoCreate=false");
            }
            n.setProperty("content",content);
            tx.success();
        }
    }

    public byte[] load(String label, String key) throws FileNotFoundException {
        byte[] content;
        try (Transaction tx = graphDb.beginTx()) {
            Node n = graphDb.findNode(Label.label(label),"name",key);
            if(n == null)
                throw new FileNotFoundException("Key "+key+" not found.");
            Object obj = n.getProperty("content");
            if(obj == null)
                throw new FileNotFoundException("Property 'content' not found for key="+key);
            content = (byte[])obj;
            tx.success();
        }
        return content;
    }

    public List<byte[]> loadAll(String label) {
        List<byte[]> byteList = new ArrayList<>();
        ResourceIterator<Node> nodes = graphDb.findNodes(Label.label(label));
        Node n;
        Object obj;
        while(nodes.hasNext()) {
            n = nodes.next();
            obj = n.getProperty("content");
            byteList.add((byte[])obj);
        }
        return byteList;
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

            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                    LOG.info("Stopping...");
                    graphDb.shutdown();
                    LOG.info("Stopped.");
                }
            } );

            return true;
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
