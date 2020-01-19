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

    public Boolean delete(String label, String key) {
        try (Transaction tx = graphDb.beginTx()) {
            Node n = graphDb.findNode(Label.label(label), "name", key);
            if(n != null)
                n.delete();
            tx.success();
        }
        return true;
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

    @Override
    public List<byte[]> loadRange(String label, int start, int numberEntries) {
        return null;
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
