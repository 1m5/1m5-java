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
import io.onemfive.data.NetworkNode;
import io.onemfive.data.NetworkPeer;
import io.onemfive.util.RandomUtil;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

public class PeerDB {

    private static final Logger LOG = Logger.getLogger(PeerDB.class.getName());

    private boolean initialized = false;
    private String location;
    private String name;
    private static final String TABLE = "Peer";
    private Properties properties;
    private Connection connection;
    private String dbURL;

    private PreparedStatement peersCountByNetwork;
    private final Object peersCountByNetworkLock = new Object();
    private PreparedStatement peersById;
    private final Object peersByIdLock = new Object();
    private PreparedStatement peersByNetwork;
    private final Object peersByNetworkLock = new Object();
    private PreparedStatement peerByIdAndNetwork;
    private final Object peerByIdAndNetworkLock = new Object();
    private PreparedStatement peerByAddress;
    private final Object peerByAddressLock = new Object();
    private PreparedStatement peerByFingerprint;
    private final Object peerByFingerprintLock = new Object();
    private PreparedStatement peerInsertPS;
    private final Object peerInsertPSLock = new Object();
    private PreparedStatement peerUpdatePS;
    private final Object peerUpdatePSLock = new Object();

    // Considering this app is highly multi-threaded
    // and it's using shared prepared statements,
    // make thread-safe by providing a lock Object
    // to synchronize on so that prepared statements
    // do not get used simultaneously causing data
    // corruption.
    private static final String PEER_TABLE_DDL = "create table "+TABLE+"(id varchar(256), network varchar(16), username varchar(32), alias varchar(32), address varchar(4096), fingerprint varchar(256), type varchar(32))";

    public Boolean savePeer(NetworkPeer p, Boolean autocreate) {
        LOG.info("Saving NetworkPeer...");
        if(p.getId()==null || p.getId().isEmpty()) {
            LOG.warning("NetworkPeer.id is empty. Must have an id for Network Peers to save.");
            return false;
        }
        boolean update = false;
        ResultSet rs = null;
        LOG.info("Looking up Node by Id: "+p.getId());
        synchronized (peerByFingerprintLock) {
            try {
                peerByFingerprint.clearParameters();
                peerByFingerprint.setString(1, p.getDid().getPublicKey().getFingerprint());
                rs = peerByFingerprint.executeQuery();
                update = rs.next();
            } catch (SQLException e) {
                LOG.warning(e.getLocalizedMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
                }
            }
        }
        if(update) {
            LOG.info("Find and Update Peer Node...");
            synchronized (peerUpdatePSLock) {
                try {
                    peerUpdatePS.clearParameters();
                    peerUpdatePS.setString(1, p.getId());
                    peerUpdatePS.setString(2, p.getNetwork().name());
                    peerUpdatePS.setString(3, p.getDid().getUsername());
                    peerUpdatePS.setString(4, p.getDid().getPublicKey().getAlias());
                    peerUpdatePS.setString(5, p.getDid().getPublicKey().getAddress());
                    peerUpdatePS.setString(6, p.getDid().getPublicKey().getFingerprint());
                    peerUpdatePS.setString(7, p.getDid().getPublicKey().getType());
                    peerUpdatePS.setString(8, p.getId());
                    peerUpdatePS.executeUpdate();
                } catch (SQLException e) {
                    LOG.warning(e.getLocalizedMessage());
                    return false;
                }
            }
        } else if(autocreate) {
            LOG.info("Creating NetworkPeer in DB...");
            if(p.getNetwork()==null) {
                LOG.warning("Can not insert NetworkPeer into database without a network.");
                return false;
            }
            synchronized (peerInsertPSLock) {
                try {
                    peerInsertPS.clearParameters();
                    peerInsertPS.setString(1, p.getId());
                    peerInsertPS.setString(2, p.getNetwork().name());
                    peerInsertPS.setString(3, p.getDid().getUsername());
                    peerInsertPS.setString(4, p.getDid().getPublicKey().getAlias());
                    peerInsertPS.setString(5, p.getDid().getPublicKey().getAddress());
                    peerInsertPS.setString(6, p.getDid().getPublicKey().getFingerprint());
                    peerInsertPS.setString(7, p.getDid().getPublicKey().getType());
                    peerInsertPS.executeUpdate();
                } catch (SQLException e) {
                    LOG.warning(e.getLocalizedMessage());
                    return false;
                }
            }
        } else {
            LOG.warning("New Peer but autocreate is false, unable to save peer.");
        }
        return true;
    }

    public NetworkPeer randomPeer(Network network) {
        NetworkPeer p = null;
        ResultSet rs = null;
        int total = 0;
        synchronized (peersCountByNetworkLock) {
            try {
                peersCountByNetwork.clearParameters();
                peersCountByNetwork.setString(1, network.name());
                rs = peersCountByNetwork.executeQuery();
                if (rs.next()) {
                    total = rs.getInt(0);
                }
            } catch (SQLException e) {
                LOG.warning(e.getLocalizedMessage());
            } finally {
                if (rs != null)
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
            }
        }
        int random = RandomUtil.nextRandomInteger(0, total);
        rs = null;
        synchronized (peersByNetworkLock) {
            try {
                peersByNetwork.clearParameters();
                peersByNetwork.setString(1, network.name());
                peersByNetwork.execute("set schema 'SAMP'");
                rs = peersByNetwork.executeQuery();
                if (rs.first()) {
                    rs.absolute(random);
                    p = toPeer(rs);
                }
            } catch (SQLException e) {
                LOG.warning(e.getLocalizedMessage());
            } finally {
                if (rs != null)
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
            }
        }
        return p;
    }

