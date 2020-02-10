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
package io.onemfive.network.peers.graph;

import io.onemfive.data.JSONSerializable;
import io.onemfive.data.Network;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;
import org.neo4j.graphdb.RelationshipType;

import java.util.*;

/**
 * Relationships among Network Peers.
 *
 * @author objectorange
 */
public class P2PRelationship implements JSONSerializable {

    public static final String TOTAL_ACKS = "totalAcks";
    public static final String LAST_ACK_TIME = "lastAckTime";
    public static final String AVG_ACK_LATENCY_MS = "avgAckLatencyMS";
    public static final String MEDIAN_ACK_LATENCY_MS = "medAckLatencyMS";

    /**
     * Relationship Type is based on what Network was used to establish it.
     */
    public enum RelType implements RelationshipType {
        HTTPS,
        TOR,
        I2P,
        Bluetooth,
        WiFiDirect,
        Satellite,
        FSRadio,
        LiFi,
        IMS
    }

    private Long totalAcks = 0L;
    private Long lastAckTime = 0L;
    private LinkedList<Long> ackTimesTracked = new LinkedList<>();

    public void setTotalAcks(long totalAcks) {
        this.totalAcks = totalAcks;
    }

    public Long getTotalAcks() {
        return totalAcks;
    }

    public void addAckTimeTracked(long t, int maxAcksTracked) {
        if(t <= 0) return; // not an ack
        ackTimesTracked.add(t);
        while(ackTimesTracked.size() > maxAcksTracked) {
            ackTimesTracked.removeFirst();
        }
        totalAcks++;
    }

    public void setAckTimesTracked(String trackedFlattened) {
        String[] tracked = trackedFlattened.split(",");
        for(String time : tracked) {
            ackTimesTracked.add(Long.parseLong(time));
        }
    }

    public String getAckTimesTracked() {
        StringBuilder sb = new StringBuilder();
        for(Long time : ackTimesTracked) {
            sb.append(time+",");
        }
        String trackedFlattened = sb.toString();
        trackedFlattened = trackedFlattened.substring(0, trackedFlattened.length()-1);
        return trackedFlattened;
    }

    public Long getAvgAckLatencyMS() {
        long sum = 0L;
        for (long ts : ackTimesTracked) {
            sum += ts;
        }
        return sum / ackTimesTracked.size();
    }

    public Long getMedAckLatencyMS() {
        ackTimesTracked.sort((t1, t2) -> (int)(t1 - t2));
        return ackTimesTracked.get(ackTimesTracked.size() / 2);
    }

    public void setLastAckTime(Long lastAckTime) {
        this.lastAckTime = lastAckTime;
    }
    public Long getLastAckTime() {
        return lastAckTime;
    }

    public Boolean isReliable() {
        return totalAcks > 100
                && getAvgAckLatencyMS() < 8000
                && getMedAckLatencyMS() < 8000;
    }

    public Boolean isSuperReliable() {
        return totalAcks > 1000
                && getAvgAckLatencyMS() < 4000
                && getMedAckLatencyMS() < 4000;
    }

    public Boolean isRealTime() {
        return getAvgAckLatencyMS() < 1000 && getMedAckLatencyMS() < 1000;
    }

    public Boolean belowMaxLatency(Long maxLatency) {
        return getAvgAckLatencyMS() < maxLatency && getMedAckLatencyMS() < maxLatency;
    }

    public static P2PRelationship.RelType networkToRelationship(Network network) {
        switch (network) {
            case HTTPS: return P2PRelationship.RelType.HTTPS;
            case TOR: return P2PRelationship.RelType.TOR;
            case I2P: return P2PRelationship.RelType.I2P;
            case Bluetooth: return P2PRelationship.RelType.Bluetooth;
            case WiFiDirect: return P2PRelationship.RelType.WiFiDirect;
            case Satellite: return P2PRelationship.RelType.Satellite;
            case FSRadio: return P2PRelationship.RelType.FSRadio;
            case LiFi: return P2PRelationship.RelType.LiFi;
            case IMS: return P2PRelationship.RelType.IMS;
            default: return null;
        }
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        if(totalAcks !=null) m.put(TOTAL_ACKS, totalAcks);
        m.put(AVG_ACK_LATENCY_MS, getAvgAckLatencyMS());
        m.put(MEDIAN_ACK_LATENCY_MS, getMedAckLatencyMS());
        if(lastAckTime!=null) m.put(LAST_ACK_TIME, lastAckTime);
        if(ackTimesTracked !=null) m.put("ackTimesTracked", getAckTimesTracked());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m!=null) {
            if(m.get(TOTAL_ACKS)!=null)
                totalAcks = (Long)m.get(TOTAL_ACKS);
            if(m.get(LAST_ACK_TIME)!=null)
                lastAckTime = (Long)m.get(LAST_ACK_TIME);
            if(m.get("ackTimesTracked")!=null) {
                setAckTimesTracked((String)m.get("ackTimesTracked"));
            }
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
