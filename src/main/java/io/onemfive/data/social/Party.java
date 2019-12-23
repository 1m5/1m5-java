package io.onemfive.data.social;

/**
 * An identity representing an individual or a group.
 *
 * @author objectorange
 */
public abstract class Party {

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
}
