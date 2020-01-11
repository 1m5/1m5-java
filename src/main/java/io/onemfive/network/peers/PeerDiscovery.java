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

import io.onemfive.util.tasks.TaskRunner;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.sensors.SensorsConfig;
import io.onemfive.network.NetworkTask;
import io.onemfive.network.NetworkService;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Cycle through all known peers randomly to build and maintain known peer database.
 *
 * @author objectorange
 */
public class PeerDiscovery extends NetworkTask {

    private Logger LOG = Logger.getLogger(PeerDiscovery.class.getName());
    private boolean firstRun = true;
    private NetworkService service;

    public PeerDiscovery(String taskName, NetworkService service, TaskRunner taskRunner, Properties properties) {
        super(taskName, taskRunner, properties);
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
            return SensorsConfig.UI * 1000L; // wait for UI seconds
    }

    @Override
    public Boolean execute() {
        LOG.info("Running Peer Discovery...");
        NetworkPeer localPeer = service.getPeerManager().getLocalPeer();
        if(localPeer == null) {
            LOG.warning("Sensors Service doesn't have local Peer yet. Can't run Peer Updater.");
            return false;
        }
        long totalKnown = service.getPeerManager().totalPeersByRelationship(localPeer, P2PRelationship.RelType.Known);
        if(totalKnown < 1) {
            LOG.info("No peers known.");
            if(SensorsConfig.seeds!=null && SensorsConfig.seeds.size() > 0) {
                // Launch Seeds
                for (NetworkPeer seed : SensorsConfig.seeds) {
                    LOG.info("Sending Peer Status Request to Seed Peer:\n\tNetwork: " + seed.getNetwork() + "\n\tFingerprint: "+seed.getFingerprint()+"\n\tAddress: "+seed.getAddress());
//                    service.pingOut(seed);
                    LOG.info("Sent Peer Status Request to Seed Peer.");
                }
            } else {
                LOG.warning("No seeds available! Please provide at least one seed!");
                return false;
            }
        } else if(totalKnown < SensorsConfig.MaxPT) {
            LOG.info(totalKnown+" known peers less than Maximum Peers Tracked of "+ SensorsConfig.MaxPT+"; continuing peer discovery...");
            NetworkPeer p = service.getPeerManager().getRandomPeer(localPeer);
            if(p != null) {
                LOG.info("Sending Peer Status Request to Known Peer...");
//                service.pingOut(p);
                LOG.info("Sent Peer Status Request to Known Peer.");
            }
        } else {
            LOG.info("Maximum Peers Tracked of "+ SensorsConfig.MaxPT+" reached. No need to look for more.");
        }
        firstRun = false;
        return true;
    }

}
