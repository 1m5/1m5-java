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
public class RadioDetection {

    private static final Logger LOG = Logger.getLogger(RadioDetection.class.getName());

    public static Map<String, Radio> radiosAvailable(PeerReport peerReport) {
        Map<String, Radio> radios = new HashMap<>();
        LOG.info("Currently only Bluetooth supported.");
        Bluetooth btRadio = new Bluetooth();
        btRadio.setPeerReport(peerReport);
        radios.put(Bluetooth.class.getName(), btRadio);
        return radios;
    }

}
