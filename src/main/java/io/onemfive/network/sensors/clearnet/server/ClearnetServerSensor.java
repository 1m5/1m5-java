package io.onemfive.network.sensors.clearnet.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.notification.SubscriptionRequest;
import io.onemfive.data.*;
import io.onemfive.data.util.DLC;
import io.onemfive.network.sensors.SensorStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;

import io.onemfive.core.Config;
import io.onemfive.network.sensors.BaseSensor;
import io.onemfive.network.sensors.SensorManager;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Sets up HTTP server listeners.
 * Only localhost (127.0.0.1) is supported.
 *
 * @author objectorange
 */
public final class ClearnetServerSensor extends BaseSensor {

    public static final String HANDLER_ID = "1m5.sensors.clearnet.server.handler.id";

    /**
     * Configuration of Servers in the form:
     *      name, port, launch on start, concrete implementation of io.onemfive.network.sensors.clearnet.server.AsynchronousEnvelopeHandler, run websocket, relative resource directory|n,...}
     */
    public static final String SERVERS_CONFIG = "1m5.sensors.clearnet.server.config";

    private static final Logger LOG = Logger.getLogger(ClearnetServerSensor.class.getName());

    private boolean isTest = false;

    private final List<Server> servers = new ArrayList<>();
    private EnvelopeWebSocket webSocket = null;
    private final Map<String,AsynchronousEnvelopeHandler> handlers = new HashMap<>();
    private int nextHandlerId = 1;

    private Properties properties;

    public ClearnetServerSensor() {}

    public ClearnetServerSensor(SensorManager sensorManager, Sensitivity sensitivity, Integer priority) {
        super(sensorManager, sensitivity, priority);
    }

    String registerHandler(AsynchronousEnvelopeHandler handler) {
        String nextHandlerIdStr = String.valueOf(nextHandlerId++);
        handlers.put(nextHandlerIdStr, handler);
        return nextHandlerIdStr;
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
    public boolean sendOut(Packet packet) {

        return true;
    }

    @Override
    public boolean replyOut(Packet packet) {
        LOG.info("Reply to ClearnetServerSensor; forwarding to registered handler...");
        Envelope e = packet.getEnvelope();
        String handlerId = (String)e.getHeader(HANDLER_ID);
        if(handlerId == null) {
            LOG.warning("Handler id not found in Envelope header. Ensure this is placed in the Envelope header="+HANDLER_ID);
            sensorManager.suspend(e);
            return false;
        }
        AsynchronousEnvelopeHandler handler = handlers.get(handlerId);
        if(handler == null) {
            LOG.warning("Handler with id="+handlerId+" not registered. Please ensure it's registered prior to calling send().");
            sensorManager.suspend(e);
            return false;
        }
        handler.reply(e);
        return true;
    }

    @Override
    public boolean start(Properties p) {
        LOG.info("Starting...");
        updateStatus(SensorStatus.INITIALIZING);
        Config.logProperties(p);
        try {
            properties = Config.loadFromClasspath("clearnet-server.config", p, false);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        updateStatus(SensorStatus.STARTING);
        if("true".equals(properties.getProperty(Config.PROP_UI))) {
            String webDir = this.getClass().getClassLoader().getResource("io/onemfive/network/sensors/clearnet/server/ui").toExternalForm();
            // Start HTTP Server for 1M5 UI
            AsynchronousEnvelopeHandler dataHandler = new EnvelopeJSONDataHandler();
            dataHandler.setSensor(this);
            dataHandler.setServiceName("1M5-Data-Service");

            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(false);
            resourceHandler.setResourceBase(webDir);

            ContextHandler dataContext = new ContextHandler();
            dataContext.setContextPath("/data/*");
            dataContext.setHandler(dataHandler);

            HandlerCollection handlers = new HandlerCollection();
            handlers.addHandler(new SessionHandler());
            handlers.addHandler(dataContext);
            handlers.addHandler(resourceHandler);
            handlers.addHandler(new DefaultHandler());

            boolean launchOnStart = "true".equals(properties.getProperty(Config.PROP_UI_LAUNCH_ON_START));
            // 571 BC - Birth of Laozi, Chinese Philosopher and Writer, author of Tao Te Ching
            if(!startServer("1M5", 5710, handlers, launchOnStart))
                return false;
        }

        if(properties.getProperty(SERVERS_CONFIG)!=null) {
            String serversConfig = properties.getProperty(SERVERS_CONFIG);
            LOG.info("Building servers configuration: "+serversConfig);
            String[] servers = serversConfig.split(":");
            LOG.info("Number of servers to start: "+servers.length);
            boolean launchOnStart = false;
            if(servers.length > 0) {
                // TODO: Support multiple servers?
//            for(String s : servers) {
                String s = servers[0];
                HandlerCollection handlers = new HandlerCollection();

                String[] m = s.split(",");
                String name = m[0];
                if(name==null){
                    LOG.warning("Name must be provided for HTTP server.");
                    return false;
                }

                String type = m[1];
                if(type==null) {
                    LOG.warning("Type must be provided for HTTP Proxy with name="+name);
                    return false;
                }

                String portStr = m[2];
                if (portStr == null) {
                    LOG.warning("Port must be provided for HTTP server with name=" + name);
                    return false;
                }
                int port = Integer.parseInt(portStr);

                if("proxy".equals(type)) {
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
                } else if("local".equals(type)) {

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

                if(!startServer(name, port, handlers, launchOnStart)) {
                    LOG.warning("Unable to start server "+name);
                    updateStatus(SensorStatus.ERROR);
                } else {
                    if(webSocket != null) {
                        LOG.info("Subscribing WebSocket ("+webSocket.getClass().getName()+") to TEXT notifications...");
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
                        if(sendIn(e)) {
                            updateStatus(SensorStatus.NETWORK_CONNECTED);
                        } else {
                            updateStatus(SensorStatus.ERROR);
                            LOG.warning("Error sending subscription request to Notification Service for Web Socket.");
                        }
                    } else {
                        updateStatus(SensorStatus.NETWORK_CONNECTED);
                    }
                }
            }
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
//            LOG.finest(server.dump());
            servers.add(server);
            LOG.info("HTTP Server for "+name+" started on 127.0.0.1:"+port);
        } catch (Exception e) {
            LOG.severe("Exception caught while starting HTTP Server for "+name+" with port "+port+": "+e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        if(launch)
            ClearnetServerUtil.launchBrowser("http://127.0.0.1:"+port+"/");
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
        for(Server server : servers) {
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
        for(Server server : servers) {
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
        ClearnetServerSensor sensor = new ClearnetServerSensor(null, null, null);
        sensor.start(p);
    }

}
