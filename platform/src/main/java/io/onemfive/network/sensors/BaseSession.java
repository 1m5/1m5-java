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

import io.onemfive.network.ops.NetworkNotifyOp;
import io.onemfive.network.ops.NetworkOp;
import io.onemfive.network.ops.NetworkRequestOp;
import io.onemfive.network.ops.NetworkResponseOp;
import io.onemfive.util.RandomUtil;

import java.util.*;

public abstract class BaseSession implements SensorSession {

    private final Integer id;
    protected Properties properties;
    protected Status status = SensorSession.Status.STOPPED;
    private List<SessionListener> listeners = new ArrayList<>();
    protected String address;
    protected BaseSensor sensor;
    protected Map<Integer,NetworkRequestOp> waitingOps = new HashMap<>();

    public BaseSession(BaseSensor sensor) {
        this.sensor = sensor;
        id = RandomUtil.nextRandomInteger();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public boolean init(Properties properties) {
        this.properties = properties;
        return true;
    }

    @Override
    public void addSessionListener(SessionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void handle(NetworkOp networkOp) {
        networkOp.setSensorManager(sensor.getSensorManager());
        if(networkOp instanceof NetworkNotifyOp) {
            ((NetworkNotifyOp) networkOp).publish();
        } else if(networkOp instanceof NetworkRequestOp) {
            NetworkRequestOp requestOp = (NetworkRequestOp)networkOp;
            requestOp.operate();
        } else if(networkOp instanceof NetworkResponseOp) {
            long end = System.currentTimeMillis();
            NetworkResponseOp responseOp = (NetworkResponseOp)networkOp;
            if(waitingOps.get(responseOp.id)!=null) {
                responseOp.requestOp = waitingOps.get(responseOp.id);
            }
            responseOp.operate();
            sensor.getSensorManager().getPeerManager().savePeerStatusTimes(responseOp.requestOp.fromId, sensor.network, responseOp.fromId, responseOp.requestOp.start, end);
        }
    }

}
