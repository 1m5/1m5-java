package io.onemfive.data;

import io.onemfive.util.RandomUtil;

import java.util.Map;

/**
 * Events
 *
 */
public final class EventMessage extends BaseMessage {

    public enum Type {
        EMAIL,
        ERROR,
        EXCEPTION,
        BUS_STATUS,
        CLIENT_STATUS,
        PEER_STATUS,
        SENSOR_STATUS,
        SERVICE_STATUS,
        DID_STATUS,
        NETWORK_STATE_UPDATE,
        HTML,
        JSON,
        TEXT
    }

    private Long id = RandomUtil.nextRandomLong();

    private String type;
    private String name;
    private Object message;

    public EventMessage(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = super.toMap();
        m.put("type", type);
        if(name!=null) m.put("name", name);
        if(message!=null) m.put("message", message);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m.get("type")!=null) type = (String)m.get("type");
        if(m.get("name")!=null) name = (String)m.get("name");
        if(m.get("message")!=null) message = m.get("message");
    }
}
