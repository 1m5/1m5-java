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
package io.onemfive.network.sensors.bluetooth;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.data.PublicKey;
import io.onemfive.network.NetworkTask;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.util.tasks.TaskRunner;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class BluetoothDeviceDiscovery extends NetworkTask implements DiscoveryListener {

    private static Logger LOG = Logger.getLogger(BluetoothDeviceDiscovery.class.getName());

    private final Object inquiryCompletedEvent = new Object();
    private int result;
    private NetworkPeer currentPeer;
    private RemoteDevice currentDevice;

    public static final Map<String, RemoteDevice> remoteDevices = new HashMap<>();

    public BluetoothDeviceDiscovery(BluetoothSensor sensor, TaskRunner taskRunner) {
        super(BluetoothDeviceDiscovery.class.getName(), taskRunner, sensor);
        this.sensor = sensor;
    }

    public int getResult() {
        return result;
    }

    @Override
    public Boolean execute() {
        running = true;
        try {
            RemoteDevice[] devices = LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.CACHED);
            if(devices!=null) {
                for(RemoteDevice device : devices) {
                    remoteDevices.put(device.getBluetoothAddress(), device);
                }
            }
        } catch (BluetoothStateException e) {
            LOG.warning(e.getLocalizedMessage());
        }
        try {
            synchronized (inquiryCompletedEvent) {
                boolean inquiring = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
                if (inquiring) {
                    LOG.info("wait for device inquiry to complete...");
                    inquiryCompletedEvent.wait();
                }
            }
        } catch (BluetoothStateException e) {
            if("Bluetooth Device is not available".equals(e.getLocalizedMessage())) {
                LOG.warning("PLease turn on the bluetooth radio.");
            } else {
                LOG.warning(e.getLocalizedMessage());
            }
            return false;
        } catch (InterruptedException e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        }
        running = false;
        return true;
    }

    @Override
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        String msg = "Device " + remoteDevice.getBluetoothAddress() + " found.";
        currentDevice = remoteDevice;
        try {
            currentPeer = new NetworkPeer(Network.Bluetooth, remoteDevice.getFriendlyName(true), "1234");
            PublicKey pk = currentPeer.getDid().getPublicKey();
            pk.setAddress(remoteDevice.getBluetoothAddress());
            pk.addAttribute("isAuthenticated", remoteDevice.isAuthenticated());
            pk.addAttribute("isEncrypted", remoteDevice.isEncrypted());
            pk.addAttribute("isTrustedDevice", remoteDevice.isTrustedDevice());
            pk.addAttribute("majorDeviceClass", deviceClass.getMajorDeviceClass());
            pk.addAttribute("minorDeviceClass", deviceClass.getMinorDeviceClass());
            pk.addAttribute("serviceClasses", deviceClass.getServiceClasses());
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
        }
        LOG.info(msg);
    }

    @Override
    public void inquiryCompleted(int discType) {
        result = discType;
        switch (discType) {
            case DiscoveryListener.INQUIRY_COMPLETED : {
                LOG.info("Bluetooth inquiry completed. Caching peer.");
                remoteDevices.put(currentDevice.getBluetoothAddress(), currentDevice);
                ((BluetoothSensor)sensor).updateStatus(SensorStatus.NETWORK_CONNECTED);
                break;
            }
            case DiscoveryListener.INQUIRY_TERMINATED : {
                LOG.warning("Bluetooth inquiry terminated.");break;
            }
            case DiscoveryListener.INQUIRY_ERROR : {
                LOG.warning("Bluetooth inquiry errored.");break;
            }
            default: {
                LOG.warning("Unknown Bluetooth inquiry result code: "+discType);
            }
        }
        synchronized(inquiryCompletedEvent){
            inquiryCompletedEvent.notifyAll();
        }
        lastCompletionTime = System.currentTimeMillis();
        running = false;
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
        LOG.warning("servicesDiscovered() implemented in ServiceDiscovery.");
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        LOG.warning("serviceSearchCompleted() implemented in ServiceDiscovery.");
    }
}
