package io.onemfive.network.sensors.radio.technologies.bluetooth;

import io.onemfive.network.sensors.radio.RadioSensor;
import io.onemfive.network.sensors.radio.tasks.RadioTask;
import io.onemfive.network.sensors.radio.tasks.TaskRunner;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class DeviceDiscovery extends RadioTask implements DiscoveryListener {

    private static Logger LOG = Logger.getLogger(DeviceDiscovery.class.getName());

    private final Object inquiryCompletedEvent = new Object();

    private Map<String,RemoteDevice> devices;
    private int result;

    public DeviceDiscovery(Map<String, RemoteDevice> devices, RadioSensor sensor, TaskRunner taskRunner, Properties properties, Long periodicity) {
        super(sensor, taskRunner, properties, periodicity);
        this.devices = devices;
        startRunning = true;
    }

    public int getResult() {
        return result;
    }

    @Override
    public boolean runTask() {
        super.runTask();
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
