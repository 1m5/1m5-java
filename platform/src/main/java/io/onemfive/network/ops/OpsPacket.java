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
import io.onemfive.network.Packet;

import java.util.HashMap;
import java.util.Map;

public class OpsPacket extends Packet implements JSONSerializable {

    public static final byte PING_REQUEST = 0x01;
    public static final byte PING_RESPONSE = 0x02;
    public static final byte RELIABLE_PEERS_REQUEST = 0x03;
    public static final byte RELIABLE_PEERS_RESPONSE = 0x04;

    public static final String OPS = "ops";
    public static final String FROM_ID = "fId";
    public static final String FROM_ADDRESS = "fAdd";
    public static final String FROM_NFINGERPRINT = "fNFpt";
    public static final String FROM_NADDRESS = "fNAdd";
    public static final String TO_ID = "tId";
    public static final String TO_NFINGERPRINT = "tNFpt";
    public static final String TO_NADDRESS = "tNAdd";
    public static final String URL = "url";

    public Map<String,Object> atts = new HashMap<>();

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = super.toMap();
        m.putAll(atts);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        atts = m;
    }

}
