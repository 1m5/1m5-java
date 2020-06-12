package io.onemfive.network;

import io.onemfive.data.ServiceMessage;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.Map;
import java.util.logging.Logger;

public abstract class Packet extends ServiceMessage {

    private Logger LOG = Logger.getLogger(Packet.class.getName());

    public static int SENDING_FAILED = 1;

    public static final String ID = "id";
    public static final String TYPE = "type";

    protected String id;
    protected String type;

    public Packet() {
        type = this.getClass().getName();
    }

    public String getId() {
        return id;
    }

    public Packet setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = super.toMap();
        if(id != null) m.put(ID, id);
        if(type != null) m.put(TYPE, type);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m.get(ID) != null) id = (String)m.get(ID);
        if(m.get(TYPE)!=null) type = (String)m.get(TYPE);
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
