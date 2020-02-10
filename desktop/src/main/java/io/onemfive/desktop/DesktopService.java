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
package io.onemfive.desktop;

import io.onemfive.OneMFivePlatform;
import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.notification.SubscriptionRequest;
import io.onemfive.data.*;
import io.onemfive.data.route.Route;
import io.onemfive.desktop.views.TopicListener;
import io.onemfive.desktop.views.home.HomeView;
import io.onemfive.desktop.views.ops.network.bluetooth.BluetoothSensorOpsView;
import io.onemfive.desktop.views.ops.network.fullspectrum.FullSpectrumRadioSensorOpsView;
import io.onemfive.desktop.views.ops.network.i2p.I2PSensorOpsView;
import io.onemfive.desktop.views.ops.network.ims.IMSOpsView;
import io.onemfive.desktop.views.ops.network.lifi.LiFiSensorOpsView;
import io.onemfive.desktop.views.ops.network.satellite.SatelliteSensorOpsView;
import io.onemfive.desktop.views.ops.network.tor.TORSensorOpsView;
import io.onemfive.desktop.views.ops.network.wifidirect.WifiDirectSensorOpsView;
import io.onemfive.desktop.views.personal.identities.IdentitiesView;
import io.onemfive.desktop.views.settings.network.bluetooth.BluetoothSensorSettingsView;
import io.onemfive.desktop.views.settings.network.i2p.I2PSensorSettingsView;
import io.onemfive.desktop.views.settings.network.ims.IMSSettingsView;
import io.onemfive.desktop.views.settings.network.tor.TORSensorSettingsView;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.network.sensors.SensorStatusListener;
import io.onemfive.network.sensors.bluetooth.BluetoothSensor;
import io.onemfive.network.sensors.fullspectrum.FullSpectrumRadioSensor;
import io.onemfive.network.sensors.i2p.I2PSensor;
import io.onemfive.network.sensors.lifi.LiFiSensor;
import io.onemfive.network.sensors.satellite.SatelliteSensor;
import io.onemfive.network.sensors.tor.SimpleTorSensor;
import io.onemfive.network.sensors.wifidirect.WiFiDirectSensor;
import io.onemfive.util.DLC;
import javafx.application.Platform;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class DesktopService extends BaseService {

    private static final Logger LOG = Logger.getLogger(DesktopService.class.getName());

    public static final String OPERATION_NOTIFY_UI = "NOTIFY_UI";

    public static final String OPERATION_UPDATE_ACTIVE_IDENTITY = "UPDATE_ACTIVE_IDENTITY";
    public static final String OPERATION_UPDATE_IDENTITIES = "UPDATE_IDENTITIES";

    public DesktopService() {
    }

    public DesktopService(MessageProducer producer, ServiceStatusListener listener) {
        super(producer, listener);
    }

    @Override
    public void handleDocument(Envelope e) {
        handleAll(e);
    }

    @Override
    public void handleEvent(Envelope e) {
        handleAll(e);
    }

    @Override
    public void handleHeaders(Envelope e) {
        handleAll(e);
    }

    private void handleAll(Envelope e) {
        LOG.info("Received UI Service request...");
        Route route = e.getRoute();
        String operation = route.getOperation();
        switch (operation) {
            case OPERATION_UPDATE_ACTIVE_IDENTITY: {
                LOG.info("Update active identity request...");
                final DID activeIdentity = (DID)DLC.getEntity(e);
                if(activeIdentity!=null) {
                    Platform.runLater(() -> {
                        LOG.info("Updating IdentitiesView active DID...");
                        IdentitiesView v = (IdentitiesView)MVC.loadView(IdentitiesView.class, true);
                        v.updateActiveDID(activeIdentity);
                    });
                }
                break;
            }
            case OPERATION_UPDATE_IDENTITIES: {
                LOG.info("Update identities request...");
                final List<DID> identities = (List<DID>)DLC.getValue("identities", e);
                if(identities!=null) {
                    Platform.runLater(() -> {
                        LOG.info("Updating IdentitiesView identities...");
                        IdentitiesView v = (IdentitiesView)MVC.loadView(IdentitiesView.class, true);
                        v.updateIdentities(identities);
                    });
                }
                break;
            }
            case OPERATION_NOTIFY_UI: {
                LOG.warning("UI Notifications not yet implemented.");
                break;
            }
            default: {
                LOG.warning("Operation unsupported: " + operation);
            }
        }
    }

    @Override
    public boolean start(Properties p) {
        if(!super.start(p)) {
            LOG.warning("DesktopService's parent failed to start.");
            return false;
        }
        SensorManager.registerManConStatusListener(() -> Platform.runLater(() -> {
            LOG.info("Updating ManCon status...");
            HomeView v = (HomeView)MVC.loadView(HomeView.class, true);
            v.updateManConBox();
        }));

        // Tor Sensor Status
        Envelope eTorStatus = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestTorStatus = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, SimpleTorSensor.class.getName(),
        new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with TOR Sensor Status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    SensorStatus status = (SensorStatus)em.getMessage();
                    SensorStatusListener listener = (SensorStatusListener)MVC.loadView(TORSensorOpsView.class, true);
                    listener.statusUpdated(status);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, subscriptionRequestTorStatus, eTorStatus);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eTorStatus);
        OneMFivePlatform.sendRequest(eTorStatus);

        // I2P Sensor Status
        Envelope eI2PStatus = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestI2PStatus = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, I2PSensor.class.getName(),
                new Subscription() {
                    @Override
                    public void notifyOfEvent(Envelope e) {
                        Platform.runLater(() -> {
                            LOG.info("Updating UI with I2P Sensor Status...");
                            EventMessage em = (EventMessage)e.getMessage();
                            SensorStatus status = (SensorStatus)em.getMessage();
                            SensorStatusListener listener = (SensorStatusListener)MVC.loadView(I2PSensorOpsView.class, true);
                            listener.statusUpdated(status);
                        });
                    }
                });
        DLC.addData(SubscriptionRequest.class, subscriptionRequestI2PStatus, eI2PStatus);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eI2PStatus);
        OneMFivePlatform.sendRequest(eI2PStatus);

        // WiFi Direct Sensor Status
        Envelope eWFDStatus = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestWFDStatus = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, WiFiDirectSensor.class.getName(),
                new Subscription() {
                    @Override
                    public void notifyOfEvent(Envelope e) {
                        Platform.runLater(() -> {
                            LOG.info("Updating UI with WiFi-Direct Sensor Status...");
                            EventMessage em = (EventMessage)e.getMessage();
                            SensorStatus status = (SensorStatus)em.getMessage();
                            SensorStatusListener listener = (SensorStatusListener)MVC.loadView(WifiDirectSensorOpsView.class, true);
                            listener.statusUpdated(status);
                        });
                    }
                });
        DLC.addData(SubscriptionRequest.class, subscriptionRequestWFDStatus, eWFDStatus);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eWFDStatus);
        OneMFivePlatform.sendRequest(eWFDStatus);

        // Bluetooth Sensor Status
        Envelope eBTStatus = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestBTStatus = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, BluetoothSensor.class.getName(),
                new Subscription() {
                    @Override
                    public void notifyOfEvent(Envelope e) {
                        Platform.runLater(() -> {
                            LOG.info("Updating UI with Bluetooth Sensor Status...");
                            EventMessage em = (EventMessage)e.getMessage();
                            SensorStatus status = (SensorStatus)em.getMessage();
                            SensorStatusListener listener = (SensorStatusListener)MVC.loadView(BluetoothSensorOpsView.class, true);
                            listener.statusUpdated(status);
                        });
                    }
                });
        DLC.addData(SubscriptionRequest.class, subscriptionRequestBTStatus, eBTStatus);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eBTStatus);
        OneMFivePlatform.sendRequest(eBTStatus);

        // Satellite Sensor Status
        Envelope eSatStatus = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestSatStatus = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, SatelliteSensor.class.getName(),
                new Subscription() {
                    @Override
                    public void notifyOfEvent(Envelope e) {
                        Platform.runLater(() -> {
                            LOG.info("Updating UI with Satellite Sensor Status...");
                            EventMessage em = (EventMessage)e.getMessage();
                            SensorStatus status = (SensorStatus)em.getMessage();
                            SensorStatusListener listener = (SensorStatusListener)MVC.loadView(SatelliteSensorOpsView.class, true);
                            listener.statusUpdated(status);
                        });
                    }
                });
        DLC.addData(SubscriptionRequest.class, subscriptionRequestSatStatus, eSatStatus);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eSatStatus);
        OneMFivePlatform.sendRequest(eSatStatus);

        // Full Spectrum Radio Sensor Status
        Envelope eFSRStatus = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestFSRStatus = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, FullSpectrumRadioSensor.class.getName(),
                new Subscription() {
                    @Override
                    public void notifyOfEvent(Envelope e) {
                        Platform.runLater(() -> {
                            LOG.info("Updating UI with Full Spectrum Radio Sensor Status...");
                            EventMessage em = (EventMessage)e.getMessage();
                            SensorStatus status = (SensorStatus)em.getMessage();
                            SensorStatusListener listener = (SensorStatusListener)MVC.loadView(FullSpectrumRadioSensorOpsView.class, true);
                            listener.statusUpdated(status);
                        });
                    }
                });
        DLC.addData(SubscriptionRequest.class, subscriptionRequestFSRStatus, eFSRStatus);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eFSRStatus);
        OneMFivePlatform.sendRequest(eFSRStatus);

        // LiFi Sensor Status
        Envelope eLFStatus = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestLFStatus = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, LiFiSensor.class.getName(),
                new Subscription() {
                    @Override
                    public void notifyOfEvent(Envelope e) {
                        Platform.runLater(() -> {
                            LOG.info("Updating UI with LiFi Sensor Status...");
                            EventMessage em = (EventMessage)e.getMessage();
                            SensorStatus status = (SensorStatus)em.getMessage();
                            SensorStatusListener listener = (SensorStatusListener)MVC.loadView(LiFiSensorOpsView.class, true);
                            listener.statusUpdated(status);
                        });
                    }
                });
        DLC.addData(SubscriptionRequest.class, subscriptionRequestLFStatus, eLFStatus);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eLFStatus);
        OneMFivePlatform.sendRequest(eLFStatus);

        // 1M5 Peer Status
        Envelope e1M5Peer = Envelope.documentFactory();
        SubscriptionRequest sensorStatusRequest1M5Peer = new SubscriptionRequest(EventMessage.Type.PEER_STATUS, Network.IMS.name(), new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with 1M5 Peer status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    NetworkPeer np = (NetworkPeer)em.getMessage();
                    TopicListener aware = (TopicListener)MVC.loadView(IMSOpsView.class, true);
                    aware.modelUpdated(NetworkPeer.class.getName(), np);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, sensorStatusRequest1M5Peer, e1M5Peer);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, e1M5Peer);
        OneMFivePlatform.sendRequest(e1M5Peer);

        // TOR Peer Status
        Envelope eTORPeer = Envelope.documentFactory();
        SubscriptionRequest sensorStatusRequestTORPeer = new SubscriptionRequest(EventMessage.Type.PEER_STATUS, Network.TOR.name(), new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with TOR Peer status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    NetworkPeer np = (NetworkPeer)em.getMessage();
                    TopicListener aware = (TopicListener)MVC.loadView(TORSensorOpsView.class, true);
                    aware.modelUpdated(NetworkPeer.class.getName(), np);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, sensorStatusRequestTORPeer, eTORPeer);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eTORPeer);
        OneMFivePlatform.sendRequest(eTORPeer);

        // I2P Peer Status
        Envelope eI2PPeer = Envelope.documentFactory();
        SubscriptionRequest sensorStatusRequestI2PPeer = new SubscriptionRequest(EventMessage.Type.PEER_STATUS, Network.I2P.name(), new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with I2P Peer status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    NetworkPeer np = (NetworkPeer)em.getMessage();
                    TopicListener aware = (TopicListener)MVC.loadView(I2PSensorOpsView.class, true);
                    aware.modelUpdated(NetworkPeer.class.getName(), np);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, sensorStatusRequestI2PPeer, eI2PPeer);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eI2PPeer);
        OneMFivePlatform.sendRequest(eI2PPeer);

        // Wifi-Direct Peer Status
        Envelope eWFDPeer = Envelope.documentFactory();
        SubscriptionRequest sensorStatusRequestWFDPeer = new SubscriptionRequest(EventMessage.Type.PEER_STATUS, Network.WiFiDirect.name(), new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with Wifi-Direct Peer status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    NetworkPeer np = (NetworkPeer)em.getMessage();
                    TopicListener aware = (TopicListener)MVC.loadView(WifiDirectSensorOpsView.class, true);
                    aware.modelUpdated(NetworkPeer.class.getName(), np);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, sensorStatusRequestWFDPeer, eWFDPeer);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eWFDPeer);
        OneMFivePlatform.sendRequest(eWFDPeer);

        // Bluetooth Peer Status
        Envelope eBTPeer = Envelope.documentFactory();
        SubscriptionRequest sensorStatusRequestBTPeer = new SubscriptionRequest(EventMessage.Type.PEER_STATUS, Network.Bluetooth.name(), new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with Bluetooth Peer status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    NetworkPeer np = (NetworkPeer)em.getMessage();
                    TopicListener aware = (TopicListener)MVC.loadView(BluetoothSensorOpsView.class, true);
                    aware.modelUpdated(NetworkPeer.class.getName(), np);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, sensorStatusRequestBTPeer, eBTPeer);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eBTPeer);
        OneMFivePlatform.sendRequest(eBTPeer);

        // Satellite Peer Status
        Envelope eSatPeer = Envelope.documentFactory();
        SubscriptionRequest sensorStatusRequestSatPeer = new SubscriptionRequest(EventMessage.Type.PEER_STATUS, Network.Satellite.name(), new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with Satellite Peer status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    NetworkPeer np = (NetworkPeer)em.getMessage();
                    TopicListener aware = (TopicListener)MVC.loadView(SatelliteSensorOpsView.class, true);
                    aware.modelUpdated(NetworkPeer.class.getName(), np);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, sensorStatusRequestSatPeer, eSatPeer);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eSatPeer);
        OneMFivePlatform.sendRequest(eSatPeer);

        // Full-Spectrum Radio Peer Status
        Envelope eFSRPeer = Envelope.documentFactory();
        SubscriptionRequest sensorStatusRequestFSRPeer = new SubscriptionRequest(EventMessage.Type.PEER_STATUS, Network.FSRadio.name(), new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with Full-Spectrum Radio Peer status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    NetworkPeer np = (NetworkPeer)em.getMessage();
                    TopicListener aware = (TopicListener)MVC.loadView(FullSpectrumRadioSensorOpsView.class, true);
                    aware.modelUpdated(NetworkPeer.class.getName(), np);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, sensorStatusRequestFSRPeer, eFSRPeer);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eFSRPeer);
        OneMFivePlatform.sendRequest(eFSRPeer);

        // LiFi Peer Status
        Envelope eLFPeer = Envelope.documentFactory();
        SubscriptionRequest sensorStatusRequestLFPeer = new SubscriptionRequest(EventMessage.Type.PEER_STATUS, Network.LiFi.name(), new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with LiFi Peer status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    NetworkPeer np = (NetworkPeer)em.getMessage();
                    TopicListener aware = (TopicListener)MVC.loadView(LiFiSensorOpsView.class, true);
                    aware.modelUpdated(NetworkPeer.class.getName(), np);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, sensorStatusRequestLFPeer, eLFPeer);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eLFPeer);
        OneMFivePlatform.sendRequest(eLFPeer);

        return true;
    }
}
