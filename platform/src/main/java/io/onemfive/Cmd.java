package io.onemfive;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.admin.AdminService;
import io.onemfive.data.Envelope;
import io.onemfive.network.NetworkService;
import io.onemfive.util.DLC;

/**
 * Common commands for platform.
 */
public class Cmd {

    public static boolean startSensor(String sensorName) {
        Envelope e = Envelope.documentFactory();
        DLC.addEntity(sensorName, e);
        DLC.addRoute(NetworkService.class, NetworkService.OPERATION_START_SENSOR, e);
        return OneMFiveAppContext.send(e);
    }

    public static boolean stopSensor(String sensorName, boolean hardStop) {
        Envelope e = Envelope.documentFactory();
        DLC.addEntity(sensorName, e);
        DLC.addNVP("hardStop", hardStop, e);
        DLC.addRoute(NetworkService.class, NetworkService.OPERATION_STOP_SENSOR, e);
        return OneMFiveAppContext.send(e);
    }

    public static boolean startBluetoothDiscovery() {
        Envelope e = Envelope.documentFactory();
        DLC.addRoute(NetworkService.class, NetworkService.OPERATION_START_BLUETOOTH_DISCOVERY, e);
        return OneMFiveAppContext.send(e);
    }

    public static boolean stopBluetoothDiscovery() {
        Envelope e = Envelope.documentFactory();
        DLC.addRoute(NetworkService.class, NetworkService.OPERATION_STOP_BLUETOOTH_DISCOVERY, e);
        return OneMFiveAppContext.send(e);
    }

//    public static boolean startService(String serviceName) {
//        Envelope e = Envelope.documentFactory();
//        DLC.addEntity(serviceName, e);
//        DLC.addRoute(AdminService.class, AdminService.OPERATION_START_SERVICE, e);
//        return OneMFiveAppContext.send(e);
//    }
//
//    public static boolean stopService(String serviceName) {
//        Envelope e = Envelope.documentFactory();
//        DLC.addEntity(serviceName, e);
//        DLC.addRoute(AdminService.class, AdminService.OPERATION_STOP_SERVICE, e);
//        return OneMFiveAppContext.send(e);
//    }

}
