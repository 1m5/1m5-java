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
