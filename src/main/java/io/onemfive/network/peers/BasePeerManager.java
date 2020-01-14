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

import io.onemfive.core.keyring.AuthNRequest;
import io.onemfive.util.tasks.TaskRunner;
import io.onemfive.data.DID;
import io.onemfive.data.NetworkPeer;
import io.onemfive.network.NetworkService;

import java.util.Properties;
import java.util.logging.Logger;

import static io.onemfive.data.ServiceMessage.NO_ERROR;

public abstract class BasePeerManager implements PeerManager, PeerReport {

    private static final Logger LOG = Logger.getLogger(BasePeerManager.class.getName());

    private Properties properties;

    protected NetworkService service;
    protected NetworkPeer localPeer = new NetworkPeer();
    protected TaskRunner taskRunner;

    public BasePeerManager() {
        localPeer = new NetworkPeer();
        localPeer.setLocal(true);
    }

    public BasePeerManager(TaskRunner runner) {
        this();
        taskRunner = runner;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setNetworkService(NetworkService service) {
        this.service = service;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    @Override
    public NetworkPeer getLocalPeer() {
        return localPeer;
    }

    @Override
    public void updateLocalPeer(AuthNRequest r) {
        if (r.statusCode == NO_ERROR) {
            LOG.info("Updating Local Peer: \n\taddress: "+r.identityPublicKey.getAddress()+"\n\tfingerprint: "+r.identityPublicKey.getFingerprint());
            if(r.identityPublicKey.getAddress()!=null)
                localPeer.setAddress(r.identityPublicKey.getAddress());
            if(r.identityPublicKey.getFingerprint()!=null)
                localPeer.setFingerprint(r.identityPublicKey.getFingerprint());
            DID d = localPeer.getDid();
            d.setAuthenticated(true);
            d.setVerified(true);
            savePeer(localPeer, true);
            LOG.info("Added returned public key to local Peer:"+localPeer);
        } else {
            LOG.warning("Error returned from AuthNRequest: " + r.statusCode);
        }
    }

    @Override
    public void updateLocalPeer(DID d) {
        NetworkPeer updatedNetworkPeer = new NetworkPeer();
        updatedNetworkPeer.setDid(d);
        if(updatedNetworkPeer.getI2PFingerprint()!=null)
            localPeer.setI2PFingerprint(updatedNetworkPeer.getI2PFingerprint());
        if(updatedNetworkPeer.getI2PAddress()!=null)
            localPeer.setI2PAddress(updatedNetworkPeer.getI2PAddress());
        LOG.info("Saving local peer's DID updated with I2P addresses: "+localPeer);
        savePeer(localPeer, false);
        LOG.info("DID with I2P Addresses saved; DCDN Service ready for requests.");
    }

    @Override
    public Boolean init(Properties properties) {
        this.properties = properties;
        return true;
    }

    @Override
    public void run() {
        init(properties);
    }
}
