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
package io.onemfive.network.sensors.clearnet;

import io.onemfive.core.Config;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.notification.SubscriptionRequest;
import io.onemfive.data.*;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.Packet;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorSession;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.util.BrowserUtil;
import io.onemfive.util.DLC;
import io.onemfive.util.Multipart;
import okhttp3.*;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Clearnet access acting as a Client and Server
 */
public class ClearnetSensor extends BaseSensor {

    protected static final Set<String> trustedHosts = new HashSet<>();

    protected static final HostnameVerifier trustAllHostnameVerifier = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    protected static final HostnameVerifier hostnameVerifier = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return trustedHosts.contains(hostname);
        }
    };

    protected X509TrustManager trustAllX509TrustManager = new X509TrustManager() {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
    };

    // Create a trust manager that does not validate certificate chains
    protected TrustManager[] trustAllTrustManager = new TrustManager[]{trustAllX509TrustManager};

    protected ConnectionSpec httpSpec;
    protected OkHttpClient httpClient;

    protected ConnectionSpec httpsCompatibleSpec;
    protected OkHttpClient httpsCompatibleClient;

    protected ConnectionSpec httpsStrongSpec;
    protected OkHttpClient httpsStrongClient;

    protected Proxy proxy = null;

    public static final String HANDLER_ID = "1m5.sensors.clearnet.handler.id";

    /**
     * Configuration of Servers in the form:
     *      name, port, launch on start, concrete implementation of io.onemfive.network.sensors.clearnet.AsynchronousEnvelopeHandler, run websocket, relative resource directory|n,...}
     */
    public static final String SERVERS_CONFIG = "1m5.sensors.clearnet.servers.config";

    private static final Logger LOG = Logger.getLogger(ClearnetSensor.class.getName());

    private boolean isTest = false;
    private boolean clientsEnabled = false;
    private boolean serversEnabled = false;

    private final Map<String,Server> servers = new HashMap<>();
    private EnvelopeWebSocket webSocket = null;
    private final Map<String,AsynchronousEnvelopeHandler> handlers = new HashMap<>();
    private int nextHandlerId = 1;

    private Properties properties;

    public ClearnetSensor() {
        super(new NetworkPeer(Network.CLEAR));
    }

    public ClearnetSensor(SensorManager sensorManager) {
        super(sensorManager, new NetworkPeer(Network.CLEAR));
    }

    public String registerHandler(AsynchronousEnvelopeHandler handler) {
        String nextHandlerIdStr = String.valueOf(nextHandlerId++);
        handlers.put(nextHandlerIdStr, handler);
        return nextHandlerIdStr;
    }

    @Override
    public SensorSession establishSession(NetworkPeer peer, Boolean autoConnect) {
        return null;
    }

    @Override
    public boolean sendOut(Packet packet) {
        Envelope e = packet.getEnvelope();
        URL url = e.getURL();
        if(url != null) {
            LOG.info("URL="+url.toString());
        } else {
            LOG.info("URL must not be null.");
            return false;
        }
        Map<String,Object> h = e.getHeaders();
        Map<String,String> hStr = new HashMap<>();
        if(h.containsKey(Envelope.HEADER_CONTENT_DISPOSITION) && h.get(Envelope.HEADER_CONTENT_DISPOSITION)!=null) {
            hStr.put(Envelope.HEADER_CONTENT_DISPOSITION,(String)h.get(Envelope.HEADER_CONTENT_DISPOSITION));
        }
        if(h.containsKey(Envelope.HEADER_CONTENT_TYPE) && h.get(Envelope.HEADER_CONTENT_TYPE)!=null) {
            hStr.put(Envelope.HEADER_CONTENT_TYPE, (String)h.get(Envelope.HEADER_CONTENT_TYPE));
        }
        if(h.containsKey(Envelope.HEADER_CONTENT_TRANSFER_ENCODING) && h.get(Envelope.HEADER_CONTENT_TRANSFER_ENCODING)!=null) {
            hStr.put(Envelope.HEADER_CONTENT_TRANSFER_ENCODING, (String)h.get(Envelope.HEADER_CONTENT_TRANSFER_ENCODING));
        }
        if(h.containsKey(Envelope.HEADER_USER_AGENT) && h.get(Envelope.HEADER_USER_AGENT)!=null) {
            hStr.put(Envelope.HEADER_USER_AGENT, (String)h.get(Envelope.HEADER_USER_AGENT));
        }

        ByteBuffer bodyBytes = null;
        CacheControl cacheControl = null;
        if(e.getMultipart() != null) {
            // handle file upload
            Multipart m = e.getMultipart();
            hStr.put(Envelope.HEADER_CONTENT_TYPE, "multipart/form-data; boundary=" + m.getBoundary());
            try {
                bodyBytes = ByteBuffer.wrap(m.finish().getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
                // TODO: Provide error message
                LOG.warning("IOException caught while building HTTP body with multipart: "+e1.getLocalizedMessage());
                return false;
            }
            cacheControl = new CacheControl.Builder().noCache().build();
        }

        Headers headers = Headers.of(hStr);

        Message m = e.getMessage();
        if(m instanceof DocumentMessage) {
            Object contentObj = DLC.getContent(e);
            if(contentObj instanceof String) {
                if(bodyBytes == null) {
                    bodyBytes = ByteBuffer.wrap(((String)contentObj).getBytes());
                } else {
                    bodyBytes.put(((String)contentObj).getBytes());
                }
            } else if(contentObj instanceof byte[]) {
                if(bodyBytes == null) {
                    bodyBytes = ByteBuffer.wrap((byte[])contentObj);
                } else {
                    bodyBytes.put((byte[])contentObj);
                }
            }
        } else {
            LOG.warning("Only DocumentMessages supported at this time.");
            DLC.addErrorMessage("Only DocumentMessages supported at this time.",e);
            return false;
        }

        RequestBody requestBody = null;
        if(bodyBytes != null) {
            requestBody = RequestBody.create(MediaType.parse((String) h.get(Envelope.HEADER_CONTENT_TYPE)), bodyBytes.array());
        }

        okhttp3.Request.Builder b = new okhttp3.Request.Builder().url(url);
        if(cacheControl != null)
            b = b.cacheControl(cacheControl);
        b = b.headers(headers);
        switch(e.getAction()) {
            case POST: {b = b.post(requestBody);break;}
            case PUT: {b = b.put(requestBody);break;}
            case DELETE: {b = (requestBody == null ? b.delete() : b.delete(requestBody));break;}
            case GET: {b = b.get();break;}
            default: {
                LOG.warning("Envelope.action must be set to ADD, UPDATE, REMOVE, or VIEW");
                return false;
            }
        }
        Request req = b.build();
        if(req == null) {
            LOG.warning("okhttp3 builder didn't build request.");
            return false;
        }
        Response response = null;
        if(url.toString().startsWith("https:")) {
            LOG.info("Sending https request, host="+url.getHost());
//            if(trustedHosts.contains(url.getHost())) {
            try {
//                    LOG.info("Trusted host, using compatible connection...");
                response = httpsStrongClient.newCall(req).execute();
                if(!response.isSuccessful()) {
                    LOG.warning(response.toString()+" - code="+response.code());
                    m.addErrorMessage(response.code()+"");
                    handleFailure(m);
                    return false;
                }
            } catch (IOException e1) {
                LOG.warning(e1.getLocalizedMessage());
                m.addErrorMessage(e1.getLocalizedMessage());
                return false;
            }
//            } else {
//                try {
//                    System.out.println(ClearnetClientSensor.class.getSimpleName() + ": using strong connection...");
//                    response = httpsStrongClient.newCall(req).execute();
//                    if (!response.isSuccessful()) {
//                        m.addErrorMessage(response.code()+"");
//                        return false;
//                    }
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                    m.addErrorMessage(ex.getLocalizedMessage());
//                    return false;
//                }
//            }
        } else {
            LOG.info("Sending http request, host="+url.getHost());
            if(httpClient == null) {
                LOG.severe("httpClient was not set up.");
                return false;
            }
            try {
                response = httpClient.newCall(req).execute();
                if(!response.isSuccessful()) {
                    LOG.warning("HTTP request not successful: "+response.code());
                    m.addErrorMessage(response.code()+"");
                    handleFailure(m);
                    return false;
                }
            } catch (IOException e2) {
                LOG.warning(e2.getLocalizedMessage());
                m.addErrorMessage(e2.getLocalizedMessage());
                return false;
            }
        }

        LOG.info("Received http response.");
        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            LOG.info(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }
        ResponseBody responseBody = response.body();
        if(responseBody != null) {
            try {
                DLC.addContent(responseBody.bytes(),e);
            } catch (IOException e1) {
                LOG.warning(e1.getLocalizedMessage());
            } finally {
                responseBody.close();
            }
            LOG.info(new String((byte[])DLC.getContent(e)));
        } else {
            LOG.info("Body was null.");
            DLC.addContent(null,e);
        }

        return true;
    }

    protected void handleFailure(Message m) {
        if(m!=null && m.getErrorMessages()!=null && m.getErrorMessages().size()>0) {
            boolean blocked = false;
            for (String err : m.getErrorMessages()) {
                LOG.warning("HTTP Error Message: " + err);
                if(!blocked) {
                    switch (err) {
                        case "403": {
                            // Forbidden
                            LOG.info("Received HTTP 403 response: Forbidden. HTTP Request considered blocked.");
                            m.addErrorMessage("BLOCKED");
                            blocked = true;
                            break;
                        }
                        case "408": {
                            // Request Timeout
                            LOG.info("Received HTTP 408 response: Request Timeout. HTTP Request considered blocked.");
                            m.addErrorMessage("BLOCKED");
                            blocked = true;
                            break;
                        }
                        case "410": {
                            // Gone
                            LOG.info("Received HTTP 410 response: Gone. HTTP Request considered blocked.");
                            m.addErrorMessage("BLOCKED");
                            blocked = true;
                            break;
                        }
                        case "418": {
                            // I'm a teapot
                            LOG.warning("Received HTTP 418 response: I'm a teapot. HTTP Sensor ignoring.");
                            break;
                        }
                        case "451": {
                            // Unavailable for legal reasons; your IP address might be denied access to the resource
                            LOG.info("Received HTTP 451 response: unavailable for legal reasons. HTTP Request considered blocked.");
                            m.addErrorMessage("BLOCKED");
                            blocked = true;
                            break;
                        }
                        case "511": {
                            // Network Authentication Required
                            LOG.info("Received HTTP 511 response: network authentication required. HTTP Request considered blocked.");
                            m.addErrorMessage("BLOCKED");
                            blocked = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String[] getOperationEndsWith() {
        return new String[]{".html",".htm",".do",".json"};
    }

    @Override
    public String[] getURLBeginsWith() {
        return new String[]{"http","https"};
    }

    @Override
    public String[] getURLEndsWith() {
        return new String[]{".html",".htm",".do",".json"};
    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Starting...");
        updateStatus(SensorStatus.INITIALIZING);
        Config.logProperties(p);
        try {
            properties = Config.loadFromClasspath("1m5-sensors-clearnet.config", p, false);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        String sensorsDirStr = properties.getProperty("1m5.dir.sensors");
        if (sensorsDirStr == null) {
            LOG.warning("1m5.dir.sensors property is null. Please set prior to instantiating Clearnet Client Sensor.");
            return false;
        }
        try {
            File sensorDir = new File(new File(sensorsDirStr), "clearnet");
            if (!sensorDir.exists() && !sensorDir.mkdir()) {
                LOG.warning("Unable to create Clearnet Sensor directory.");
                return false;
            } else {
                properties.put("1m5.dir.sensors.clearnet", sensorDir.getCanonicalPath());
            }
        } catch (IOException e) {
            LOG.warning("IOException caught while building Clearnet sensor directory: \n" + e.getLocalizedMessage());
            return false;
        }

        updateStatus(SensorStatus.STARTING);

        // Clients setup
        if ("true".equals(properties.getProperty("1m5.sensors.clearnet.client.enable"))) {
            clientsEnabled = true;
            boolean trustAllCerts = "true".equals(properties.get("1m5.network.sensors.clearnet.client.trustallcerts"));
            SSLContext trustAllSSLContext = null;
            try {
                if (trustAllCerts) {
                    LOG.info("Initialize SSLContext with trustallcerts...");
                    trustAllSSLContext = SSLContext.getInstance("TLS");
                    trustAllSSLContext.init(null, trustAllTrustManager, new java.security.SecureRandom());
                }
            } catch (NoSuchAlgorithmException e) {
                LOG.warning(e.getLocalizedMessage());
                return false;
            } catch (KeyManagementException e) {
                LOG.warning(e.getLocalizedMessage());
                return false;
            }

            try {
                LOG.info("Setting up HTTP spec clients for http, https, and strong https....");
                httpSpec = new ConnectionSpec
                        .Builder(ConnectionSpec.CLEARTEXT)
                        .build();
                if (proxy == null) {
                    LOG.info("Setting up http client...");
                    httpClient = new OkHttpClient.Builder()
                            .connectionSpecs(Collections.singletonList(httpSpec))
                            .retryOnConnectionFailure(true)
                            .followRedirects(true)
                            .build();
                } else {
                    LOG.info("Setting up http client with proxy...");
                    httpClient = new OkHttpClient.Builder()
                            .connectionSpecs(Arrays.asList(httpSpec))
                            .retryOnConnectionFailure(true)
                            .followRedirects(true)
                            .proxy(proxy)
                            .build();
                }

                LOG.info("Setting https.protocols to system property...");
                System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");

                httpsCompatibleSpec = new ConnectionSpec
                        .Builder(ConnectionSpec.COMPATIBLE_TLS)
//                    .supportsTlsExtensions(true)
//                    .allEnabledTlsVersions()
//                    .allEnabledCipherSuites()
                        .build();

                if (proxy == null) {
                    LOG.info("Setting up https client...");
                    if (trustAllCerts) {
                        LOG.info("Trust All Certs HTTPS Compatible Client building...");
                        httpsCompatibleClient = new OkHttpClient.Builder()
                                .sslSocketFactory(trustAllSSLContext.getSocketFactory(), trustAllX509TrustManager)
                                .hostnameVerifier(trustAllHostnameVerifier)
                                .build();
                    } else {
                        LOG.info("Standard HTTPS Compatible Client building...");
                        httpsCompatibleClient = new OkHttpClient.Builder()
                                .connectionSpecs(Arrays.asList(httpsCompatibleSpec))
                                .build();
                    }
                } else {
                    LOG.info("Setting up https client with proxy...");
                    if (trustAllCerts) {
                        LOG.info("Trust All Certs HTTPS Compatible Client with Proxy building...");
                        httpsCompatibleClient = new OkHttpClient.Builder()
                                .sslSocketFactory(trustAllSSLContext.getSocketFactory(), trustAllX509TrustManager)
                                .hostnameVerifier(trustAllHostnameVerifier)
                                .proxy(proxy)
                                .build();
                    } else {
                        LOG.info("Standard HTTPS Compatible Client with Proxy building...");
                        httpsCompatibleClient = new OkHttpClient.Builder()
                                .connectionSpecs(Arrays.asList(httpsCompatibleSpec))
                                .proxy(proxy)
                                .build();
                    }
                }

                httpsStrongSpec = new ConnectionSpec
                        .Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                        .cipherSuites(
                                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                        .build();

                if (proxy == null) {
                    LOG.info("Setting up strong https client...");
                    if (trustAllCerts) {
                        LOG.info("Trust All Certs Strong HTTPS Compatible Client building...");
                        httpsStrongClient = new OkHttpClient.Builder()
                                .connectionSpecs(Collections.singletonList(httpsStrongSpec))
                                .retryOnConnectionFailure(true)
                                .followSslRedirects(true)
                                .sslSocketFactory(trustAllSSLContext.getSocketFactory(), trustAllX509TrustManager)
                                .hostnameVerifier(trustAllHostnameVerifier)
                                .build();
                    } else {
                        LOG.info("Standard Strong HTTPS Compatible Client building...");
                        httpsStrongClient = new OkHttpClient.Builder()
                                .connectionSpecs(Collections.singletonList(httpsStrongSpec))
                                .retryOnConnectionFailure(true)
                                .followSslRedirects(true)
                                .build();
                    }
                } else {
                    LOG.info("Setting up strong https client with proxy...");
                    if (trustAllCerts) {
                        LOG.info("Trust All Certs Strong HTTPS Compatible Client with Proxy building...");
                        httpsStrongClient = new OkHttpClient.Builder()
                                .connectionSpecs(Collections.singletonList(httpsStrongSpec))
                                .retryOnConnectionFailure(true)
                                .followSslRedirects(true)
                                .sslSocketFactory(trustAllSSLContext.getSocketFactory(), trustAllX509TrustManager)
                                .hostnameVerifier(trustAllHostnameVerifier)
                                .proxy(proxy)
                                .build();
                    } else {
                        LOG.info("Standard Strong HTTPS Compatible Client with Proxy building...");
                        httpsStrongClient = new OkHttpClient.Builder()
                                .connectionSpecs(Collections.singletonList(httpsStrongSpec))
                                .retryOnConnectionFailure(true)
                                .followSslRedirects(true)
                                .proxy(proxy)
                                .build();
                    }
                }

            } catch (Exception e) {
                LOG.warning("Exception caught launching Clearnet Sensor clients: " + e.getLocalizedMessage());
                return false;
            }
        }

        // Servers setup
        if (properties.getProperty(SERVERS_CONFIG) != null) {
            serversEnabled = true;
            String serversConfig = properties.getProperty(SERVERS_CONFIG);
            LOG.info("Building servers configuration: " + serversConfig);
            String[] serv = serversConfig.split(":");
            LOG.info("Number of servers to start: " + serv.length);
            boolean launchOnStart = false;
            for (String s : serv) {
                HandlerCollection handlers = new HandlerCollection();

                String[] m = s.split(",");
                String name = m[0];
                if (name == null) {
                    LOG.warning("Name must be provided for HTTP server.");
                    return false;
                }

                String type = m[1];
                if (type == null) {
                    LOG.warning("Type must be provided for HTTP Proxy with name=" + name);
                    return false;
                }

                String portStr = m[2];
                if (portStr == null) {
                    LOG.warning("Port must be provided for HTTP server with name=" + name);
                    return false;
                }
                int port = Integer.parseInt(portStr);

                if ("proxy".equals(type)) {
                    String kandlerStr = m[3];
                    AsynchronousEnvelopeHandler handler = null;
//                    handlers.addHandler(new DefaultHandler());
                    try {
                        handler = (AsynchronousEnvelopeHandler) Class.forName(kandlerStr).newInstance();
                        handler.setSensor(this);
                        handler.setServiceName(name);
                        handler.setParameters(m);
                        handlers.addHandler(handler);
                    } catch (InstantiationException e) {
                        LOG.warning("Handler must be implementation of " + AsynchronousEnvelopeHandler.class.getName() + " to ensure asynchronous replies with Envelopes gets returned to calling thread.");
                        return false;
                    } catch (IllegalAccessException e) {
                        LOG.warning("Getting an IllegalAccessException while attempting to instantiate Handler implementation class " + kandlerStr + ". Launch application with appropriate read access.");
                        return false;
                    } catch (ClassNotFoundException e) {
                        LOG.warning("Handler implementation " + kandlerStr + " not found. Ensure library included.");
                        return false;
                    }
                } else if ("local".equals(type)) {

                    String launchOnStartStr = m[3];
                    launchOnStart = "true".equals(launchOnStartStr);

                    String spaStr = m[4];
                    boolean spa = "true".equals(spaStr);

                    String dataHandlerStr = m[5];
                    AsynchronousEnvelopeHandler dataHandler = null;

                    String resourceDirectory = m[6];
                    URL webDirURL = this.getClass().getClassLoader().getResource(resourceDirectory);

                    String useSocketStr = m[7];

                    String webSocketAdapter = null;
                    if ("true".equals(useSocketStr) && m.length > 8) {
                        webSocketAdapter = m[8];
                    }
                    // TODO: Make Web Socket context path configurable

                    SessionHandler sessionHandler = new SessionHandler();

                    // TODO: Make data context path configurable
                    ContextHandler dataContext = new ContextHandler();
                    dataContext.setContextPath("/data/*");

                    ResourceHandler resourceHandler = new ResourceHandler();
                    resourceHandler.setDirectoriesListed(false);
                    resourceHandler.setWelcomeFiles(new String[]{"index.html"});
                    if (webDirURL != null) {
                        resourceHandler.setResourceBase(webDirURL.toExternalForm());
                    }

                    ContextHandler wsContext = null;
                    if ("true".equals(useSocketStr)) {
                        if (webSocketAdapter == null) {
                            webSocket = new EnvelopeWebSocket(this);
                            LOG.info("No custom EnvelopWebSocket class provided; using generic one.");
                        } else {
                            try {
                                webSocket = (EnvelopeWebSocket) Class.forName(webSocketAdapter).newInstance();
                                webSocket.setClearnetServerSensor(this);
                            } catch (InstantiationException e) {
                                LOG.warning("Unable to instantiate WebSocket of type: " + webSocketAdapter);
                            } catch (IllegalAccessException e) {
                                LOG.warning("Illegal Access caught when attempting to instantiate WebSocket of type: " + webSocketAdapter);
                            } catch (ClassNotFoundException e) {
                                LOG.warning("WebSocket class " + webSocketAdapter + " not found. Unable to instantiate.");
                            }
                        }
                        if (webSocket == null) {
                            LOG.warning("WebSocket configured to be launched yet unable to instantiate.");
                        } else {
                            WebSocketHandler wsHandler = new WebSocketHandler() {
                                @Override
                                public void configure(WebSocketServletFactory factory) {
                                    WebSocketPolicy policy = factory.getPolicy();
                                    // set a one hour timeout
                                    policy.setIdleTimeout(60 * 60 * 1000);
//                            policy.setAsyncWriteTimeout(60 * 1000);
//                            int maxSize = 100 * 1000000;
//                            policy.setMaxBinaryMessageSize(maxSize);
//                            policy.setMaxBinaryMessageBufferSize(maxSize);
//                            policy.setMaxTextMessageSize(maxSize);
//                            policy.setMaxTextMessageBufferSize(maxSize);

                                    factory.setCreator(new WebSocketCreator() {
                                        @Override
                                        public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
                                            String query = req.getRequestURI().toString();
                                            if ((query == null) || (query.length() <= 0)) {
                                                try {
                                                    resp.sendForbidden("Unspecified query");
                                                } catch (IOException e) {

                                                }
                                                return null;
                                            }
                                            return webSocket;
                                        }
                                    });
                                }

                            };
                            wsContext = new ContextHandler();
                            wsContext.setContextPath("/events/*");
                            wsContext.setHandler(wsHandler);
                        }
                    }

                    handlers.addHandler(sessionHandler);
                    if (spa) {
                        handlers.addHandler(new SPAHandler());
                    }
                    handlers.addHandler(dataContext);
                    handlers.addHandler(resourceHandler);
                    if (wsContext != null) {
                        handlers.addHandler(wsContext);
                    }
                    handlers.addHandler(new DefaultHandler());

                    if (dataHandlerStr != null) { // optional
                        try {
                            dataHandler = (AsynchronousEnvelopeHandler) Class.forName(dataHandlerStr).newInstance();
                            dataHandler.setSensor(this);
                            dataHandler.setServiceName(name);
                            dataHandler.setParameters(m);
                            dataContext.setHandler(dataHandler);
                        } catch (InstantiationException e) {
                            LOG.warning("Data Handler must be implementation of " + AsynchronousEnvelopeHandler.class.getName() + " to ensure asynchronous replies with Envelopes gets returned to calling thread.");
                            return false;
                        } catch (IllegalAccessException e) {
                            LOG.warning("Getting an IllegalAccessException while attempting to instantiate data Handler implementation class " + dataHandlerStr + ". Launch application with appropriate read access.");
                            return false;
                        } catch (ClassNotFoundException e) {
                            LOG.warning("Data Handler implementation " + dataHandlerStr + " not found. Ensure library included.");
                            return false;
                        }
                    }
                }

                if (!startServer(name, port, handlers, launchOnStart)) {
                    LOG.warning("Unable to start server " + name);
                    updateStatus(SensorStatus.ERROR);
                    return false;
                } else {
                    if (webSocket != null) {
                        LOG.info("Subscribing WebSocket (" + webSocket.getClass().getName() + ") to TEXT notifications...");
                        // Subscribe to Text notifications
                        Subscription subscription = new Subscription() {
                            @Override
                            public void notifyOfEvent(Envelope envelope) {
                                webSocket.pushEnvelope(envelope);
                            }
                        };
                        SubscriptionRequest r = new SubscriptionRequest(EventMessage.Type.TEXT, subscription);
                        Envelope e = Envelope.documentFactory();
                        DLC.addData(SubscriptionRequest.class, r, e);
                        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, e);
                        if (!sendIn(e)) {
                            updateStatus(SensorStatus.ERROR);
                            LOG.warning("Error sending subscription request to Notification Service for Web Socket.");
                            return false;
                        }
                    }
                }
            }
            updateStatus(SensorStatus.NETWORK_CONNECTED);
        }
        LOG.info("Started.");
        return true;
    }

    private boolean startServer(String name, int port, Handler dataHandler, boolean launch) {
        Server server = new Server(new InetSocketAddress("127.0.0.1", port));
        server.setHandler(dataHandler);
        LOG.info("Starting HTTP Server for "+name+" on 127.0.0.1:"+port);
        try {
            server.start();
            LOG.finest(server.dump());
            servers.put(name, server);
            LOG.info("HTTP Server for "+name+" started on 127.0.0.1:"+port);
        } catch (Exception e) {
            LOG.severe("Exception caught while starting HTTP Server for "+name+" with port "+port+": "+e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        if(launch)
            BrowserUtil.launch("http://127.0.0.1:"+port+"/");
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
        LOG.info("Restarting...");
        for(Server server : servers.values()) {
            try {
                server.stop();
                server.start();
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        LOG.info("Restarted.");
        return true;
    }

    @Override
    public boolean shutdown() {
        LOG.info("Shutting down...");
        for(Server server : servers.values()) {
            try {
                server.stop();
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }

    public static void main(String[] args) {
        Properties p = new Properties();
        p.setProperty("1m5.ui","true");
        p.setProperty("1m5.ui.launchOnStart","true");
        ClearnetSensor sensor = new ClearnetSensor(null);
        sensor.start(p);
    }
}
