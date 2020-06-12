package io.onemfive.core.infovault;

public abstract class BaseInfoVaultDB implements InfoVaultDB {

    protected String location;
    protected String name;
    protected Status status = Status.Shutdown;

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Status getStatus() {
        return null;
    }
}
