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
package io.onemfive.network.sensors.i2p;

import io.onemfive.core.HandleResponse;
import io.onemfive.core.Notification;
import io.onemfive.core.Operation;
import io.onemfive.core.RequestReply;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.data.*;
import io.onemfive.data.Envelope;
import io.onemfive.network.Packet;
import io.onemfive.network.Request;
import io.onemfive.network.Response;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.sensors.BaseSession;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.util.DLC;
import io.onemfive.util.JSONParser;
import net.i2p.I2PException;
import net.i2p.client.I2PClientFactory;
import net.i2p.client.I2PSession;
import net.i2p.client.I2PSessionException;
import net.i2p.client.I2PSessionMuxedListener;
import net.i2p.client.datagram.I2PDatagramDissector;
import net.i2p.client.datagram.I2PDatagramMaker;
import net.i2p.client.datagram.I2PInvalidDatagramException;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.Base64;
import net.i2p.data.DataFormatException;
import net.i2p.data.Destination;
import net.i2p.util.SecureFile;
import net.i2p.util.SecureFileOutputStream;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class I2PSensorSession extends BaseSession implements I2PSessionMuxedListener {

    private static final Logger LOG = Logger.getLogger(I2PSensorSession.class.getName());

    // I2CP parameters allowed in the config file
    // Undefined parameters use the I2CP defaults
    private static final String PARAMETER_I2CP_DOMAIN_SOCKET = "i2cp.domainSocket";
    private static final List<String> I2CP_PARAMETERS = Arrays.asList(new String[] {
            PARAMETER_I2CP_DOMAIN_SOCKET,
            "inbound.length",
            "inbound.lengthVariance",
            "inbound.quantity",
            "inbound.backupQuantity",
            "outbound.length",
            "outbound.lengthVariance",
            "outbound.quantity",
            "outbound.backupQuantity",
    });

    private I2PSensor sensor;
    private I2PSession i2pSession;
    private boolean connected = false;
    private I2PSocketManager socketManager;
    private boolean isTest = false;

    public I2PSensorSession(I2PSensor sensor) {
        super();
        this.sensor = sensor;
    }

    /**
     * Initializes session properties
     */
    @Override
    public boolean init(Properties p) {
        super.init(p);
        properties = p;
        LOG.info("Initializing I2P Session....");
        // set tunnel names
        properties.setProperty("inbound.nickname", "I2PSensor");
        properties.setProperty("outbound.nickname", "I2PSensor");
        return true;
    }

    /**
     * Open a Socket with supplied Network Peer
     */
    @Override
    public boolean open(NetworkPeer newLocalI2PPeer) {
        // I2P Sensor currently uses only one internal I2P address thus ignoring any peer passed to this method
        NetworkNode localNode = sensor.getSensorManager().getPeerManager().getLocalNode();
        NetworkPeer localI2PPeer;
        if(localNode.getNetworkPeer(Network.I2P)!=null) {
            localI2PPeer = localNode.getNetworkPeer(Network.I2P);
        } else {
            localI2PPeer = new NetworkPeer(Network.I2P, localNode.getNetworkPeer().getDid().getUsername(), localNode.getNetworkPeer().getDid().getPassphrase());
            localNode.addNetworkPeer(localI2PPeer);
        }
        // read the local destination key from the key file if it exists
        File destinationKeyFile = new File(sensor.getDirectory(), localI2PPeer.getDid().getUsername());
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(destinationKeyFile);
            char[] destKeyBuffer = new char[(int)destinationKeyFile.length()];
            fileReader.read(destKeyBuffer);
            byte[] localDestinationKey = Base64.decode(new String(destKeyBuffer));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(localDestinationKey);
            socketManager = I2PSocketManagerFactory.createDisconnectedManager(inputStream, null, 0, properties);
        } catch (IOException e) {
            LOG.info("Destination key file doesn't exist or isn't readable." + e);
        } catch (I2PSessionException e) {
            // Won't happen, inputStream != null
            e.printStackTrace();
            LOG.warning(e.getLocalizedMessage());
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    LOG.warning("Error closing file: " + destinationKeyFile.getAbsolutePath() + ": " + e);
                }
            }
        }

        // if the local destination key can't be read or is invalid, create a new one
        if (socketManager == null) {
            LOG.info("Creating new local destination key");
            try {
                ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
                I2PClientFactory.createClient().createDestination(arrayStream);
                byte[] localDestinationKey = arrayStream.toByteArray();

                LOG.info("Creating I2P Socket Manager...");
                ByteArrayInputStream inputStream = new ByteArrayInputStream(localDestinationKey);
                socketManager = I2PSocketManagerFactory.createDisconnectedManager(inputStream, null, 0, properties);
                LOG.info("I2P Socket Manager created.");

                destinationKeyFile = new SecureFile(destinationKeyFile.getAbsolutePath());
                if (destinationKeyFile.exists()) {
                    File oldKeyFile = new File(destinationKeyFile.getPath() + "_backup");
                    if (!destinationKeyFile.renameTo(oldKeyFile)) {
                        LOG.warning("Cannot rename destination key file <" + destinationKeyFile.getAbsolutePath() + "> to <" + oldKeyFile.getAbsolutePath() + ">");
                        return false;
                    }
                } else if (!destinationKeyFile.createNewFile()) {
                    LOG.warning("Cannot create destination key file: <" + destinationKeyFile.getAbsolutePath() + ">");
                    return false;
                }

                BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new SecureFileOutputStream(destinationKeyFile)));
                try {
                    fileWriter.write(Base64.encode(localDestinationKey));
                }
                finally {
                    fileWriter.close();
                }
            } catch (I2PException e) {
                LOG.warning("Error creating local destination key: " + e.getLocalizedMessage());
                return false;
            } catch (IOException e) {
                LOG.warning("Error writing local destination key to file: " + e.getLocalizedMessage());
                return false;
            }
        }
        i2pSession = socketManager.getSession();
        if(localI2PPeer.getDid().getPublicKey().getAddress()==null || localI2PPeer.getDid().getPublicKey().getAddress().isEmpty()) {
            Destination localDestination = i2pSession.getMyDestination();
            String address = localDestination.toBase64();
            String fingerprint = localDestination.calculateHash().toBase64();
            String algorithm = localDestination.getPublicKey().getType().getAlgorithmName();
            // Ensure network is correct
            localI2PPeer.setNetwork(Network.I2P);
            // Add destination to PK and update DID info
            localI2PPeer.getDid().setStatus(DID.Status.ACTIVE);
            localI2PPeer.getDid().setDescription("DID for I2PSensorSession");
            localI2PPeer.getDid().setAuthenticated(true);
            localI2PPeer.getDid().setVerified(true);
            localI2PPeer.getDid().getPublicKey().setAlias(localNode.getNetworkPeer().getDid().getUsername());
            localI2PPeer.getDid().getPublicKey().isIdentityKey(true);
            localI2PPeer.getDid().getPublicKey().setAddress(address);
            localI2PPeer.getDid().getPublicKey().setBase64Encoded(true);
            localI2PPeer.getDid().getPublicKey().setFingerprint(fingerprint);
            localI2PPeer.getDid().getPublicKey().setType(algorithm);
            sensor.getSensorManager().getPeerManager().savePeer(localI2PPeer, true);
        }
        LOG.info("I2PSensor Address in base64: " + localI2PPeer.getDid().getPublicKey().getAddress());
        LOG.info("I2PSensor Fingerprint (hash) in base64: " + localI2PPeer.getDid().getPublicKey().getFingerprint());
        return true;
    }

    /**
     * Connect to I2P network
     * @return
     */
    @Override
    public boolean connect() {
        if(!isOpen()) {
            LOG.info("No Socket Manager open.");
            open(null);
        }
        i2pSession = socketManager.getSession();
        LOG.info("I2P Session connecting...");
        long start = System.currentTimeMillis();
        try {
            // Throws I2PSessionException if the connection fails
            i2pSession.connect();
            connected = true;
        } catch (I2PSessionException e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        }
        long end = System.currentTimeMillis();
        long durationMs = end - start;
        LOG.info("I2P Session connected. Took "+(durationMs/1000)+" seconds.");

        i2pSession.addMuxedSessionListener(this, I2PSession.PROTO_ANY, I2PSession.PORT_ANY);

        return true;
    }

    @Override
    public boolean disconnect() {
        if(i2pSession!=null) {
            try {
                i2pSession.destroySession();
                connected = false;
            } catch (I2PSessionException e) {
                LOG.warning(e.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    public boolean isOpen() {
        return socketManager!=null;
    }

    @Override
    public boolean isConnected() {
        boolean isConnected = i2pSession != null && connected && !i2pSession.isClosed();
        if(!isConnected) connected = false;
        return isConnected;
    }

    @Override
    public boolean close() {
        disconnect();
        socketManager.destroySocketManager();
        return true;
    }

    @Override
    public Boolean send(Packet packet) {
        if(packet == null){
            LOG.warning("No Packet.");
            packet.statusCode = ServiceMessage.REQUEST_REQUIRED;
            return false;
        }
        if(packet.getToPeer() == null) {
            LOG.warning("No Peer for I2P found in toDID while sending to I2P.");
            packet.statusCode = Packet.DESTINATION_PEER_REQUIRED;
            return false;
        }
        if(packet.getToPeer().getNetwork()!=Network.I2P) {
            LOG.warning("Not a packet for I2P.");
            packet.statusCode = Packet.DESTINATION_PEER_WRONG_NETWORK;
            return false;
        }
        String content = packet.toJSON();
        LOG.info("Content to send: "+content);
        if(content.length() > 31500) {
            // Just warn for now
            // TODO: Split into multiple serialized packets
            LOG.warning("Content longer than 31.5kb. May have issues.");
        }

        try {
            Destination toDestination = i2pSession.lookupDest(packet.getToPeer().getDid().getPublicKey().getAddress());
            if(toDestination == null) {
                LOG.warning("I2P Peer To Destination not found.");
                packet.statusCode = Packet.DESTINATION_PEER_NOT_FOUND;
                return false;
            }
            I2PDatagramMaker m = new I2PDatagramMaker(i2pSession);
            byte[] payload = m.makeI2PDatagram(content.getBytes());
            if(i2pSession.sendMessage(toDestination, payload, I2PSession.PROTO_UNSPECIFIED, I2PSession.PORT_ANY, I2PSession.PORT_ANY)) {
                LOG.info("I2P Message sent.");
                return true;
            } else {
                LOG.warning("I2P Message sending failed.");
                packet.statusCode = Packet.SENDING_FAILED;
                return false;
            }
        } catch (I2PSessionException e) {
            String errMsg = "Exception while sending I2P message: " + e.getLocalizedMessage();
            LOG.warning(errMsg);
            packet.exception = e;
            packet.errorMessage = errMsg;
            if("Already closed".equals(e.getLocalizedMessage())) {
                LOG.info("I2P Connection closed. Could be no internet access or getting blocked. Assume blocked for re-route. If not blocked, I2P will automatically re-establish connection when network access returns.");
                sensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
            }
            return false;
        }
    }

    /**
     * Will be called only if you register via
     * setSessionListener() or addSessionListener().
     * And if you are doing that, just use I2PSessionListener.
     *
     * If you register via addSessionListener(),
     * this will be called only for the proto(s) and toport(s) you register for.
     *
     * After this is called, the client should call receiveMessage(msgId).
     * There is currently no method for the client to reject the message.
     * If the client does not call receiveMessage() within a timeout period
     * (currently 30 seconds), the session will delete the message and
     * log an error.
     *
     * @param session session to notify
     * @param msgId message number available
     * @param size size of the message - why it's a long and not an int is a mystery
     */
    @Override
    public void messageAvailable(I2PSession session, int msgId, long size) {
        LOG.info("Message received by I2P Sensor...");
        byte[] msg;
        try {
            msg = session.receiveMessage(msgId);
        } catch (I2PSessionException e) {
            LOG.warning("Can't get new message from I2PSession: " + e.getLocalizedMessage());
            return;
        }
        if (msg == null) {
            LOG.warning("I2PSession returned a null message: msgId=" + msgId + ", size=" + size + ", " + session);
            return;
        }
//        if(sensor.getStatus()==SensorStatus.NETWORK_CONNECTED) {
//            sensor.updateStatus(SensorStatus.NETWORK_VERIFIED);
//        }
        try {
            LOG.info("Loading I2P Datagram...");
            I2PDatagramDissector d = new I2PDatagramDissector();
            d.loadI2PDatagram(msg);
            LOG.info("I2P Datagram loaded.");
            byte[] payload = d.getPayload();
            String strPayload = new String(payload);
            LOG.info("Getting sender as I2P Destination...");
            Destination sender = d.getSender();
            String address = sender.toBase64();
            String fingerprint = sender.getHash().toBase64();
            LOG.info("Received I2P Message:\n\tFrom: " + address +"\n\tContent:\n\t" + strPayload);
            Map<String,Object> pm = (Map<String,Object>) JSONParser.parse(strPayload);
            Packet packet;
            Envelope e = null;
            boolean sendAsEvent = true;
            if(pm.get("type")!=null) {
                String type = (String)pm.get("type");
                LOG.info("Type discovered: "+type);
                packet = (Packet)Class.forName(type).getConstructor().newInstance();
                packet.fromMap(pm);
                e = packet.getEnvelope();
                if(e != null && e.getRoute() != null && e.getRoute().getOperation() != null) {
                    Operation op = (Operation) Class.forName(e.getRoute().getOperation()).getConstructor().newInstance();
                    if (op instanceof NetworkOp) {
                        handleNetworkOpPacket(packet, (NetworkOp) op);
                    } else {
                        sendAsEvent = false;
                    }
                }
            }
            if(sendAsEvent) {
                LOG.info("Creating Event Message for Notification Service...");
                e = Envelope.eventFactory(EventMessage.Type.TEXT);
                NetworkPeer from = new NetworkPeer(Network.I2P);
                from.getDid().getPublicKey().setAddress(address);
                from.getDid().getPublicKey().setFingerprint(fingerprint);
                e.setDID(from.getDid());
                EventMessage m = (EventMessage) e.getMessage();
                m.setName(fingerprint);
                m.setMessage(strPayload);
                DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, e);
            }
            if(!sensor.sendIn(e)) {
                LOG.warning("Unsuccessful sending of envelope to bus.");
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
            LOG.warning("Invalid datagram received: " + e.getLocalizedMessage());
        } catch (I2PInvalidDatagramException e) {
            e.printStackTrace();
            LOG.warning("Datagram failed verification: " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("Error processing datagram: " + e.getLocalizedMessage());
        }
    }

    /**
     * Instruct the client that the given session has received a message
     *
     * Will be called only if you register via addMuxedSessionListener().
     * Will be called only for the proto(s) and toport(s) you register for.
     *
     * After this is called, the client should call receiveMessage(msgId).
     * There is currently no method for the client to reject the message.
     * If the client does not call receiveMessage() within a timeout period
     * (currently 30 seconds), the session will delete the message and
     * log an error.
     *
     * Only one listener is called for a given message, even if more than one
     * have registered. See I2PSessionDemultiplexer for details.
     *
     * @param session session to notify
     * @param msgId message number available
     * @param size size of the message - why it's a long and not an int is a mystery
     * @param proto 1-254 or 0 for unspecified
     * @param fromPort 1-65535 or 0 for unspecified
     * @param toPort 1-65535 or 0 for unspecified
     */
    @Override
    public void messageAvailable(I2PSession session, int msgId, long size, int proto, int fromPort, int toPort) {
//        if (proto == I2PSession.PROTO_DATAGRAM || proto == I2PSession.PROTO_STREAMING)
        messageAvailable(session, msgId, size);
//        else
//            LOG.warning("Received unhandled message with proto="+proto+" and id="+msgId);
    }

    @Override
    public void handleNetworkOpPacket(Packet packet, NetworkOp op) {
        op.setSensorManager(sensor.getSensorManager());
        if(op instanceof RequestReply) {
            // Handle incoming Request returning a Response
            sensor.sendOut(((RequestReply)op).operate((Request)packet));
        } else if(op instanceof HandleResponse) {
            boolean success = ((HandleResponse)op).operate((Response)packet);
            if(!success) {
                LOG.warning("Handling response op ("+op.toString()+") failed: "+packet);
            }
        } else if(op instanceof Notification) {
            ((Notification)op).notify(packet);
        }
    }

    /**
     * Instruct the client that the session specified seems to be under attack
     * and that the client may wish to move its destination to another router.
     * All registered listeners will be called.
     *
     * Unused. Not fully implemented.
     *
     * @param i2PSession session to report abuse to
     * @param severity how bad the abuse is
     */
    @Override
    public void reportAbuse(I2PSession i2PSession, int severity) {
        LOG.warning("I2P Session reporting abuse. Severity="+severity);
        sensor.reportRouterStatus();
    }

    /**
     * Notify the client that the session has been terminated.
     * All registered listeners will be called.
     *
     * @param session session to report disconnect to
     */
    @Override
    public void disconnected(I2PSession session) {
        LOG.warning("I2P Session reporting disconnection.");
        sensor.reportRouterStatus();
    }

    /**
     * Notify the client that some throwable occurred.
     * All registered listeners will be called.
     *
     * @param session session to report error occurred
     * @param message message received describing error
     * @param throwable throwable thrown during error
     */
    @Override
    public void errorOccurred(I2PSession session, String message, Throwable throwable) {
        LOG.severe("Router says: "+message+": "+throwable.getLocalizedMessage());
        sensor.reportRouterStatus();
    }

    private Properties getI2CPOptions() {
        Properties opts = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (I2CP_PARAMETERS.contains(entry.getKey()))
                opts.put(entry.getKey(), entry.getValue());
        }
        return opts;
    }

}
