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
import io.onemfive.network.sensors.SensorsConfig;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashMap;
import java.util.Map;

/**
 * Relationships among Network Peers.
 *
 * @author objectorange
 */
public class P2PRelationship implements JSONSerializable {

    public enum RelType implements RelationshipType {
        Known,
        Reliable,
        SuperReliable,
        Banned,
        AvailableThrough
    }

    private Long totalAcks = 0L;
    private Long lastAckTime = 0L;
    private Long avgAckLatencyMS = 0L;
    private String ackTimesTracked;

    public void setTotalAcks(long totalAcks) {
        this.totalAcks = totalAcks;
    }

    public Long getTotalAcks() {
        return totalAcks;
    }

    public void addAckTimeTracked(long t) {
        if(t <= 0) return; // not an ack
        if(ackTimesTracked ==null || ackTimesTracked.isEmpty()) {
            ackTimesTracked = String.valueOf(t);
        } else {
            ackTimesTracked += "," + t;
        }
        int currNumberAcksTracked = ackTimesTracked.split(",").length;
        while(currNumberAcksTracked > SensorsConfig.MaxAT) {
            ackTimesTracked = ackTimesTracked.substring(ackTimesTracked.indexOf(",")+1);
            currNumberAcksTracked--;
        }
        totalAcks++;
    }

    public String getAckTimesTracked() {
        return ackTimesTracked;
    }

    public void setAckTimesTracked(String ackTimes) {
        this.ackTimesTracked = ackTimes;
    }

    public Long getAvgAckLatencyMS() {
        if(ackTimesTracked !=null) {
            String[] times = ackTimesTracked.split(",");
            long sum = 0L;
            long t;
            for (String ts : times) {
                t = Long.parseLong(ts);
                sum += t;
            }
            avgAckLatencyMS = sum / times.length;
        } else {
            avgAckLatencyMS = 0L;
        }
        return avgAckLatencyMS;
    }

    public void setAvgAckLatencyMS(Long avgAckLatencyMS) {
        this.avgAckLatencyMS = avgAckLatencyMS;
    }

    public Long getLastAckTime() {
        return lastAckTime;
    }

    public void setLastAckTime(Long lastAckTime) {
        this.lastAckTime = lastAckTime;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        if(totalAcks !=null) m.put("totalAcks", totalAcks);
        if(avgAckLatencyMS !=null) m.put("avgAckLatencyMS", avgAckLatencyMS);
        if(lastAckTime!=null) m.put("lastAckTime", lastAckTime);
        if(ackTimesTracked !=null) m.put("ackTimesTracked", ackTimesTracked);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m!=null) {
            if(m.get("totalAcks")!=null)
                totalAcks = (Long)m.get("totalAcks");
            if(m.get("avgAckLatencyMS")!=null)
                avgAckLatencyMS = (Long)m.get("avgAckLatencyMS");
            if(m.get("lastAckTime")!=null)
                lastAckTime = (Long)m.get("lastAckTime");
            if(m.get("ackTimesTracked")!=null)
                ackTimesTracked = (String)m.get("ackTimesTracked");
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
