package io.onemfive.network.sensors.radio;

import io.onemfive.data.Addressable;
import io.onemfive.data.DID;
import io.onemfive.data.JSONSerializable;
import io.onemfive.data.NetworkPeer;

import java.util.ArrayList;
import java.util.List;

/**
 * A peer on the Radio network.
 */
public class RadioPeer extends NetworkPeer implements Addressable, JSONSerializable {

    private List<Signal> availableSignals = new ArrayList<>();

    public RadioPeer() {
        this(null, null);
    }

    public RadioPeer(String username, String passphrase) {
        super(NetworkPeer.Network.SDR.name(), username, passphrase);
    }

    public RadioPeer(NetworkPeer peer) {
        fromMap(peer.toMap());
    }

    public void addAvailableSignal(Signal signal){
        availableSignals.add(signal);
    }

    public void removeAvailableSignal(Signal signal) {
        availableSignals.remove(signal);
    }

    public List<Signal> getAvailableSignals() {
        return availableSignals;
    }

    public void clearAvailableSignals(){
        availableSignals.clear();
    }

    public Signal mostAvailableSignal() {
        if(availableSignals.size()==0) {
            return null;
        }
        Signal signal = availableSignals.get(0);
        for(Signal s : availableSignals) {
            if(s.getScore() > signal.getScore()) {
                signal = s;
            }
        }
        return signal;
    }

    @Override
    public Object clone() {
        RadioPeer clone = new RadioPeer();
        clone.did = (DID)did.clone();
        clone.network = network;
        return clone;
    }
}
