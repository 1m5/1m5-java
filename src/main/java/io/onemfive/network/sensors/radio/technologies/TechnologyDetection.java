package io.onemfive.network.sensors.radio.technologies;

import io.onemfive.network.sensors.radio.Radio;
import io.onemfive.network.sensors.radio.technologies.bluetooth.Bluetooth;
import io.onemfive.network.peers.PeerReport;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Detects what technologies are available on the local node and their status.
 */
public class TechnologyDetection {

    private static final Logger LOG = Logger.getLogger(TechnologyDetection.class.getName());

    public static Map<String,Radio> radiosAvailable(PeerReport peerReport) {
        Map<String,Radio> radios = new HashMap<>();
        LOG.warning("TechnologyDetection.radiosAvailable() not yet implemented. Using Bluetooth to test.");
        Bluetooth btRadio = new Bluetooth();
        btRadio.setPeerReport(peerReport);
        radios.put(Bluetooth.class.getName(), btRadio);
        return radios;
    }

}
