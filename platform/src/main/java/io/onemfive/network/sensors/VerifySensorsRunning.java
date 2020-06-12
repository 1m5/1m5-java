package io.onemfive.network.sensors;

import io.onemfive.network.NetworkTask;
import io.onemfive.util.tasks.TaskRunner;

import java.util.logging.Logger;

public class VerifySensorsRunning extends NetworkTask {

    private Logger LOG = Logger.getLogger(VerifySensorsRunning.class.getName());

    public VerifySensorsRunning(TaskRunner taskRunner, SensorManager sensorManager) {
        super(VerifySensorsRunning.class.getName(), taskRunner, sensorManager);
    }

    @Override
    public Boolean execute() {
        LOG.info("Starting...");
        running = true;
        for(Sensor sensor : sensorManager.getRegisteredSensors().values()) {
            if(sensor.getStatus()==SensorStatus.NETWORK_UNAVAILABLE) {
                LOG.info("Attempting to start Unavailable Sensor: "+sensor.getClass().getName());
                if(sensor.start(sensorManager.getProperties())) {
                    sensorManager.getActiveSensors().put(sensor.getClass().getName(), sensor);
                }
            }
        }
        running = false;
        LOG.info("Completed.");
        return true;
    }
}
