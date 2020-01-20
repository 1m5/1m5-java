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
package io.onemfive.network.peers;

import io.onemfive.data.JSONSerializable;
import io.onemfive.network.NetworkConfig;
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

    public enum RelType implements RelationshipType {
        Known,
        Reliable,
        SuperReliable,
        Banned
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

    public void addAckTimeTracked(long t) {
        if(t <= 0) return; // not an ack
        ackTimesTracked.add(t);
        while(ackTimesTracked.size() > NetworkConfig.MaxAT) {
            ackTimesTracked.removeFirst();
        }
        totalAcks++;
    }

    public LinkedList<Long> getAckTimesTracked() {
        return ackTimesTracked;
    }

    public Long getAvgAckLatencyMS() {
        long sum = 0L;
        for (long ts : ackTimesTracked) {
            sum += ts;
        }
        return sum / ackTimesTracked.size();
    }

    public Long getMedAckLatencyMS() {
        return ackTimesTracked.get(ackTimesTracked.size() / 2);
    }

    public void setLastAckTime(Long lastAckTime) {
        this.lastAckTime = lastAckTime;
    }
    public Long getLastAckTime() {
        return lastAckTime;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        if(totalAcks !=null) m.put(TOTAL_ACKS, totalAcks);
        m.put(AVG_ACK_LATENCY_MS, getAvgAckLatencyMS());
        m.put(MEDIAN_ACK_LATENCY_MS, getMedAckLatencyMS());
        if(lastAckTime!=null) m.put(LAST_ACK_TIME, lastAckTime);
        if(ackTimesTracked !=null) m.put("ackTimesTracked", ackTimesTracked);
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
                ackTimesTracked = (LinkedList<Long>) m.get("ackTimesTracked");
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
