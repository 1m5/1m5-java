package io.onemfive.network.sensors.i2p;

import io.onemfive.core.util.tasks.TaskRunner;
import io.onemfive.network.NetworkTask;

public class CheckRouterStats extends NetworkTask {

    private I2PSensor sensor;

    public CheckRouterStats(String taskName, TaskRunner taskRunner, I2PSensor sensor) {
        super(taskName, taskRunner);
        super.periodicity = 60 * 1000L; // Every 60 seconds
        this.sensor = sensor;
    }

    @Override
    public Boolean execute() {
        sensor.checkRouterStats();
        return true;
    }
}
