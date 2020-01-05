package io.onemfive.data.social;

import io.onemfive.data.JSONSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * An identity representing an individual or a group.
 *
 * @author objectorange
 */
public abstract class Party implements JSONSerializable {

    private Profile profile;
    private String name;

    public Party(){}

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(name!=null) m.put("name",name);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m!=null) {
            if(m.get("name")!=null) {
                name = (String)m.get("name");
            }
        }
    }
}
