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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class PublicKey implements Addressable, JSONSerializable {

    private String alias;
    private String fingerprint;
    private String address;
    private Boolean isIdentityKey = false;
    private Boolean isEncryptionKey = false;

    public PublicKey() {}

    public PublicKey(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    public void setEncodedBase64(byte[] encoded) {
        this.address = Base64.getEncoder().encodeToString(encoded);
    }

    public boolean isIdentityKey() {
        return isIdentityKey;
    }

    public void isIdentityKey(boolean identityKey) {
        isIdentityKey = identityKey;
    }

    public boolean isEncryptionKey() {
        return isEncryptionKey;
    }

    public void isEncryptionKey(boolean encryptionKey) {
        isEncryptionKey = encryptionKey;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        if(alias!=null) m.put("alias",alias);
        if(fingerprint!=null) m.put("fingerprint",fingerprint);
        if(address !=null) m.put("address", address);
        if(isEncryptionKey!=null) m.put("isEncryptionKey",isEncryptionKey);
        if(isIdentityKey!=null) m.put("isIdentityKey",isIdentityKey);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("alias")!=null) alias = (String)m.get("alias");
        if(m.get("fingerprint")!=null) fingerprint = (String)m.get("fingerprint");
        if(m.get("address")!=null) address = (String)m.get("address");
        if(m.get("isEncryptionKey")!=null) isEncryptionKey = (Boolean)m.get("isEncryptionKey");
        if(m.get("isIdentityKey")!=null) isIdentityKey = (Boolean)m.get("isIdentityKey");
    }

    @Override
    public Object clone() {
        PublicKey clone = new PublicKey();
        clone.alias = alias;
        clone.fingerprint = fingerprint;
        clone.address = address;
        return clone;
    }
}
