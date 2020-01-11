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
package io.onemfive.network.sensors.radio.discovery;

import io.onemfive.network.sensors.radio.RadioPeer;
import io.onemfive.network.sensors.radio.RadioSensor;
import io.onemfive.network.sensors.radio.tasks.RadioTask;
import io.onemfive.network.sensors.radio.tasks.TaskRunner;

import java.util.Properties;
import java.util.logging.Logger;

public class RadioPeerDiscovery extends RadioTask {

    private Logger LOG = Logger.getLogger(RadioPeerDiscovery.class.getName());

    public RadioPeerDiscovery(RadioSensor sensor, TaskRunner taskRunner, Properties properties, long periodicity) {
        super(sensor, taskRunner, properties, periodicity);
    }

    @Override
    public boolean runTask() {
        LOG.info("Starting Radio Peer Discovery...");
        RadioPeer localNode = sensor.getLocalNode();
        if(localNode==null) {
            LOG.info("Local RadioPeer not established yet. Can't run Peer Discovery.");
            return false;
        }
        LOG.info("Completed Radio Peer Discovery.");
        return false;
    }
}
