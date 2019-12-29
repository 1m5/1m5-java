package io.onemfive.network.sensors;

import io.onemfive.core.LifeCycle;
import io.onemfive.core.util.tasks.TaskRunner;
import io.onemfive.data.*;

import java.io.File;

/**
 * Expected behavior from a Sensor.
 *
 * @author objectorange
 */
public interface Sensor extends LifeCycle {
    void setTaskRunner(TaskRunner taskRunner);
    boolean sendIn(Envelope envelope);
    boolean sendOut(Packet packet);
    boolean replyIn(Envelope envelope);
    boolean replyOut(Packet packet);
    void setNetwork(Network network);
    Network getNetwork();
    SensorStatus getStatus();
    Integer getRestartAttempts();
    Sensitivity getSensitivity();
    String[] getOperationEndsWith();
    String[] getURLBeginsWith();
    String[] getURLEndsWith();
    // When Sensitivities are equal, priority can factor into order of evaluation
    Integer getPriority();
    void setSensorManager(SensorManager sensorManager);
    void setSensitivity(Sensitivity sensitivity);
    void setPriority(Integer priority);
    File getDirectory();
}
