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
