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

import io.onemfive.data.Envelope;
import io.onemfive.data.ServiceMessage;

import java.util.Map;
import java.util.logging.Logger;

public abstract class Packet extends ServiceMessage {

    public static int DESTINATION_PEER_REQUIRED = 1;
    public static int DESTINATION_PEER_WRONG_NETWORK = 2;
    public static int DESTINATION_PEER_NOT_FOUND = 3;
    public static int NO_ENVELOPE = 4;
    public static int NO_ROUTE = 5;
    public static int NO_SERVICE = 6;
    public static int NO_OPERATION = 7;
    public static int SENDING_FAILED = 8;

    private Logger LOG = Logger.getLogger(Packet.class.getName());

    private String id;
    protected Envelope envelope;

    protected String type;
    protected NetworkPeer originationPeer;
    protected NetworkPeer fromPeer;
    protected NetworkPeer toPeer;
    protected NetworkPeer destinationPeer;

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

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public NetworkPeer getOriginationPeer() {
        return originationPeer;
    }

    public Packet setOriginationPeer(NetworkPeer originationPeer) {
        this.originationPeer = originationPeer;
        return this;
    }

    public NetworkPeer getFromPeer() {
        return fromPeer;
    }

    public Packet setFromPeer(NetworkPeer fromPeer) {
        this.fromPeer = fromPeer;
        return this;
    }

    public NetworkPeer getToPeer() {
        return toPeer;
    }

    public Packet setToPeer(NetworkPeer toPeer) {
        this.toPeer = toPeer;
        return this;
    }

    public NetworkPeer getDestinationPeer() {
        return destinationPeer;
    }

    public Packet setDestinationPeer(NetworkPeer destinationPeer) {
        this.destinationPeer = destinationPeer;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = super.toMap();
        if(id != null) m.put("id", id);
        if(type != null) m.put("type", type);
        if(envelope != null) m.put("envelope", envelope.toMap());
        if(originationPeer != null) m.put("originationPeer", originationPeer.toMap());
        if(fromPeer != null) m.put("fromPeer", fromPeer.toMap());
        if(toPeer != null) m.put("toPeer", toPeer.toMap());
        if(destinationPeer != null) m.put("destinationPeer", destinationPeer.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m.get("id") != null) id = (String)m.get("id");
        if(m.get("type")!=null) type = (String)m.get("type");
        if(m.get("envelope") != null) {
            Map<String, Object> dm = (Map<String, Object>)m.get("envelope");
            envelope = new Envelope();
        }
        if(m.get("originationPeer") != null) {
            fromPeer = new NetworkPeer();
            fromPeer.fromMap((Map<String, Object>)m.get("originationPeer"));
        }
        if(m.get("fromPeer") != null) {
            fromPeer = new NetworkPeer();
            fromPeer.fromMap((Map<String, Object>)m.get("fromPeer"));
        }
        if(m.get("toPeer") != null) {
            toPeer = new NetworkPeer();
            toPeer.fromMap(((Map<String, Object>)m.get("toPeer")));
        }
        if(m.get("destinationPeer") != null) {
            fromPeer = new NetworkPeer();
            fromPeer.fromMap((Map<String, Object>)m.get("destinationPeer"));
        }
    }
}
