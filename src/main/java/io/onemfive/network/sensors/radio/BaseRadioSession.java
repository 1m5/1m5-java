package io.onemfive.network.sensors.radio;

import io.onemfive.data.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRadioSession implements RadioSession {

    private final Integer id;
    protected Status status = RadioSession.Status.STOPPED;
    protected Radio radio;
    private List<RadioSessionListener> listeners = new ArrayList<>();

    public BaseRadioSession(Radio radio) {
        id = RandomUtil.nextRandomInteger();
        this.radio = radio;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Radio getRadio() {
        return radio;
    }

    @Override
    public void addSessionListener(RadioSessionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeSessionListener(RadioSessionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Status getStatus() {
        return status;
    }

}
