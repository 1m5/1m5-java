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
package io.onemfive.network.ops;

import io.onemfive.data.JSONSerializable;
import io.onemfive.data.Network;
import io.onemfive.network.sensors.SensorManager;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.HashMap;
import java.util.Map;

public abstract class NetworkOp implements JSONSerializable {

    protected SensorManager sensorManager;

    public String type;
    public Integer id;
    public String fromId;
    public String fromAddress;
    public Network fromNetwork;
    public String fromNetworkFingerprint;
    public String fromNetworkAddress;
    public Integer fromNetworkPort;
    public transient String toNetworkAddress;
    public transient Integer toNetworkPort;
    public transient Boolean useSSL = false;

    public NetworkOp() {
        type = this.getClass().getName();
    }

    public NetworkOp(SensorManager sensorManager) {
        type = this.getClass().getName();
        this.sensorManager = sensorManager;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("type",type);
        m.put("id",id);
        m.put("fromId",fromId);
        m.put("fromAddress",fromAddress);
        m.put("fromNetwork",fromNetwork.name());
        m.put("fromNetworkFingerprint",fromNetworkFingerprint);
        m.put("fromNetworkAddress",fromNetworkAddress);
        if(fromNetworkPort!=null) m.put("fromNetworkPort",fromNetworkPort);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        type = (String)m.get("type");
        id = (Integer)m.get("id");
        fromId = (String)m.get("fromId");
        fromAddress = (String)m.get("fromAddress");
        fromNetwork = Network.valueOf((String)m.get("fromNetwork"));
        fromNetworkFingerprint = (String)m.get("fromNetworkFingerprint");
        fromNetworkAddress = (String)m.get("fromNetworkAddress");
        if(m.get("fromNetworkPort")!=null) fromNetworkPort = (Integer)m.get("fromNetworkPort");
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
