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
package io.onemfive.network.sensors;

import io.onemfive.core.Config;
import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.data.Sensitivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class SensorsConfig {

    private static final Logger LOG = Logger.getLogger(SensorsConfig.class.getName());

    public static void update(Properties properties) {
        try {
            properties.putAll(Config.loadFromClasspath("sensors.config", properties, false));
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        if(properties.getProperty("io.onemfive.network.sensitivity") != null) {
            try {
                currentSensitivity = Sensitivity.valueOf(properties.getProperty("io.onemfive.network.sensitivity"));
            } catch (IllegalArgumentException e) {
                LOG.warning(e.getLocalizedMessage()+"\n\tSetting default sensitivity to: "+Sensitivity.HIGH.name());
                if(currentSensitivity==null) {
                    currentSensitivity = Sensitivity.HIGH;
                }
            }
        }
        if(properties.getProperty("onemfive.sensors.seeds") != null) {
            String i2pSeedsStr = properties.getProperty("onemfive.sensors.seeds");
            if(i2pSeedsStr!=null && !"".equals(i2pSeedsStr)) {
                String[] sl = i2pSeedsStr.split(",");
                NetworkPeer np;
                String[] na;
                for(String s : sl) {
//                    na = s.split("|");
//                    np = new NetworkPeer(na[0]);
//                    np.setAddress(na[1]);
                    // For now just assume I2P
                    np = new NetworkPeer(Network.I2P.name());
                    np.setAddress(s);
                    seeds.add(np);
                }
            }
        }
        if(properties.getProperty("onemfive.sensors.banned") != null) {
            String i2pBannedStr = properties.getProperty("onemfive.sensors.banned");
            if(i2pBannedStr!=null && !"".equals(i2pBannedStr)) {
                String[] bl = i2pBannedStr.split(",");
                NetworkPeer np;
                String[] na;
                for(String b : bl) {
                    na = b.split("|");
                    np = new NetworkPeer(na[0]);
                    np.setAddress(na[1]);
                    banned.add(np);
                }
            }
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

    public static Sensitivity currentSensitivity = Sensitivity.HIGH; // Default
    // ------------ Discovery ---------------
    // Seeds
    public static List<NetworkPeer> seeds = new ArrayList<>();
    // Banned
    public static List<NetworkPeer> banned = new ArrayList<>();
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

}
