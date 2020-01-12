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
package io.onemfive.data;

import io.onemfive.util.JSONParser;

import java.util.*;

/**
 * Decentralized IDentification
 *
 * An identity container for both personal and network identities.
 * Requires an username and passphrase to secure them.
 * Personal identities are managed through the Key Ring Service while
 * network identities are persisted to the local hard drive.
 *
 * @author objectorange
 */
public class DID implements Persistable, PIIClearable, JSONSerializable {

    public enum Status {INACTIVE, ACTIVE, SUSPENDED}

    public static String VERSION = "https://w3id.org/did/v1";

    public static String DEFAULT_ALIAS = "default";

    private String username = DEFAULT_ALIAS;
    private volatile String passphrase;
    private volatile String passphrase2;
    private Hash passphraseHash;
    private Hash.Algorithm passphraseHashAlgorithm = Hash.Algorithm.PBKDF2WithHmacSHA1; // Default
    private String description = "";
    private Status status = Status.INACTIVE;
    private volatile Boolean verified = false;
    private volatile Boolean authenticated = false;
    // Identities used for personal identification: Alias, PublicKey
    private Map<String, PublicKey> identities = new HashMap<>();
    // Identities used in peer networks: Network name, NetworkPeer
    private Map<String, NetworkPeer> peers = new HashMap<>();
    // Attributes
    private Map<String,Object> attributes = new HashMap<>();

    public DID() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getPassphrase2() {
        return passphrase2;
    }

    public void setPassphrase2(String passphrase2) {
        this.passphrase2 = passphrase2;
    }

    public void addPeer(NetworkPeer networkPeer) {
        peers.put(networkPeer.getNetwork(), networkPeer);
    }

    public NetworkPeer getPeer(String network) {
        return peers.get(network);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean getVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Hash getPassphraseHash() {
        return passphraseHash;
    }

    public void setPassphraseHash(Hash passphraseHash) {
        this.passphraseHash = passphraseHash;
    }

    public Hash.Algorithm getPassphraseHashAlgorithm() {
        return passphraseHash.getAlgorithm();
    }

    public void setPassphraseHashAlgorithm(Hash.Algorithm passphraseHashAlgorithm) {
        this.passphraseHashAlgorithm = passphraseHashAlgorithm;
    }

    public PublicKey getPublicKey() {
        if(username != null) {
            if(identities.get(username)==null)
                identities.put(username,new PublicKey());
            return identities.get(username);
        }
        else if(identities.get(DEFAULT_ALIAS)!=null)
            return identities.get(DEFAULT_ALIAS);
        else {
            PublicKey pk = new PublicKey();
            identities.put(DEFAULT_ALIAS,pk);
            pk.setAlias(DEFAULT_ALIAS);
            return pk;
        }
    }

    public PublicKey getPublicKey(String alias) {
        return identities.get(alias);
    }

    public void addPublicKey(PublicKey publicKey) {
        identities.put(publicKey.getAlias(), publicKey);
    }

    public Collection<PublicKey> availableIdentities(){
        return identities.values();
    }

    public Collection<NetworkPeer> availableNetworkPeers() {
        return peers.values();
    }

    public NetworkPeer getNetworkPeer(Network network) {
        if(peers.containsKey(network.name())) {
            return peers.get(network.name());
        }
        return null;
    }

    public Map<String,Object> getAttributes(){
        return attributes;
    }

    public void addAttribute(String name, Object attribute) {
        if(attributes==null) {
            attributes = new HashMap<>();
        }
        attributes.put(name, attribute);
    }

    public Object getAttribute(String name) {
        if(attributes==null) {
            return null;
        }
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        if(attributes!=null) {
            attributes.remove(name);
        }
    }

    @Override
    public void clearSensitive() {
        username = null;
        passphrase = null;
        passphrase2 = null;
        passphraseHash = null;
        passphraseHashAlgorithm = null;
        description = null;
        status = Status.ACTIVE;
        verified = false;
        authenticated = false;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(username!=null)
            m.put("username",username);
        if(passphrase!=null)
            m.put("passphrase",passphrase);
        if(passphraseHash!=null)
            m.put("passphraseHash",passphraseHash.getHash());
        if(passphraseHashAlgorithm!=null)
            m.put("passphraseHashAlgorithm",passphraseHashAlgorithm.getName());
        if(passphrase2!=null)
            m.put("passphrase2",passphrase2);
        if(description!=null)
            m.put("description",description);
        if(status!=null)
            m.put("status",status.name());
        if(verified!=null)
            m.put("verified",verified.toString());
        if(authenticated!=null)
            m.put("authenticated",authenticated.toString());
        if(identities != null && identities.size() > 0) {
            Map<String,Object> ids = new HashMap<>();
            m.put("identities",ids);
            Set<String> aliases = identities.keySet();
            for(String a : aliases) {
                Map<String,Object> key = new HashMap<>();
                ids.put(a, key);
                PublicKey p = identities.get(a);
                key.put("alias", String.valueOf(p.getAlias()));
                key.put("fingerprint", p.getFingerprint());
                key.put("address", p.getAddress());
            }
        }
        if(peers != null && peers.size() > 0) {
            Map<String,Object> pm = new HashMap<>();
            m.put("peers",pm);
            Set<String> networks = peers.keySet();
            for(String n : networks) {
                pm.put(n, peers.get(n).toMap());
            }
        }
        if(attributes!=null && attributes.size() > 0) {
            m.put("attributes", attributes);
        }
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("username")!=null)
            username = (String)m.get("username");
        if(m.get("passphrase")!=null)
            passphrase = (String)m.get("passphrase");
        if(m.get("passphraseHashAlgorithm")!=null)
            passphraseHashAlgorithm = Hash.Algorithm.valueOf((String)m.get("passphraseHashAlgorithm"));
        if(m.get("passphraseHash")!=null)
            passphraseHash = new Hash(((String)m.get("passphraseHash")), passphraseHashAlgorithm);
        if(m.get("passphrase2")!=null)
            passphrase2 = (String)m.get("passphrase2");
        if(m.get("description")!=null)
            description = (String)m.get("description");
        if(m.get("status")!=null)
            status = Status.valueOf((String)m.get("status"));
        if(m.get("verified")!=null)
            verified = Boolean.parseBoolean((String)m.get("verified"));
        if(m.get("authenticated")!=null)
            authenticated = Boolean.parseBoolean((String)m.get("authenticated"));
        if(m.get("identities")!=null) {
            Map<String,Object> im = (Map<String,Object>)m.get("identities");
            identities = new HashMap<>();
            Set<String> aliases = im.keySet();
            PublicKey key;
            for(String a : aliases) {
                Map<String,Object> km = (Map<String,Object>)im.get(a);
                key = new PublicKey();
                key.setAlias((String)km.get("alias"));
                key.setFingerprint((String)km.get("fingerprint"));
                key.setAddress((String)km.get("address"));
                identities.put(a, key);
            }
        }
        NetworkPeer p;
        if(m.get("peers")!=null){
            Map<String,Object> pm = (Map<String,Object>)m.get("peers");
            peers = new HashMap<>();
            Set<String> networks = pm.keySet();
            for(String n : networks) {
                p = new NetworkPeer();
                p.fromMap((Map<String,Object>)pm.get(n));
                peers.put(n, p);
            }
        }
        if(m.get("attributes")!=null) {
            attributes = (Map<String,Object>)m.get("atttributes");
        }
    }

