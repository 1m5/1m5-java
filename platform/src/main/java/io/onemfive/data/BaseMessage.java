package io.onemfive.data;

import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add Description
 *
 */
public abstract class BaseMessage implements Message, Persistable {

    protected String clazz;

    private List<String> errorMessages = new ArrayList<>();
    public void addErrorMessage(String errorMessage) {
        errorMessages.add(errorMessage);
    }
    @Override
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    @Override
    public void clearErrorMessages() {
        errorMessages.clear();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        m.put("clazz", this.getClass().getName());
        if(errorMessages!=null) m.put("errorMessages", errorMessages);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("errorMessages")!=null) errorMessages = (List<String>)m.get("errorMessages");
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
