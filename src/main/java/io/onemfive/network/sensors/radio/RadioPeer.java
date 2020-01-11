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
package io.onemfive.network.sensors.radio;

import io.onemfive.data.*;

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
        super(Network.SDR.name(), username, passphrase);
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
