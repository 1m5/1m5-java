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
package io.onemfive.network.sensors.tor;

import io.onemfive.data.JSONSerializable;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;
import io.onemfive.util.RandomUtil;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class TORHiddenService implements JSONSerializable {

    public Integer virtualPort;
    public Integer targetPort;
    public String serviceID;
    public String privateKey;

    public TORHiddenService() {}

    public TORHiddenService(String serviceID, String privateKey) throws NoSuchAlgorithmException {
        this(randomTORPort(), randomTORPort(), serviceID, privateKey);
    }

    public TORHiddenService(Integer virtualPort, Integer targetPort, String serviceID, String privateKey) throws NoSuchAlgorithmException {
        this.virtualPort = virtualPort;
        this.targetPort = targetPort;
        this.serviceID = serviceID;

        if (privateKey.startsWith("-----BEGIN")) // we reused a key
            this.privateKey = privateKey;
        else {
            String type = null;
            if (privateKey.startsWith(TORAlgorithms.RSA1024))
                type = "RSA";
            else if (privateKey.startsWith(TORAlgorithms.ED25519V3))
                type = "OPENSSH";
            else
                throw new NoSuchAlgorithmException(type);
            this.privateKey = "-----BEGIN " + type + " PRIVATE KEY-----\n"
                    + privateKey.substring(privateKey.indexOf(":") + 1) + "\n-----END " + type
                    + " PRIVATE KEY-----";
        }
    }

    public static int randomTORPort() {
        return RandomUtil.nextRandomInteger(10000, 65535);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(virtualPort !=null) m.put("virtualPort", virtualPort);
        if(targetPort != null) m.put("targetPort", targetPort);
        if(serviceID!=null) m.put("serviceID", serviceID);
        if(privateKey!=null) m.put("privateKey", privateKey);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("virtualPort")!=null) virtualPort = (Integer)m.get("virtualPort");
        if(m.get("targetPort")!=null) targetPort = (Integer)m.get("targetPort");
        if(m.get("serviceID")!=null) serviceID = (String)m.get("serviceID");
        if(m.get("privateKey")!=null) privateKey = (String)m.get("privateKey");
    }

    @Override
    public String toJSON() {
        return JSONPretty.toPretty(JSONParser.toString(toMap()), 4);
    }

    @Override
    public void fromJSON(String json) {
        fromMap((Map<String,Object>)JSONParser.parse(json));
    }
}
