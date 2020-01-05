package io.onemfive.data.social;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Group extends Party {

    private List<Individual> individuals = new ArrayList<>();

    public void add(Individual i) {
        individuals.add(i);
    }

    public List<Individual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(List<Individual> i) {
        individuals = i;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = super.toMap();
        if(individuals!=null) {
            List<Map<String,Object>> ind = new ArrayList<>();
            m.put("individuals", ind);
            for(Individual i : individuals) {
                ind.add(i.toMap());
            }
        }
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m!=null) {
            individuals = new ArrayList<>();
            List<Map<String,Object>> ind = (List<Map<String,Object>>)m.get("individuals");
            for(Map<String,Object> mi : ind) {
                Individual i = new Individual();
                i.fromMap(mi);
                individuals.add(i);
            }
        }
    }
}
