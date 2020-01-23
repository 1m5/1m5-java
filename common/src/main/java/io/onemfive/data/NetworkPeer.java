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

;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A Peer on the 1M5 peer-to-peer network identified by DID and Network and
 * whether or not it is local.
 *
 * @author objectorange
 */
public final class NetworkPeer implements JSONSerializable {

    private static Logger LOG = Logger.getLogger(NetworkPeer.class.getName());

    private String id;
    private Network network;
    private DID did;
    protected Boolean local = false;

    public NetworkPeer() {
        this(Network.IMS, null, null);
    }

    public NetworkPeer(Network network) {
        this(network, null, null);
    }

    public NetworkPeer(String username, String passphrase) {
        this(Network.IMS, username, passphrase);
    }

    public NetworkPeer(Network network, String username, String passphrase) {
        this.network = network;
        did = new DID();
        did.setUsername(username);
        did.setPassphrase(passphrase);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public DID getDid() {
        return did;
    }

    public void setDid(DID did) {
        this.did = did;
    }

    public Boolean getLocal() {
        return local;
    }

    public void setLocal(Boolean local) {
        this.local = local;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(id!=null)
            m.put("id", id);
        if(local!=null)
            m.put("local", local);
        if(network!=null)
            m.put("network", network.name());
        if(did!=null)
            m.put("did",did.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("id")!=null) id = (String)m.get("id");
        if(m.get("local")!=null) local = (Boolean) m.get("local");
        if(m.get("network")!=null) network = Network.valueOf((String)m.get("network"));
        if(m.get("did")!=null) {
            did = new DID();
            if(m.get("did") instanceof String)
                did.fromMap((Map<String,Object>) JSONParser.parse((String)m.get("did")));
            else
                did.fromMap((Map<String,Object>)m.get("did"));
        }
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