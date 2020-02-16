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
package io.onemfive.network.sensors.tor.external;

import io.onemfive.data.*;
import io.onemfive.network.NetworkPacket;
import io.onemfive.network.Request;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.network.sensors.clearnet.ClearnetSession;
import io.onemfive.network.sensors.tor.TORHiddenService;
import io.onemfive.network.sensors.tor.TORSensor;
import io.onemfive.network.sensors.tor.external.control.DebuggingEventHandler;
import io.onemfive.network.sensors.tor.external.control.TORControlConnection;
import io.onemfive.util.DLC;
import io.onemfive.util.FileUtil;
import net.i2p.data.Base64;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class TORSensorSessionExternal extends ClearnetSession {

    private static final Logger LOG = Logger.getLogger(TORSensorSessionExternal.class.getName());

    public static final String HOST = "127.0.0.1";
    public static final Integer PORT_SOCKS = 9050;
    public static final Integer PORT_CONTROL = 9100;
    public static final Integer PORT_HIDDEN_SERVICE = 9151;

    private TORControlConnection controlConnection;
    private TORSensor sensor;

    public TORSensorSessionExternal(TORSensor torSensor) {
        super(torSensor, new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(HOST, PORT_SOCKS)));
        this.sensor = torSensor;
        clientsEnabled = true; // Need to use clients proxied
        serverEnabled = false; // Embedded Server not used; use external TOR daemon
    }

    /**
     * Create TOR hidden service using external TOR daemon.
     * Supplied username will be used as local file name to save private key to.
     */
    @Override
    public boolean open(String privateKeyName) {
        NetworkNode localNode = sensor.getSensorManager().getPeerManager().getLocalNode();
        NetworkPeer localTORPeer;
        if(localNode.getNetworkPeer(Network.TOR)!=null) {
            localTORPeer = localNode.getNetworkPeer(Network.TOR);
        } else {
            localTORPeer = new NetworkPeer(Network.TOR, localNode.getNetworkPeer().getDid().getUsername(), localNode.getNetworkPeer().getDid().getPassphrase());
            localNode.addNetworkPeer(localTORPeer);
        }
        // read the local TOR address key from the address file if it exists
        String torSensorDir = properties.getProperty("1m5.dir.sensors.tor");
        File privKeyFile = new File(torSensorDir, privateKeyName);
        FileReader fileReader = null;
        String json = null;
        try {
            fileReader = new FileReader(privKeyFile);
            char[] addressBuffer = new char[(int)privKeyFile.length()];
            fileReader.read(addressBuffer);
            json = new String(addressBuffer);
        } catch (IOException e) {
            LOG.info("Private key file doesn't exist or isn't readable." + e);
        } catch (Exception e) {
            // Won't happen, inputStream != null
            LOG.warning(e.getLocalizedMessage());
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    LOG.warning("Error closing file: " + privKeyFile.getAbsolutePath() + ": " + e);
                }
            }
        }
        TORHiddenService hiddenService;
        try {
            controlConnection = getControlConnection();
//                Map<String, String> m = conn.getInfo(Arrays.asList("stream-status", "orconn-status", "circuit-status", "version"));
            Map<String, String> m = controlConnection.getInfo(Arrays.asList("version"));
            StringBuilder sb = new StringBuilder();
            sb.append("TOR config:");
            for (Iterator<Map.Entry<String, String>> i = m.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<String, String> e = i.next();
                sb.append("\n\t"+e.getKey()+"="+e.getValue());
            }
            LOG.info(sb.toString());
            controlConnection.setEventHandler(new DebuggingEventHandler(LOG));
            controlConnection.setEvents(Arrays.asList("EXTENDED", "CIRC", "ORCONN", "INFO", "NOTICE", "WARN", "ERR", "HS_DESC", "HS_DESC_CONTENT"));
            if(json==null) {
                // Private key file doesn't exist or is unreadable so create a new hidden service
                hiddenService = controlConnection.createHiddenService(TORHiddenService.randomTORPort());
                LOG.info("TOR Hidden Service Created: " + hiddenService.serviceID + " on port: "+hiddenService.port);
//                controlConnection.destroyHiddenService(hiddenService.serviceID);
//                hiddenService = controlConnection.createHiddenService(hiddenService.port, hiddenService.privateKey);
//                LOG.info("TOR Hidden Service Created: " + hiddenService.serviceID + " on port: "+hiddenService.port);
                // Now save the private key
                if(!privKeyFile.exists() && !privKeyFile.createNewFile()) {
                    LOG.warning("Unable to create file: "+privKeyFile.getAbsolutePath());
                    return false;
                }
                FileUtil.writeFile(hiddenService.toJSON().getBytes(), privKeyFile.getAbsolutePath());
            } else {
                hiddenService = new TORHiddenService();
                hiddenService.fromJSON(json);
                if(controlConnection.isHSAvailable(hiddenService.serviceID)) {
                    LOG.info("TOR Hidden service available: "+hiddenService.serviceID + " on port: "+hiddenService.port);
                } else {
                    LOG.info("TOR Hidden service not available; creating: "+hiddenService.serviceID);
                    hiddenService = controlConnection.createHiddenService(hiddenService.port, hiddenService.privateKey);
                    LOG.info("TOR Hidden Service created: " + hiddenService.serviceID+ " on port: "+hiddenService.port);
                }
            }
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        } catch (NoSuchAlgorithmException e) {
            LOG.warning("TORAlgorithm not supported: "+e.getLocalizedMessage());
            return false;
        }
        address = hiddenService.serviceID;
        localTORPeer.setId(localNode.getNetworkPeer().getId());
        localTORPeer.getDid().getPublicKey().setFingerprint(hiddenService.serviceID); // used as key
        localTORPeer.getDid().getPublicKey().setAddress(hiddenService.serviceID);
        sensor.getNetworkState().localPeer = localTORPeer;
        return false;
    }

    @Override
    public Boolean send(NetworkPacket packet) {
        if(!sensor.sendOut(packet)) {
            handleFailure(packet.getEnvelope().getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Boolean send(NetworkOp op) {
        return sensor.sendOut(op);
    }

    private TORControlConnection getControlConnection() throws IOException {
        Socket s = new Socket("127.0.0.1", 9051);
        TORControlConnection conn = new TORControlConnection(s);
        conn.authenticate(new byte[0]);
        return conn;
    }

    public void get(URL url) {
        // Get URL
        Request request = new Request();
        Envelope envelope = Envelope.documentFactory();
        envelope.setURL(url);
        envelope.setAction(Envelope.Action.GET);
        request.setEnvelope(envelope);
        if(send(request)) {
            byte[] content = (byte[]) DLC.getContent(envelope);
            LOG.info("Content length: "+content.length);
            LOG.info("Content: "+new String(content));
        }
    }

    protected void handleFailure(Message m) {
        if(m!=null && m.getErrorMessages()!=null && m.getErrorMessages().size()>0) {
            boolean blocked = false;
            for (String err : m.getErrorMessages()) {
                LOG.warning("HTTP Error Message (Tor): " + err);
                if(!blocked) {
                    switch (err) {
                        case "403": {
                            // Forbidden
                            LOG.info("Received HTTP 403 response (Tor): Forbidden. Tor Sensor considered blocked.");
                            sensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
                            blocked = true;
                            break;
                        }
                        case "408": {
                            // Request Timeout
                            LOG.info("Received HTTP 408 response (Tor): Request Timeout. Tor Sensor considered blocked.");
                            sensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
                            blocked = true;
                            break;
                        }
                        case "410": {
                            // Gone
                            LOG.info("Received HTTP 410 response (Tor): Gone. Tor Sensor considered blocked.");
                            sensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
                            blocked = true;
                            break;
                        }
                        case "418": {
                            // I'm a teapot
                            LOG.warning("Received HTTP 418 response (Tor): I'm a teapot. Tor Sensor ignoring.");
                            break;
                        }
                        case "451": {
                            // Unavailable for legal reasons; your IP address might be denied access to the resource
                            LOG.info("Received HTTP 451 response (Tor): unavailable for legal reasons. Tor Sensor considered blocked.");
                            // Notify Sensor Manager Tor is getting blocked
                            sensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
                            blocked = true;
                            break;
                        }
                        case "511": {
                            // Network Authentication Required
                            LOG.info("Received HTTP511 response (Tor): network authentication required. Tor Sensor considered blocked.");
                            sensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
                            blocked = true;
                            break;
                        }
                    }
                }
            }
        }
    }

//    public static void main(String[] args) {
//        Properties p = new Properties();
//        p.setProperty("1m5.dir.sensors","/home/objectorange/1m5/platform/services/io.onemfive.network.NetworkService/sensors");
//        SimpleTorSensor s = new SimpleTorSensor();
//        s.start(p);
//        try {
//            URL duckduckGoOnion = new URL("https://3g2upl4pq6kufc4m.onion/");
//            s.get(duckduckGoOnion);
//        } catch (MalformedURLException e) {
//            System.out.println(e.getLocalizedMessage());
//        }

//        try {
//            TorControlConnection conn = getConnection(args);
//            Map<String,String> m = conn.getInfo(Arrays.asList("stream-status","orconn-status","circuit-status","version"));
//            for (Iterator<Map.Entry<String, String>> i = m.entrySet().iterator(); i.hasNext(); ) {
//                Map.Entry<String,String> e = i.next();
//                System.out.println("KEY: "+e.getKey());
//                System.out.println("VAL: "+e.getValue());
//            }
//
//            conn.setEventHandler(new DebuggingEventHandler(LOG));
//            conn.setEvents(Arrays.asList("EXTENDED", "CIRC", "ORCONN", "INFO", "NOTICE", "WARN", "ERR", "HS_DESC", "HS_DESC_CONTENT" ));
//            TorControlConnection.CreateHiddenServiceResult result = conn.createHiddenService(10026);
//            System.out.println("ServiceID: "+result.serviceID);
//            System.out.println("PrivateKey: "+result.privateKey);
//            conn.destroyHiddenService(result.serviceID);
//            result = conn.createHiddenService(10026, result.privateKey);
//            System.out.println("ServiceID: "+result.serviceID);
//            System.out.println("PrivateKey: "+result.privateKey);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
