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
import io.onemfive.network.NetworkState;
import io.onemfive.network.Request;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.network.sensors.clearnet.ClearnetSensor;
import io.onemfive.network.sensors.tor.TOR;
import io.onemfive.network.sensors.tor.TORSensor;
import io.onemfive.network.sensors.tor.external.control.DebuggingEventHandler;
import io.onemfive.network.sensors.tor.external.control.TorControlConnection;
import io.onemfive.util.DLC;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class TORExternal implements TOR {

    private static final Logger LOG = Logger.getLogger(TORExternal.class.getName());

    public static final String HOST = "127.0.0.1";
    public static final Integer PORT_SOCKS = 9050;
    public static final Integer PORT_CONTROL = 9100;
    public static final Integer PORT_HIDDEN_SERVICE = 9151;

    private TORSensor torSensor;
    private NetworkState networkState;
    private SensorManager sensorManager;
    private ClearnetSensor clearnetSensor;
    private TorControlConnection controlConnection;

    public TORExternal(TORSensor torSensor, NetworkState networkState, SensorManager sensorManager) {
        this.torSensor = torSensor;
        this.networkState = networkState;
        this.sensorManager = sensorManager;
        clearnetSensor = new ClearnetSensor(sensorManager, Network.TOR);
        clearnetSensor.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(HOST, PORT_SOCKS)));
    }

    @Override
    public boolean sendOut(NetworkPacket packet) {
        if(!clearnetSensor.sendOut(packet)) {
            handleFailure(packet.getEnvelope().getMessage());
            return false;
        }
        return true;
    }

    private TorControlConnection getControlConnection() throws IOException {
        Socket s = new Socket("127.0.0.1", 9051);
        TorControlConnection conn = new TorControlConnection(s);
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
        if(sendOut(request)) {
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
                            torSensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
                            blocked = true;
                            break;
                        }
                        case "408": {
                            // Request Timeout
                            LOG.info("Received HTTP 408 response (Tor): Request Timeout. Tor Sensor considered blocked.");
                            torSensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
                            blocked = true;
                            break;
                        }
                        case "410": {
                            // Gone
                            LOG.info("Received HTTP 410 response (Tor): Gone. Tor Sensor considered blocked.");
                            torSensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
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
                            torSensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
                            blocked = true;
                            break;
                        }
                        case "511": {
                            // Network Authentication Required
                            LOG.info("Received HTTP511 response (Tor): network authentication required. Tor Sensor considered blocked.");
                            torSensor.updateStatus(SensorStatus.NETWORK_BLOCKED);
                            blocked = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean start(Properties properties) {
        properties.setProperty("1m5.sensors.clearnet.client.enable","true");
        if(clearnetSensor.start(properties)) {
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
                controlConnection.setEventHandler(new DebuggingEventHandler(LOG));
                controlConnection.setEvents(Arrays.asList("EXTENDED", "CIRC", "ORCONN", "INFO", "NOTICE", "WARN", "ERR", "HS_DESC", "HS_DESC_CONTENT"));

                NetworkNode localNode = sensorManager.getPeerManager().getLocalNode();
                NetworkPeer imsPeer = localNode.getNetworkPeer();
                NetworkPeer localTORPeer = sensorManager.getPeerManager().loadPeerByIdAndNetwork(imsPeer.getId(), Network.TOR);
                if(localTORPeer==null) {
                    localTORPeer = new NetworkPeer(Network.TOR, localNode.getNetworkPeer().getDid().getUsername(), localNode.getNetworkPeer().getDid().getPassphrase());
                    localTORPeer.setId(imsPeer.getId());
                }
                localNode.addNetworkPeer(localTORPeer);
                if(localTORPeer.getDid().getPublicKey().getAddress()==null) {
                    TorControlConnection.CreateHiddenServiceResult result = controlConnection.createHiddenService(10026);
//                    sb.append("\n\tTOR Hidden Service Address: " + result.serviceID);
//                    sb.append("\n\tPrivateKey: " + result.privateKey);
                    controlConnection.destroyHiddenService(result.serviceID);
                    result = controlConnection.createHiddenService(10026, result.privateKey);
                    sb.append("\n\tTOR Hidden Service Address: " + result.serviceID);
//                    sb.append("\n\tPrivateKey: " + result.privateKey);
                    localTORPeer.setId(localNode.getNetworkPeer().getId());
                    localTORPeer.getDid().getPublicKey().setFingerprint(result.serviceID); // used as key
                    localTORPeer.getDid().getPublicKey().setAddress(result.serviceID);
                    networkState.localPeer = localTORPeer;
                }
                sensorManager.getPeerManager().savePeer(localTORPeer, true);
                LOG.info(sb.toString());
            } catch (IOException ex) {
                LOG.warning(ex.getLocalizedMessage());
                torSensor.updateStatus(SensorStatus.NETWORK_UNAVAILABLE);
                return false;
            }
        } else {
            LOG.warning("Clearnet Sensor failed to start. Unable to start Tor Sensor External (Clearnet Sensor set up with TOR as proxy).");
            return false;
        }
        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean shutdown() {
        return clearnetSensor.shutdown();
    }

    @Override
    public boolean gracefulShutdown() {
        return clearnetSensor.gracefulShutdown();
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
