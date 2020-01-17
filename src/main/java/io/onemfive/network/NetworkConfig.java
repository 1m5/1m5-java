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

import io.onemfive.core.Config;
import io.onemfive.data.ManCon;
import io.onemfive.util.JSONParser;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class NetworkConfig {

    private static final Logger LOG = Logger.getLogger(NetworkConfig.class.getName());

    public static ManCon currentManCon = ManCon.HIGH; // Default
    // ------------ Discovery ---------------
    public static String env = "devnet"; // default
    // Seeds
    public static Map<String,List<NetworkPeer>> seeds = new HashMap<>();
    // Banned
    public static Map<String,List<NetworkPeer>> banned = new HashMap<>();
    // Min Peers Tracked - the point at which Discovery process goes into 'hyper' mode.
    public static int MinPT = 10;
    // Max Peers Tracked - the total number of Peers to attempt to maintain knowledge of
    public static int MaxPT = 100;
    // Max Peers Sent - Maximum number of peers to send in a peer list (the bigger a datagram, the less chance of it getting through).
    public static int MaxPS = 5;
    // Max Acknowledgments Tracked
    public static int MaxAT = 20;
    // Update Interval - seconds between Discovery process
    public static int UI = 60;
    // Reliable Peer Min Acks
    public static int MinAckRP = 20;
    // Super Reliable Peer Min Acks
    public static int MinAckSRP = 10000;

    public static void update(Properties properties) {
        try {
            properties.putAll(Config.loadFromClasspath("1m5-network.config", properties, false));
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        if(properties.getProperty("io.onemfive.network.sensitivity") != null) {
            try {
                currentManCon = ManCon.valueOf(properties.getProperty("io.onemfive.network.sensitivity"));
            } catch (IllegalArgumentException e) {
                LOG.warning(e.getLocalizedMessage()+"\n\tSetting default sensitivity to: "+ ManCon.HIGH.name());
                if(currentManCon ==null) {
                    currentManCon = ManCon.HIGH;
                }
            }
        }

        try {
            Map<String,Object> pm = (Map<String,Object>)JSONParser.parse(Paths.get("1m5-peers.json"));
            List<Map<String,Object>> envScope = (List<Map<String,Object>>)pm.get("peers");
            for(Map<String,Object> eM : envScope) {
                String env = (String)eM.get("environment");
                NetworkPeer np;
                List<Map<String,Object>> seedList = (List<Map<String,Object>>)eM.get("seeds");
                for(Map<String,Object> s : seedList) {
                    np = new NetworkPeer();
                    np.fromMap(s);
                    if(seeds.get(env)==null) seeds.put(env, new ArrayList<>());
                    seeds.get(env).add(np);
                }
                List<Map<String,Object>> bannedList = (List<Map<String,Object>>)eM.get("banned");
                for(Map<String,Object> b : bannedList) {
                    np = new NetworkPeer();
                    np.fromMap(b);
                    if(banned.get(env)==null) banned.put(env, new ArrayList<>());
                    banned.get(env).add(np);
                }
            }
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
        }

        if(properties.getProperty("onemfive.sensors.MinPT") != null) {
            MinPT = Integer.parseInt(properties.getProperty("onemfive.sensors.MinPT"));
        }
        if(properties.getProperty("onemfive.sensors.MaxPT") != null) {
            MaxPT = Integer.parseInt(properties.getProperty("onemfive.sensors.MaxPT"));
        }
        if(properties.getProperty("onemfive.sensors.MaxPS") != null) {
            MaxPS = Integer.parseInt(properties.getProperty("onemfive.sensors.MaxPS"));
        }
        if(properties.getProperty("onemfive.sensors.MaxAT") != null) {
            MaxAT = Integer.parseInt(properties.getProperty("onemfive.sensors.MaxAT"));
        }
        if(properties.getProperty("onemfive.sensors.UI") != null) {
            UI = Integer.parseInt(properties.getProperty("onemfive.sensors.UI"));
        }
        if(properties.getProperty("onemfive.sensors.MinAckRP") != null) {
            MinAckRP = Integer.parseInt(properties.getProperty("onemfive.sensors.MinAckRP"));
        }
        if(properties.getProperty("onemfive.sensors.MinAckSRP") != null) {
            MinAckSRP = Integer.parseInt(properties.getProperty("onemfive.sensors.MinAckSRP"));
        }
    }

}
