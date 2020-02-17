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
import io.onemfive.data.NetworkPeer;

import java.util.Map;
import java.util.logging.Logger;

public abstract class NetworkPacket extends Packet {

    private Logger LOG = Logger.getLogger(NetworkPacket.class.getName());

    public static int DESTINATION_PEER_REQUIRED = 2;
    public static int DESTINATION_PEER_WRONG_NETWORK = 3;
    public static int DESTINATION_PEER_NOT_FOUND = 4;
    public static int NO_ENVELOPE = 5;
    public static int NO_ROUTE = 6;
    public static int NO_SERVICE = 7;
    public static int NO_OPERATION = 8;
    public static int NO_ADDRESS = 9;
    public static int NO_FINGERPRINT = 10;
    public static int NO_PORT = 11;

    protected Envelope envelope;

    protected NetworkPeer originationPeer;
    protected NetworkPeer fromPeer;
    protected NetworkPeer toPeer;
    protected NetworkPeer destinationPeer;

    protected Boolean delayed = false;
    protected Long minDelay = 0L;
    protected Long maxDelay = 0L;

    protected Boolean copy = false;
    protected Integer minCopies = 0;
    protected Integer maxCopies = 0;

    public NetworkPacket() {
        super();
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

    public NetworkPacket setOriginationPeer(NetworkPeer originationPeer) {
        this.originationPeer = originationPeer;
        return this;
    }

    public NetworkPeer getFromPeer() {
        return fromPeer;
    }

    public NetworkPacket setFromPeer(NetworkPeer fromPeer) {
        this.fromPeer = fromPeer;
        return this;
    }

    public NetworkPeer getToPeer() {
        return toPeer;
    }

    public NetworkPacket setToPeer(NetworkPeer toPeer) {
        this.toPeer = toPeer;
        return this;
    }

    public NetworkPeer getDestinationPeer() {
        return destinationPeer;
    }

    public NetworkPacket setDestinationPeer(NetworkPeer destinationPeer) {
        this.destinationPeer = destinationPeer;
        return this;
    }

    public Boolean getDelayed() {
        return delayed;
    }

    public void setDelayed(Boolean delayed) {
        this.delayed = delayed;
    }

    public Long getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(Long minDelay) {
        this.minDelay = minDelay;
    }

    public Long getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(Long maxDelay) {
        this.maxDelay = maxDelay;
    }

    public Boolean getCopy() {
        return copy;
    }

    public void setCopy(Boolean copy) {
        this.copy = copy;
    }

    public Integer getMinCopies() {
        return minCopies;
    }

    public void setMinCopies(Integer minCopies) {
        this.minCopies = minCopies;
    }

    public Integer getMaxCopies() {
        return maxCopies;
    }

    public void setMaxCopies(Integer maxCopies) {
        this.maxCopies = maxCopies;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = super.toMap();
        if(envelope != null) m.put("envelope", envelope.toMap());
        if(originationPeer != null) m.put("originationPeer", originationPeer.toMap());
        if(fromPeer != null) m.put("fromPeer", fromPeer.toMap());
        if(toPeer != null) m.put("toPeer", toPeer.toMap());
        if(destinationPeer != null) m.put("destinationPeer", destinationPeer.toMap());
        if(delayed != null) m.put("delayed", delayed);
        if(minDelay != null) m.put("minDelay", minDelay);
        if(maxDelay != null) m.put("maxDelay", maxDelay);
        if(copy != null) m.put("copy", copy);
        if(minCopies != null) m.put("minCopies", minCopies);
        if(maxCopies != null) m.put("maxCopies", maxCopies);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
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
        if(m.get("delayed") != null) delayed = (Boolean)m.get("delayed");
        if(m.get("minDelay") != null) minDelay = (Long)m.get("minDelay");
        if(m.get("maxDelay") != null) maxDelay = (Long)m.get("maxDelay");
        if(m.get("copy") != null) copy = (Boolean)m.get("copy");
        if(m.get("minCopies") != null) minCopies = (Integer)m.get("minCopies");
        if(m.get("maxCopies") != null) maxCopies = (Integer)m.get("maxCopies");
    }
}
