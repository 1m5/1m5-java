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
package io.onemfive.network.sensors.radio.technologies.bluetooth;

import io.onemfive.network.sensors.radio.RadioSensor;
import io.onemfive.network.sensors.radio.tasks.RadioTask;
import io.onemfive.network.sensors.radio.tasks.TaskRunner;

import javax.bluetooth.*;
import javax.bluetooth.UUID;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ServiceDiscovery extends RadioTask implements DiscoveryListener {

    private static final Logger LOG = Logger.getLogger(ServiceDiscovery.class.getName());

    private final Object serviceSearchCompletedEvent = new Object();

    private Map<String, RemoteDevice> devices;
    private Map<String, List<String>> deviceServices;
    private Map<String, BluetoothPeer> peers;

    private RemoteDevice currentDevice;

    private int result;

    public ServiceDiscovery(Map<String, RemoteDevice> devices, Map<String, List<String>> deviceServices, Map<String, BluetoothPeer> peers, RadioSensor sensor, TaskRunner taskRunner, Properties properties, Long periodicity) {
        super(sensor, taskRunner, properties, periodicity);
        this.devices = devices;
        this.deviceServices = deviceServices;
        this.peers = peers;
        startRunning = false;
    }

    public int getResult() {
        return result;
    }

    @Override
    public boolean runTask() {
        super.runTask();
        started = true;
        UUID obexObjPush = ServiceClasses.getUUID(ServiceClasses.OBEX_OBJECT_PUSH);
//        if ((properties != null) && (properties.size() > 0)) {
//            objPush = new UUID(args[0], false);
//        }
//        UUID obexFileXfer = ServiceClasses.getUUID(ServiceClasses.OBEX_FILE_TRANSFER);
//        UUID oneMFiveEnvPush = ServiceClasses.getUUID(ServiceClasses.ONEMFIVE_ENVELOPE_PUSH);
//        UUID oneMFiveBinXfer = ServiceClasses.getUUID(ServiceClasses.ONEMFIVE_BINARY_TRANSFER);

        UUID[] searchUuidSet = new UUID[] { obexObjPush };
//        UUID[] searchUuidSet = new UUID[] { obexObjPush, obexFileXfer, oneMFiveEnvPush, oneMFiveBinXfer };

        int[] attrIDs =  new int[] {
                0x0100 // Service name
        };
        Collection<RemoteDevice> deviceList = devices.values();
        LOG.info(deviceList.size()+" devices to search services on...");
        for(RemoteDevice device : deviceList) {
            currentDevice = device;
            try {
                synchronized (serviceSearchCompletedEvent) {
                    LOG.info("search services on " + device.getBluetoothAddress() + " " + device.getFriendlyName(false));
                    LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, device, this);
                    serviceSearchCompletedEvent.wait();
                }
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            } catch (InterruptedException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        lastCompletionTime = System.currentTimeMillis();
        started = false;
        return true;
    }

    @Override
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        LOG.warning("deviceDiscovered() implemented in DeviceDiscovery.");
    }

    @Override
    public void inquiryCompleted(int discType) {
        LOG.warning("inquiryCompleted() implemented in DeviceDiscovery.");
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
        LOG.info(serviceRecords.length+" Services returned for transID: "+transID);
        for (int i = 0; i < serviceRecords.length; i++) {
            String url = serviceRecords[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            if (url == null) {
                LOG.info("Not a NoAuthN-NoEncrypt service.");
                continue;
            }
            if(deviceServices.get(currentDevice.getBluetoothAddress())==null) {
                deviceServices.put(currentDevice.getBluetoothAddress(), new ArrayList<>());
            }
            if(!deviceServices.get(currentDevice.getBluetoothAddress()).contains(url)) {
                deviceServices.get(currentDevice.getBluetoothAddress()).add(url);
            }
            DataElement serviceName = serviceRecords[i].getAttributeValue(0x0100);
            if (serviceName != null) {
                LOG.info("service " + serviceName.getValue() + " found " + url);
                // TODO: Ensure 1M5 has service name then perform check
//                if("1M5".equals(serviceName.getValue())) {
                    BluetoothPeer peer;
                    if(peers.get(currentDevice.getBluetoothAddress())==null) {
                        peer = new BluetoothPeer();
                        peer.setAddress(currentDevice.getBluetoothAddress());
                        peer.setUrl(url);
                        peer.setLocal(false);
                        peers.put(currentDevice.getBluetoothAddress(), peer);
                    }
//                }
            } else {
                LOG.info("service found " + url);
            }
        }
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        result = respCode;
        LOG.info("transID: "+transID);
        switch(respCode) {
            case DiscoveryListener.SERVICE_SEARCH_COMPLETED : {
                LOG.info("Bluetooth search completed.");break;
            }
            case DiscoveryListener.SERVICE_SEARCH_TERMINATED : {
                LOG.warning("Bluetooth search terminated.");break;
            }
            case DiscoveryListener.SERVICE_SEARCH_ERROR : {
                LOG.warning("Bluetooth search errored. Removing device from list.");
                devices.remove(currentDevice.getBluetoothAddress());
                deviceServices.remove(currentDevice.getBluetoothAddress());
                peers.remove(currentDevice.getBluetoothAddress());
                break;
            }
            case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS : {
                try {
                    LOG.info("Bluetooth search found no records for device (address; "+currentDevice.getBluetoothAddress()+", name: "+currentDevice.getFriendlyName(false)+").");
                } catch (IOException e) {
                    LOG.info("Bluetooth search found no records for device (address; "+currentDevice.getBluetoothAddress()+").");
                }
            }
            case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE : {
                try {
                    LOG.info("Bluetooth search device (address; "+currentDevice.getBluetoothAddress()+", name: "+currentDevice.getFriendlyName(false)+") not reachable.");
                } catch (IOException e) {
                    LOG.info("Bluetooth search device (address; "+currentDevice.getBluetoothAddress()+") not reachable.");
                }
                break;
            }
            default: {
                LOG.warning("Unknown Bluetooth search result: "+respCode);
            }
        }
        synchronized (serviceSearchCompletedEvent) {
            serviceSearchCompletedEvent.notifyAll();
        }
    }
}
