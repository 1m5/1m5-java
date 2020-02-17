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

import io.onemfive.core.ServiceStatus;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkState {

    public Network network = Network.IMS; // Default
    public NetworkPeer localPeer;
    public SensorStatus sensorStatus;
    public ServiceStatus serviceStatus;
    public Integer virtualPort;
    public Integer targetPort;
    // Seeds
    public final List<NetworkPeer> seeds = new ArrayList<>();
    // Banned
    public final List<NetworkPeer> banned = new ArrayList<>();
    // Min Peers Tracked - the point at which Discovery process goes into 'hyper' mode.
    public int MinPT = 10;
    // Max Peers Tracked - the total number of Peers to attempt to maintain knowledge of
    public int MaxPT = 100;
    // Max Peers Sent - Maximum number of peers to send in a peer list (the bigger a datagram, the less chance of it getting through).
    public int MaxPS = 5;
    // Max Acknowledgments Tracked
    public int MaxAT = 20;
    // Update Interval - seconds between Discovery process
    public int UpdateInterval = 60;
    // Update Interval Hyper - seconds between Discovery process when no reliable peers are known
    public int UpdateIntervalHyper = 5;
    // Reliable Peer Min Acks
    public int MinAckRP = 20;
    // Super Reliable Peer Min Acks
    public int MinAckSRP = 10000;

    public Map<String,Object> params = new HashMap<>();

    @Override
    public String toString() {
        Map<String,Object> m = new HashMap<>();
        if(network!=null) m.put("network", network.name());
        if(sensorStatus!=null) m.put("sensorStatus", sensorStatus.name());
        if(serviceStatus!=null) m.put("serviceStatus", serviceStatus.name());
        if(virtualPort!=null) m.put("virtualPort", virtualPort);
        if(targetPort!=null) m.put("targetPort", targetPort);
        if(localPeer!=null && localPeer.getDid().getUsername()!=null) m.put("username", localPeer.getDid().getUsername());
        if(localPeer!=null) m.put("fingerprint", localPeer.getDid().getPublicKey().getFingerprint());
        if(localPeer!=null) m.put("address", localPeer.getDid().getPublicKey().getAddress());
        if(params!=null) m.put("params", params);
        return JSONPretty.toPretty(JSONParser.toString(m), 4);
    }
}
