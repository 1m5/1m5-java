package io.onemfive.network.sensors.bluetooth;

import io.onemfive.network.NetworkTask;
import io.onemfive.util.tasks.TaskRunner;

import javax.bluetooth.LocalDevice;

public class CheckPowerStatus extends NetworkTask {

    private boolean powerOn = false;

    public CheckPowerStatus(TaskRunner taskRunner, BluetoothSensor sensor) {
        super(CheckPowerStatus.class.getName(), taskRunner, sensor);
    }

    @Override
    public Boolean execute() {
        if(!powerOn && LocalDevice.isPowerOn()) {
            powerOn = true;
            ((BluetoothSensor)sensor).awaken();
        } else {
            powerOn = false;
            ((BluetoothSensor)sensor).sleep();
        }
        return true;
    }
}
