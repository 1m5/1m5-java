package io.onemfive.network.sensors.fullspectrum.signals;

import io.onemfive.data.JSONSerializable;
import io.onemfive.network.NetworkPacket;
import io.onemfive.network.ops.NetworkNotifyOp;
import io.onemfive.network.ops.NetworkRequestOp;
import io.onemfive.network.sensors.SessionListener;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;
import io.onemfive.network.sensors.fullspectrum.SignalSession;
import io.onemfive.util.RandomUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class SignalSessionBase implements SignalSession, JSONSerializable {

    public static Long MIN_FREQUENCY_HZ = 3L;
    public static Long MAX_FREQUENCY_HZ = 3000000000000L; // 3 THz

    protected Properties properties;

    protected Integer id;
    protected String name;
    protected String address;
    protected Integer port = 0;
    protected Integer score = 0;
    protected Boolean active = true;
    protected String description;
    protected String governingBody = "US-FCC";
    protected Long floorFrequencyHz = MIN_FREQUENCY_HZ;
    protected Long ceilingFrequencyHz = MAX_FREQUENCY_HZ;
    protected Long currentFrequencyHz;

    public SignalSessionBase() {
        id = RandomUtil.nextRandomInteger();
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public boolean init(Properties properties) {
        this.properties = properties;
        return false;
    }

    @Override
    public boolean open(String address) {
        return false;
    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public boolean send(NetworkRequestOp requestOp) {
        return false;
    }

    @Override
    public boolean notify(NetworkNotifyOp notifyOp) {
        return false;
    }

    @Override
    public Boolean send(NetworkPacket packet) {
        return null;
    }

    @Override
    public void addSessionListener(SessionListener listener) {

    }

    @Override
    public void removeSessionListener(SessionListener listener) {

    }

    @Override
    public Status getStatus() {
        return null;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getFullName() {
        return governingBody + ":" + name;
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
    public String toJSON() {
        return JSONPretty.toPretty(JSONParser.toString(toMap()), 4);
    }

    @Override
    public void fromJSON(String json) {
        fromMap((Map<String,Object>)JSONParser.parse(json));
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
