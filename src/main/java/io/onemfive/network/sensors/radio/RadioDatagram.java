package io.onemfive.network.sensors.radio;

import io.onemfive.data.JSONSerializable;
import io.onemfive.data.content.Content;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class RadioDatagram implements JSONSerializable {

    private Logger LOG = Logger.getLogger(RadioDatagram.class.getName());

    public RadioPeer from;
    public RadioPeer to;
    public RadioPeer destination;
    public Content content;

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(from!=null) m.put("from",from.toMap());
        if(to!=null) m.put("to",to.toMap());
        if(destination!=null) m.put("destination",destination.toMap());
        if(content!=null) m.put("content",content.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("from")!=null) {
            from = new RadioPeer();
            from.fromMap((Map<String,Object>)m.get("from"));
        }
        if(m.get("to")!=null) {
            to = new RadioPeer();
            to.fromMap((Map<String,Object>)m.get("to"));
        }
        if(m.get("destination")!=null) {
            destination = new RadioPeer();
            destination.fromMap((Map<String,Object>)m.get("destination"));
        }
        if(m.get("content")!=null) {
            try {
                content = Content.newInstance(m);
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
            content.fromMap((Map<String,Object>)m.get("content"));
        }
    }
}
