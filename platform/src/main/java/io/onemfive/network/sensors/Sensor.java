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
package io.onemfive.network.sensors;

import io.onemfive.core.LifeCycle;
import io.onemfive.data.Envelope;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
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
}
