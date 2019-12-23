package io.onemfive.network.sensors.radio.signals;

import io.onemfive.data.JSONSerializable;
import io.onemfive.data.util.JSONParser;
import io.onemfive.data.util.JSONPretty;
import io.onemfive.network.sensors.radio.Signal;

import java.util.HashMap;
import java.util.Map;

public abstract class SignalBase implements Signal, JSONSerializable {

    public static Long MIN_FREQUENCY_HZ = 3L;
    public static Long MAX_FREQUENCY_HZ = 3000000000000L; // 3 THz

    protected String name;
    protected Integer port = 0;
    protected Integer score = 0;
    protected Boolean active = true;
    protected String description;
    protected String governingBody = "US-FCC";
    protected Long floorFrequencyHz = MIN_FREQUENCY_HZ;
    protected Long ceilingFrequencyHz = MAX_FREQUENCY_HZ;
    protected Long currentFrequencyHz;

    public String getId() {
        return governingBody + ":" + name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGoverningBody() {
        return governingBody;
    }

    public void setGoverningBody(String governingBody) {
        this.governingBody = governingBody;
    }

    public Long getFloorFrequencyHz() {
        return floorFrequencyHz;
    }

    public void setFloorFrequencyHz(Long floorFrequencyHz) {
        this.floorFrequencyHz = floorFrequencyHz;
    }

    public Long getCeilingFrequencyHz() {
        return ceilingFrequencyHz;
    }

    public void setCeilingFrequencyHz(Long ceilingFrequencyHz) {
        this.ceilingFrequencyHz = ceilingFrequencyHz;
    }

    public Long getCurrentFrequencyHz() {
        return currentFrequencyHz;
    }

    public void setCurrentFrequencyHz(Long currentFrequencyHz) {
        this.currentFrequencyHz = currentFrequencyHz;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(getId()!=null) m.put("id",getId());
        if(getName()!=null) m.put("name",getName());
        if(getPort()!=null) m.put("port",getPort());
        if(getGoverningBody()!=null) m.put("governingBody",getGoverningBody());
        if(getActive()!=null) m.put("active", getActive());
        if(getDescription()!=null) m.put("description", getDescription());
        if(getFloorFrequencyHz()!=null) m.put("floorFrequencyHz", getFloorFrequencyHz());
        if(getCeilingFrequencyHz()!=null) m.put("ceilingFrequencyHz", getCeilingFrequencyHz());
        if(getCurrentFrequencyHz()!=null) m.put("currentFrequencyHz", getCurrentFrequencyHz());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("name")!=null) name = (String)m.get("name");
        if(m.get("port")!=null) port = Integer.parseInt((String)m.get("port"));
        if(m.get("governingBody")!=null) governingBody = (String)m.get("governingBody");
        if(m.get("active")!=null) active = Boolean.parseBoolean((String)m.get("active"));
        if(m.get("description")!=null) description = (String)m.get("description");
        if(m.get("floorFrequencyHz")!=null) floorFrequencyHz = Long.parseLong((String)m.get("floorFrequencyHz"));
        if(m.get("ceilingFrequencyHz")!=null) ceilingFrequencyHz = Long.parseLong((String)m.get("ceilingFrequencyHz"));
        if(m.get("currentFrequencyHz")!=null) currentFrequencyHz = Long.parseLong((String)m.get("currentFrequencyHz"));
    }

    @Override
    public String toString() {
        return JSONPretty.toPretty(JSONParser.toString(toMap()), 4);
    }
}
