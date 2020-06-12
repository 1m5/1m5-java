package io.onemfive.network.sensors.i2p;

import io.onemfive.util.tasks.TaskRunner;
import io.onemfive.network.NetworkTask;

public class CheckRouterStats extends NetworkTask {

    public CheckRouterStats(TaskRunner taskRunner, I2PSensor sensor) {
        super(CheckRouterStats.class.getName(), taskRunner, sensor);
    }

    @Override
    public Boolean execute() {
        ((I2PSensor)sensor).checkRouterStats();
        return true;
    }
}
