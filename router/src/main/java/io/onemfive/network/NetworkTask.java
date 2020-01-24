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
package io.onemfive.network;

import io.onemfive.data.NetworkNode;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.peers.PeerManager;
import io.onemfive.network.sensors.Sensor;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.util.tasks.BaseTask;
import io.onemfive.util.tasks.TaskRunner;

/**
 * A Task with access to NetworkService.
 *
 * @author objectorange
 */
public abstract class NetworkTask extends BaseTask {

    protected Sensor sensor;
    protected SensorManager sensorManager;
    protected PeerManager peerManager;
    protected NetworkNode localNode;
    protected NetworkPeer localPeer;

    public NetworkTask(String taskName, TaskRunner taskRunner, Sensor sensor) {
        super(taskName, taskRunner);
        this.sensor = sensor;
        sensorManager = sensor.getSensorManager();
        peerManager = sensorManager.getPeerManager();
        localNode = peerManager.getLocalNode();
        localPeer = localNode.getLocalNetworkPeer();
    }
}
