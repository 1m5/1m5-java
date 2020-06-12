package io.onemfive.network.sensors.fullspectrum.bands;

public abstract class Band {

    protected final long begin;
    protected final long end;

    public Band(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }
}
