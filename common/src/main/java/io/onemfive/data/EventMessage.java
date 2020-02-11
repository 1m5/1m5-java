/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.data;

import io.onemfive.util.RandomUtil;

import java.util.Map;

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