    public NetworkNode loadNode(String id) {
        NetworkNode n = null;
        ResultSet rs = null;
        synchronized (peersByIdLock) {
            try {
                peersById.clearParameters();
                peersById.setString(1, id);
                rs = peersById.executeQuery();
                while (rs.next()) {
                    if (n == null)
                        n = new NetworkNode();
                    n.addNetworkPeer(toPeer(rs));
                }
            } catch (SQLException e) {
                LOG.warning(e.getLocalizedMessage());
            } finally {
                if (rs != null)
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
            }
        }
        LOG.info("Node loaded:\n\t"+n);
        return n;
    }

    public NetworkPeer loadPeerById(String id) {
        return loadPeerByIdAndNetwork(id, Network.IMS);
    }

    public NetworkPeer loadPeerByIdAndNetwork(String id, Network network) {
        NetworkPeer p = null;
        ResultSet rs = null;
        synchronized (peerByIdAndNetworkLock) {
            try {
                peerByIdAndNetwork.clearParameters();
                peerByIdAndNetwork.setString(1, id);
                peerByIdAndNetwork.setString(2, network.name());
                rs = peerByIdAndNetwork.executeQuery();
                if (rs.next()) {
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
                if (rs != null)
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
            }
        }
        return p;
    }

    public NetworkPeer loadPeerByAddress(String address) {
        NetworkPeer p = null;
        ResultSet rs = null;
        synchronized (peerByAddressLock) {
            try {
                peerByAddress.clearParameters();
                peerByAddress.setString(1, address);
                rs = peerByAddress.executeQuery();
                if (rs.next()) {
                    p = new NetworkPeer();
                    p.setId(rs.getString(NetworkPeer.ID));
                    p.setNetwork(Network.valueOf(rs.getString(NetworkPeer.NETWORK)));
                    p.getDid().setUsername(rs.getString(NetworkPeer.USERNAME));
                    p.getDid().getPublicKey().setAlias(rs.getString(NetworkPeer.ALIAS));
                    p.getDid().getPublicKey().setAddress(rs.getString(NetworkPeer.ADDRESS));
                    p.getDid().getPublicKey().setFingerprint(rs.getString(NetworkPeer.FINGERPRINT));
                    p.getDid().getPublicKey().setType(rs.getString(NetworkPeer.KEY_TYPE));
                }
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
                }
            }
        }
        return p;
    }

    private NetworkPeer toPeer(ResultSet rs) throws SQLException {
        NetworkPeer p = new NetworkPeer();
        p.setId(rs.getString(NetworkPeer.ID));
        p.setNetwork(Network.valueOf(rs.getString(NetworkPeer.NETWORK)));
        p.getDid().setUsername(rs.getString(NetworkPeer.USERNAME));
        p.getDid().getPublicKey().setAlias(rs.getString(NetworkPeer.ALIAS));
        p.getDid().getPublicKey().setAddress(rs.getString(NetworkPeer.ADDRESS));
        p.getDid().getPublicKey().setFingerprint(rs.getString(NetworkPeer.FINGERPRINT));
        p.getDid().getPublicKey().setType(rs.getString(NetworkPeer.KEY_TYPE));
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

    public boolean init(Properties p) {
        if(location==null) {
            LOG.warning("Derby DB location required. Please provide.");
            return false;
        }
        if(name==null) {
            LOG.warning("Derby DB name required. Please provide.");
            return false;
        }
        if(!initialized) {
            this.properties = p;
            System.setProperty("derby.system.home",location);
            dbURL = "jdbc:derby:"+name;
            try {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver").getConstructor().newInstance();
                connection = DriverManager.getConnection(dbURL+";create=true", properties);
//                connection.setAutoCommit(false);
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

            Statement stmt = null;
            try {
                stmt = connection.createStatement();
                stmt.execute(PEER_TABLE_DDL);
            } catch (SQLException e) {
                LOG.info(e.getLocalizedMessage());
            } finally {
                if(stmt!=null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
                }
            }

            try {
                peersCountByNetwork = connection.prepareStatement("select count(id) from Peer where network=?");
                peersById = connection.prepareStatement("select * from Peer where id=?");
                peersByNetwork = connection.prepareStatement("select * from Peer where network=?", TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                peerByIdAndNetwork = connection.prepareStatement("select * from Peer where id=? and network=?");
                peerByFingerprint = connection.prepareStatement("select * from Peer where fingerprint=?");
                peerByAddress = connection.prepareStatement("select * from Peer where address=?");
                peerInsertPS = connection.prepareStatement("insert into Peer values (?, ?, ?, ?, ?, ?, ?)");
                peerUpdatePS = connection.prepareStatement("update Peer set id=?, network=?, username=?, alias=?, address=?, fingerprint=?, type=? where id=?");
            } catch (SQLException e) {
                LOG.info(e.getLocalizedMessage());
            }

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
