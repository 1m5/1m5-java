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
package io.onemfive.network.sensors.i2p;

import io.onemfive.network.*;
import io.onemfive.network.peers.P2PRelationship;
import io.onemfive.util.tasks.TaskRunner;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Cycle through all known peers randomly to build and maintain known peer database.
 *
 * @author objectorange
 */
public class I2PPeerDiscovery extends NetworkTask {

    private Logger LOG = Logger.getLogger(I2PPeerDiscovery.class.getName());
    private boolean firstRun = true;
    private NetworkService service;
    private I2PSensor sensor;

    public I2PPeerDiscovery(String taskName, I2PSensor sensor, NetworkService service, TaskRunner taskRunner, Properties properties) {
        super(taskName, taskRunner, properties);
        this.sensor = sensor;
        this.service = service;
    }

    @Override
    public Long getPeriodicity() {
        if(firstRun) {
            // five minutes to give time for 1M5 Sensor Manager to warm up establishing network sessions
            // TODO: replace with event-driven notification of 1M5 Sensor Manager status
            return 5 * 60 * 1000L;
        }
        else
            return NetworkConfig.UI * 1000L; // wait for UI seconds
    }

    @Override
    public Boolean execute() {
        LOG.info("Running I2P Peer Discovery...");
        NetworkPeer localI2PPeer = service.getPeerManager().getLocalNode().getLocalNetworkPeer(Network.I2P);
        if(localI2PPeer == null) {
            LOG.warning("Network Service doesn't have local Peer yet. Can't run Peer Updater.");
            return false;
        }
        long totalKnown = service.getPeerManager().totalPeersByRelationship(localI2PPeer, P2PRelationship.RelType.Known);
        if(totalKnown < 1) {
            LOG.info("No I2P peers known.");
            if(NetworkConfig.seeds!=null && NetworkConfig.seeds.size() > 0) {
                // Launch Seeds
                List<NetworkPeer> seeds = NetworkConfig.seeds.get(NetworkConfig.env);
                for (NetworkPeer seed : seeds) {
                    if(seed.getNetwork()==Network.I2P) {
                        LOG.info("Sending Peer Status Request to Seed Peer:\n\t" + seed);
                        Request request = new Request();
                        request.setOriginationPeer(localI2PPeer);
                        request.setDestinationPeer(seed);
                        sensor.sendOut(request);
                        LOG.info("Sent Peer Status Request to Seed Peer.");
                    }
                }
            } else {
                LOG.warning("No seeds available! Please provide at least one seed!");
                return false;
            }
        } else if(totalKnown < NetworkConfig.MaxPT) {
            LOG.info(totalKnown+" known peers less than Maximum Peers Tracked of "+ NetworkConfig.MaxPT+"; continuing peer discovery...");
            NetworkPeer p = service.getPeerManager().getRandomKnownPeer(localI2PPeer);
            if(p != null) {
                LOG.info("Sending Peer Status Request to Known Peer...");
//                service.pingOut(p);
                LOG.info("Sent Peer Status Request to Known Peer.");
            }
        } else {
            LOG.info("Maximum Peers Tracked of "+ NetworkConfig.MaxPT+" reached. No need to look for more.");
        }
        firstRun = false;
        return true;
    }

}
