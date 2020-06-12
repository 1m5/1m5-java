package io.onemfive.network.sensors;

import io.onemfive.core.LifeCycle;
import io.onemfive.data.Envelope;
import io.onemfive.data.Network;
import io.onemfive.network.NetworkState;
import io.onemfive.network.NetworkPacket;
import io.onemfive.util.tasks.TaskRunner;

import java.io.File;

/**
 * Expected behavior from a Sensor.
 *
 * @author objectorange
 */
public interface Sensor extends LifeCycle {
    void setTaskRunner(TaskRunner taskRunner);
    boolean sendOut(NetworkPacket packet);
    boolean sendIn(Envelope envelope);
    void setNetwork(Network network);
    Network getNetwork();
    SensorStatus getStatus();
    Integer getRestartAttempts();
    String[] getOperationEndsWith();
    String[] getURLBeginsWith();
    String[] getURLEndsWith();
    SensorSession establishSession(String address, Boolean autoConnect);
    void releaseSession(SensorSession sensorSession);
    void setSensorManager(SensorManager sensorManager);
    SensorManager getSensorManager();
    File getDirectory();
    NetworkState getNetworkState();
    void updateState(NetworkState networkState);
}
