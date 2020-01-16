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
import io.onemfive.util.JSONPretty;

import java.util.*;

/**
 * Decentralized IDentification
 *
 * Intentionally does not follow W3C spec as there are disagreements,
 * particularly it models a DID as a group of public keys but
 * 1M5 considers each key its own independent identity as grouping
 * can be an attempt to associate identities involuntarily - that
 * should be up to the individual in how they decide to share any
 * knowledge of identity correlation/association.
 *
 * @author objectorange
 */
public class DID implements Persistable, PIIClearable, JSONSerializable {

    public enum Status {INACTIVE, ACTIVE, SUSPENDED, PRIVATE}

    private String username;
    private volatile String passphrase;
    private volatile String passphrase2;
    private Hash passphraseHash;
    private Hash.Algorithm passphraseHashAlgorithm = Hash.Algorithm.PBKDF2WithHmacSHA1; // Default
    private String description = "";
    private Status status = Status.INACTIVE;
    private volatile Boolean verified = false;
    private volatile Boolean authenticated = false;
    private PublicKey publicKey;

    public DID() {
        publicKey = new PublicKey();
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
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public void clearSensitive() {
        username = null;
        passphrase = null;
        passphrase2 = null;
        description = null;
        status = Status.PRIVATE;
        verified = false;
        authenticated = false;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(username!=null) m.put("username",username);
        if(passphrase!=null) m.put("passphrase",passphrase);
        if(passphraseHash!=null) m.put("passphraseHash",passphraseHash.getHash());
        if(passphraseHashAlgorithm!=null) m.put("passphraseHashAlgorithm",passphraseHashAlgorithm.getName());
        if(passphrase2!=null) m.put("passphrase2",passphrase2);
        if(description!=null) m.put("description",description);
        if(status!=null) m.put("status",status.name());
        if(verified!=null) m.put("verified",verified.toString());
        if(authenticated!=null) m.put("authenticated",authenticated.toString());
        if(publicKey!=null) m.put("publicKey", publicKey.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("username")!=null) username = (String)m.get("username");
        if(m.get("passphrase")!=null) passphrase = (String)m.get("passphrase");
        if(m.get("passphraseHashAlgorithm")!=null) passphraseHashAlgorithm = Hash.Algorithm.valueOf((String)m.get("passphraseHashAlgorithm"));
        if(m.get("passphraseHash")!=null) passphraseHash = new Hash(((String)m.get("passphraseHash")), passphraseHashAlgorithm);
        if(m.get("passphrase2")!=null) passphrase2 = (String)m.get("passphrase2");
        if(m.get("description")!=null) description = (String)m.get("description");
        if(m.get("status")!=null) status = Status.valueOf((String)m.get("status"));
        if(m.get("verified")!=null) verified = Boolean.parseBoolean((String)m.get("verified"));
        if(m.get("authenticated")!=null) authenticated = Boolean.parseBoolean((String)m.get("authenticated"));
        if(m.get("publicKey")!=null) {
            publicKey = new PublicKey();
            publicKey.fromMap((Map<String,Object>)m.get("publicKey"));
        }
    }

    @Override
    public String toJSON() {
        return JSONPretty.toPretty(JSONParser.toString(toMap()), 4);
    }

    @Override
    public void fromJSON(String json) {
        fromMap((Map<String,Object>)JSONParser.parse(json));
    }

    @Override
    public String toString() {
        return toJSON();
    }

}