    public Map<String,Object> toDocumentMap() {
        return toDocumentMap(DEFAULT_ALIAS, true);
    }

    public Map<String,Object> toDocumentMap(String alias, Boolean authenticate) {
        Map<String,Object> m = new HashMap<>();

        // Context
        m.put("@context",VERSION);

        if(alias==null)
            alias = DEFAULT_ALIAS;

        PublicKey publicKey = getPublicKey(alias);
        if(publicKey == null) {
            return m;
        }

        // Subject
        m.put("id","did:1m5:"+publicKey.getFingerprint());

        if(authenticate) {
            List<Map<String, Object>> authnList = new ArrayList<>();
            m.put("authentication", authnList);
            Map<String, Object> authNM = new HashMap<>();
            authNM.put("id", "did:1m5:"+publicKey.getFingerprint());
            authNM.put("type", "RsaVerificationKey2018");
            authNM.put("controller", "did:1m5:" + publicKey.getFingerprint());
            authNM.put("publicKeyPem", publicKey.getAddress());
            authnList.add(authNM);
        }

        List<Map<String,Object>> serviceList = new ArrayList<>();
        m.put("service", serviceList);
        Map<String,Object> serviceM = new HashMap<>();
        serviceM.put("type", "io.onemfive.did.DIDService");
        serviceM.put("serviceEndpoint", "1m5:io.onemfive.did.DIDService");
        serviceList.add(serviceM);

        return m;
    }

    @Override
    public Object clone() {
        DID clone = new DID();
        clone.username = username;
        clone.passphrase = passphrase;
        clone.passphraseHash = new Hash(passphraseHash.getHash(), passphraseHash.getAlgorithm());
        clone.passphraseHashAlgorithm = passphraseHashAlgorithm;
        clone.passphrase2 = passphrase2;
        clone.description = description;
        clone.status = status;
        clone.verified = verified;
        clone.authenticated = authenticated;
        Set<String> aliases = identities.keySet();
        for(String alias : aliases) {
            clone.identities.put(alias,(PublicKey)identities.get(alias).clone());
        }
        Set<String> networks = peers.keySet();
        for(String network : networks) {
            clone.peers.put(network, (NetworkPeer)peers.get(network).clone());
        }
        return clone;
    }

    @Override
    public String toString() {
        return JSONParser.toString(toMap());
    }

}
