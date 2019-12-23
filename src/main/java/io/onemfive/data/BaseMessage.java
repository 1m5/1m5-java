package io.onemfive.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class BaseMessage implements Message, Persistable {

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
        m.put("type", this.getClass().getName());
        if(errorMessages!=null) m.put("errorMessages", errorMessages);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("errorMessages")!=null) errorMessages = (List<String>)m.get("errorMessages");
    }
}
