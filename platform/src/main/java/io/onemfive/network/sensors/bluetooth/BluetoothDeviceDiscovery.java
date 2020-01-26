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

import io.onemfive.network.NetworkTask;
import io.onemfive.util.tasks.TaskRunner;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class BluetoothDeviceDiscovery extends NetworkTask implements DiscoveryListener {

    private static Logger LOG = Logger.getLogger(BluetoothDeviceDiscovery.class.getName());

    private final Object inquiryCompletedEvent = new Object();

    private BluetoothSensor sensor;
    private Map<String,RemoteDevice> devices;
    private int result;

    public BluetoothDeviceDiscovery(Map<String, RemoteDevice> devices, BluetoothSensor sensor, TaskRunner taskRunner) {
        super(BluetoothDeviceDiscovery.class.getName(), taskRunner, sensor);
        this.devices = devices;
        this.sensor = sensor;
    }

    public int getResult() {
        return result;
    }

    @Override
    public Boolean execute() {
        started = true;
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
        return true;
    }

    @Override
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        String msg = "Device " + remoteDevice.getBluetoothAddress() + " found.";
        if(!devices.containsKey(remoteDevice.getBluetoothAddress())) {
            msg += "\r\nKnown: false";
            devices.put(remoteDevice.getBluetoothAddress(), remoteDevice);
            try {
                msg += "\r\nName: "+remoteDevice.getFriendlyName(false);
            } catch (IOException e) {
                LOG.info(e.getLocalizedMessage());
            }
        } else {
            msg += "\r\nKnown: true";
        }
        LOG.info(msg);
    }

    @Override
    public void inquiryCompleted(int discType) {
        result = discType;
        switch (discType) {
            case DiscoveryListener.INQUIRY_COMPLETED : {
                LOG.info("Bluetooth inquiry completed.");break;
            }
            case DiscoveryListener.INQUIRY_TERMINATED : {
                LOG.warning("Bluetooth inquiry terminated.");break;
            }
            case DiscoveryListener.INQUIRY_ERROR : {
                LOG.severe("Bluetooth inquiry errored.");break;
            }
            default: {
                LOG.warning("Unknown Bluetooth inquiry result code: "+discType);
            }
        }
        synchronized(inquiryCompletedEvent){
            inquiryCompletedEvent.notifyAll();
        }
        lastCompletionTime = System.currentTimeMillis();
        started = false;
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
