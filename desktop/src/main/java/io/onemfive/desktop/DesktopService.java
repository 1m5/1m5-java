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
        Envelope eTor = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestTor = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, SimpleTorSensor.class.getName(),
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
        DLC.addData(SubscriptionRequest.class, subscriptionRequestTor, eTor);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eTor);
        OneMFivePlatform.sendRequest(eTor);

        // I2P Sensor Status
        Envelope eI2P = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestI2P = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, I2PSensor.class.getName(),
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
        DLC.addData(SubscriptionRequest.class, subscriptionRequestI2P, eI2P);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eI2P);
        OneMFivePlatform.sendRequest(eI2P);

        // WiFi Direct Sensor Status
        Envelope eWFD = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestWFD = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, WiFiDirectSensor.class.getName(),
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
        DLC.addData(SubscriptionRequest.class, subscriptionRequestWFD, eWFD);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eWFD);
        OneMFivePlatform.sendRequest(eWFD);

        // Bluetooth Sensor Status
        Envelope eBT = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestBT = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, BluetoothSensor.class.getName(),
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
        DLC.addData(SubscriptionRequest.class, subscriptionRequestBT, eBT);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eBT);
        OneMFivePlatform.sendRequest(eBT);

        // Satellite Sensor Status
        Envelope eSat = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestSat = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, SatelliteSensor.class.getName(),
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
        DLC.addData(SubscriptionRequest.class, subscriptionRequestSat, eSat);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eSat);
        OneMFivePlatform.sendRequest(eSat);

        // Full Spectrum Radio Sensor Status
        Envelope eFSR = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestFSR = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, FullSpectrumRadioSensor.class.getName(),
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
        DLC.addData(SubscriptionRequest.class, subscriptionRequestFSR, eFSR);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eFSR);
        OneMFivePlatform.sendRequest(eFSR);

        // LiFi Sensor Status
        Envelope eLF = Envelope.documentFactory();
        SubscriptionRequest subscriptionRequestLF = new SubscriptionRequest(EventMessage.Type.SENSOR_STATUS, LiFiSensor.class.getName(),
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
        DLC.addData(SubscriptionRequest.class, subscriptionRequestLF, eLF);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, eLF);
        OneMFivePlatform.sendRequest(eLF);

        // Peer Status
        Envelope e2 = Envelope.documentFactory();
        SubscriptionRequest sensorStatusRequest2 = new SubscriptionRequest(EventMessage.Type.PEER_STATUS, PeerManager.class.getName(), new Subscription() {
            @Override
            public void notifyOfEvent(Envelope e) {
                Platform.runLater(() -> {
                    LOG.info("Updating UI with Peer status...");
                    EventMessage em = (EventMessage)e.getMessage();
                    NetworkPeer np = (NetworkPeer)em.getMessage();
                    TopicListener aware = null;
                    switch (np.getNetwork()) {
                        case IMS: {
                            aware = (TopicListener)MVC.loadView(IMSOpsView.class, true);
                            break;
                        }
                        case TOR: {
                            aware = (TopicListener)MVC.loadView(TORSensorOpsView.class, true);
                            break;
                        }
                        case I2P: {
                            aware = (TopicListener)MVC.loadView(I2PSensorOpsView.class, true);
                            break;
                        }
                        case WiFiDirect: {
                            aware = (TopicListener)MVC.loadView(WifiDirectSensorOpsView.class, true);
                            break;
                        }
                        case Bluetooth: {
                            aware = (TopicListener)MVC.loadView(BluetoothSensorOpsView.class, true);
                            break;
                        }
                        case Satellite: {
                            aware = (TopicListener)MVC.loadView(SatelliteSensorOpsView.class, true);
                            break;
                        }
                        case FSRadio: {
                            aware = (TopicListener)MVC.loadView(FullSpectrumRadioSensorOpsView.class, true);
                            break;
                        }
                        case LiFi: {
                            aware = (TopicListener)MVC.loadView(LiFiSensorOpsView.class, true);
                            break;
                        }
                    }
                    if(aware!=null)
                        aware.modelUpdated(NetworkPeer.class.getName(), np);
                });
            }
        });
        DLC.addData(SubscriptionRequest.class, sensorStatusRequest2, e2);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, e2);
        OneMFivePlatform.sendRequest(e2);
        return true;
    }
}
