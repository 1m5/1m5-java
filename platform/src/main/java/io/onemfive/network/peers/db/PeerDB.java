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
package io.onemfive.network.peers.db;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.peers.graph.GraphUtil;
import org.neo4j.graphdb.*;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class PeerDB {

    private static final Logger LOG = Logger.getLogger(PeerDB.class.getName());

    private boolean initialized = false;
    private String location;
    private String name;
    private static final String TABLE = "Peer";
    private Properties properties;
    private Connection connection;
    private String dbURL;

    public Boolean savePeer(NetworkPeer p, Boolean autocreate) {
        LOG.info("Saving NetworkPeer...");
        if(p.getId()==null || p.getId().isEmpty()) {
            LOG.warning("NetworkPeer.id is empty. Must have an id for Network Peers to save.");
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
            LOG.info("Creating NetworkPeer in DB...");
            if(p.getId()==null) {
                LOG.warning("Can not insert NetworkPeer into database without an id.");
                return false;
            }
            Statement stmt = null;
            try {
                stmt = connection.createStatement();
                String sql = "insert into "+TABLE+" ("+
                        NetworkPeer.ID+
                        p.getNetwork()==null?"":", "+NetworkPeer.NETWORK+
                        p.getDid().getUsername()==null?"":", "+NetworkPeer.USERNAME+
                        p.getDid().getPublicKey().getAlias()==null?"":", "+NetworkPeer.ALIAS+
                        p.getDid().getPublicKey().getAddress()==null?"":", "+NetworkPeer.ADDRESS+
                        p.getDid().getPublicKey().getFingerprint()==null?"":", "+NetworkPeer.FINGERPRINT+
                        p.getDid().getPublicKey().getType()==null?"":", "+NetworkPeer.KEY_TYPE+
                        " ) values ( "+
                        "'"+p.getId()+"'" +
                        p.getNetwork()==null?"":", '"+p.getNetwork().name()+"'"+
                        p.getDid().getUsername()==null?"":", '"+p.getDid().getUsername()+"'"+
                        p.getDid().getPublicKey().getAlias()==null?"":", '"+p.getDid().getPublicKey().getAlias()+"'"+
                        p.getDid().getPublicKey().getAddress()==null?"":", '"+p.getDid().getPublicKey().getAddress()+"'"+
                        p.getDid().getPublicKey().getFingerprint()==null?"":", '"+p.getDid().getPublicKey().getFingerprint()+"'"+
                        p.getDid().getPublicKey().getType()==null?"":", '"+p.getDid().getPublicKey().getType()+"'";
                LOG.info(sql);
                stmt.execute(sql);
            } catch (SQLException e) {
                LOG.warning(e.getLocalizedMessage());
            } finally {
                if(stmt!=null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
                }
            }
        } else {
            LOG.info("New Peer but autocreate is false, unable to save peer.");
        }
        return true;
    }

    public NetworkPeer loadPeerById(String id) {
        return loadPeerByIdAndNetwork(id, Network.IMS);
    }

    public NetworkPeer loadPeerByIdAndNetwork(String id, Network network) {
        NetworkPeer p = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery("select * from "+TABLE+" where "+NetworkPeer.ID+"='"+id+"' and "+NetworkPeer.NETWORK+"='"+network.name()+"';");
            if(rs.next()) {
                p = new NetworkPeer();
                p.setId(rs.getString(NetworkPeer.ID));
                p.setNetwork(Network.valueOf(rs.getString(NetworkPeer.NETWORK)));
                p.getDid().setUsername(rs.getString(NetworkPeer.USERNAME));
                p.getDid().getPublicKey().setAlias(rs.getString(NetworkPeer.ALIAS));
                p.getDid().getPublicKey().setAddress(rs.getString(NetworkPeer.ADDRESS));
                p.getDid().getPublicKey().setFingerprint(rs.getString(NetworkPeer.FINGERPRINT));
                p.getDid().getPublicKey().setType(rs.getString(NetworkPeer.KEY_TYPE));
            }
        } catch (SQLException e) {
            LOG.warning(e.getLocalizedMessage());
        } finally {
            if(rs!=null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOG.warning(e.getLocalizedMessage());
                }
            if(stmt!=null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.warning(e.getLocalizedMessage());
                }
        }
        return p;
    }

    public NetworkPeer loadPeerByAddress(String address) throws Exception {
        NetworkPeer p = null;
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select * from "+TABLE+" where "+NetworkPeer.ADDRESS+"='"+address+"';");
        if(rs.next()) {
            p = new NetworkPeer();
            p.setId(rs.getString(NetworkPeer.ID));
            p.setNetwork(Network.valueOf(rs.getString(NetworkPeer.NETWORK)));
            p.getDid().setUsername(rs.getString(NetworkPeer.USERNAME));
            p.getDid().getPublicKey().setAlias(rs.getString(NetworkPeer.ALIAS));
            p.getDid().getPublicKey().setAddress(rs.getString(NetworkPeer.ADDRESS));
            p.getDid().getPublicKey().setFingerprint(rs.getString(NetworkPeer.FINGERPRINT));
            p.getDid().getPublicKey().setType(rs.getString(NetworkPeer.KEY_TYPE));
        }
        rs.close();
        stmt.close();
        return p;
    }

    private boolean updatePeer(NetworkPeer p) throws Exception {
        LOG.info("Find and Update Peer Node...");
        boolean updated = false;
        LOG.info("Looking up Node by Id: "+p.getId());
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select * from "+TABLE+" where id="+p.getId());
        boolean update = rs.next();
        rs.close();
        stmt.close();
        if(update) {
            stmt = connection.createStatement();
            String sql = "update "+TABLE+" set "+
                    NetworkPeer.ID+"='"+p.getId()+"'"+
                    p.getNetwork()==null?"":", "+NetworkPeer.NETWORK+"='"+p.getNetwork().name()+"'"+
                    p.getDid().getUsername()==null?"":", "+NetworkPeer.USERNAME+"='"+p.getDid().getUsername()+"'"+
                    p.getDid().getPassphraseHash()==null?"":", "+NetworkPeer.PASSPHRASE_HASH+"='"+p.getDid().getPassphraseHash()+"'"+
                    p.getDid().getPassphraseHashAlgorithm()==null?"":", "+NetworkPeer.PASSPHRASE_HASH_ALG+"='"+p.getDid().getPassphraseHashAlgorithm().getName()+"'"+
                    p.getDid().getPublicKey().getAlias()==null?"":", "+NetworkPeer.ALIAS+"='"+p.getDid().getPublicKey().getAlias()+"'"+
                    p.getDid().getPublicKey().getAddress()==null?"":", "+NetworkPeer.ADDRESS+"='"+p.getDid().getPublicKey().getAddress()+"'"+
                    p.getDid().getPublicKey().getFingerprint()==null?"":", "+NetworkPeer.FINGERPRINT+"='"+p.getDid().getPublicKey().getFingerprint()+"'"+
                    p.getDid().getPublicKey().getType()==null?"":", "+NetworkPeer.KEY_TYPE+"='"+p.getDid().getPublicKey().getType()+"'";
            LOG.info(sql);
            stmt.execute(sql);
            stmt.close();
            updated = true;
        }
        return updated;
    }

    private Map<String,Object> toMap(PropertyContainer n) {
        return GraphUtil.getAttributes(n);
    }

    private NetworkPeer toPeer(PropertyContainer n) {
        NetworkPeer p = new NetworkPeer();
        p.setId((String)n.getProperty("id"));
        return p;
    }

    private NetworkPeer toPeer(Map<String,Object> m) {
        NetworkPeer p = new NetworkPeer();
        p.fromMap(m);
        return p;
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
            LOG.warning("Derby DB location required. Please provide.");
            return false;
        }
        if(name==null) {
            LOG.warning("Derby DB name required. Please provide.");
            return false;
        }
        if(!initialized) {
            this.properties = properties;
            File dbDir = new File(location+"/"+name);
            if(!dbDir.exists() && !dbDir.mkdir()) {
                LOG.warning("Unable to create derby db directory at: "+location+"/"+name);
                return false;
            }
            dbURL = "jdbc:derby:"+location+":"+name;
            try {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver").getConstructor().newInstance();
                connection = DriverManager.getConnection(dbURL+";create=true", properties);
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
                return false;
            }

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
        LOG.info("Stopping...");
        if(connection!=null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        try {
            DriverManager.getConnection(dbURL+";shutdown=true");
        } catch (SQLException e) {
            LOG.warning(e.getLocalizedMessage());
        }
        LOG.info("Stopped.");
        return true;
    }
}
