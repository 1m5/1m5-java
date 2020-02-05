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
package io.onemfive.network;

import io.onemfive.data.ServiceMessage;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.Map;
import java.util.logging.Logger;

public abstract class Packet extends ServiceMessage {

    private Logger LOG = Logger.getLogger(Packet.class.getName());

    public static int DESTINATION_PEER_REQUIRED = 1;
    public static int DESTINATION_PEER_WRONG_NETWORK = 2;
    public static int DESTINATION_PEER_NOT_FOUND = 3;
    public static int SENDING_FAILED = 4;

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
