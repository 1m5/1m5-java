package io.onemfive.data;

import java.util.Map;
import java.util.Random;

/**
 * Events
 *
 * @author objectorange
 */
public final class EventMessage extends BaseMessage {

    public enum Type {
        EMAIL,
        ERROR,
        EXCEPTION,
        STATUS_BUS,
        STATUS_CLIENT,
        STATUS_DID,
        STATUS_SENSOR,
        STATUS_SERVICE,
        TEXT
    }

    private Long id = new Random(4763128720251739L).nextLong();
    private String type;
    private String name;
    private Object message;

    public EventMessage(String type){this.type = type;}

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    @Override
    public Map<String, Object> toMap() {
        return super.toMap();
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
    }
}
