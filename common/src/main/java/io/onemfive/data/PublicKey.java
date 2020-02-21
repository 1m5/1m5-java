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
 * TODO: Add Description
 *
 * @author objectorange
 */
public class PublicKey implements Addressable, JSONSerializable {

    private String alias;
    private String fingerprint;
    private String address;
    private String type;
    private Boolean isIdentityKey = false;
    private Boolean isEncryptionKey = false;
    private Boolean isBase64Encoded = false;
    private Boolean isBase58Encoded = false;
    private Boolean isPEM = false;
    private Boolean isHex = false;
    private Map<String,Object> attributes = new HashMap<>();
    // Content signed is in the format of name:value.
    private Map<String,List<byte[]>> signedAttributes = new HashMap<>();

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Boolean isBase64Encoded() {
        return isBase64Encoded;
    }

    public void setBase64Encoded(Boolean base64Encoded) {
        isBase64Encoded = base64Encoded;
    }

    public Boolean isBase58Encoded() {
        return isBase58Encoded;
    }

    public void setBase58Encoded(Boolean base58Encoded) {
        isBase58Encoded = base58Encoded;
    }

    public Boolean isPEM() {
        return isPEM;
    }

    public void setPEM(Boolean PEM) {
        isPEM = PEM;
    }

    public Boolean isHex() {
        return isHex;
    }

    public void setHex(Boolean hex) {
        isHex = hex;
    }

    public Map<String,Object> getAttributes(){
        return attributes;
    }

    public void setAttributes(Map<String,Object> attributes) {
        this.attributes = attributes;
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

    public Map<String, List<byte[]>> getSignedAttributes() {
        return signedAttributes;
    }

    public void setSignedAttributes(Map<String, List<byte[]>> signedAttributes) {
        this.signedAttributes = signedAttributes;
    }

    public void addSignedAttribute(String name, byte[] signedAttributeNameValue) {
        if(signedAttributes==null) signedAttributes = new HashMap<>();
        if(signedAttributes.get(name)==null) signedAttributes.put(name, new ArrayList<>());
        signedAttributes.get(name).add(signedAttributeNameValue);
    }

    public void removeSignedAttribute(String name, byte[] signedAttributeNameValue) {
        if(signedAttributes!=null) {
            List<byte[]> bytes = signedAttributes.get(name);
            if(bytes!=null) {
                int pos = -1;
                int i=0;
                for(byte[] b : bytes) {
                    if(b.equals(signedAttributeNameValue)) {
                        pos = i;
                    }
                    i++;
                }
                if(pos>-1) {
                    bytes.remove(pos);
                }
            }
        }
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        if(alias!=null) m.put("alias",alias);
        if(fingerprint!=null) m.put("fingerprint",fingerprint);
        if(address!=null) m.put("address", address);
        if(type!=null) m.put("type", type);
        if(isEncryptionKey!=null) m.put("isEncryptionKey",isEncryptionKey);
        if(isIdentityKey!=null) m.put("isIdentityKey",isIdentityKey);
        if(isBase64Encoded!=null) m.put("isBase64Encoded",isBase64Encoded.toString());
        if(isBase58Encoded!=null) m.put("isBase58Encoded",isBase58Encoded.toString());
        if(isPEM!=null) m.put("isPEM",isPEM);
        if(isHex!=null) m.put("isHex",isHex);
        if(attributes!=null && attributes.size() > 0) {
            m.put("attributes", attributes);
        }
        if(signedAttributes!=null && signedAttributes.size() > 0) {
            m.put("signedAttributes", signedAttributes);
        }
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("alias")!=null) alias = (String)m.get("alias");
        if(m.get("fingerprint")!=null) fingerprint = (String)m.get("fingerprint");
        if(m.get("address")!=null) address = (String)m.get("address");
        if(m.get("type")!=null) type = (String)m.get("type");
        if(m.get("isEncryptionKey")!=null) isEncryptionKey = (Boolean)m.get("isEncryptionKey");
        if(m.get("isIdentityKey")!=null) isIdentityKey = (Boolean)m.get("isIdentityKey");
        if(m.get("isBase64Encoded")!=null) isBase64Encoded = Boolean.parseBoolean((String)m.get("isBase64Encoded"));
        if(m.get("isBase58Encoded")!=null) isBase58Encoded = Boolean.parseBoolean((String)m.get("isBase58Encoded"));
        if(m.get("isPEM")!=null) isPEM = (Boolean)m.get("isPEM");
        if(m.get("isHex")!=null) isHex = (Boolean)m.get("isHex");
        if(m.get("attributes")!=null) attributes = (Map<String,Object>)m.get("attributes");
        if(m.get("signedAttributes")!=null) signedAttributes = (Map<String,List<byte[]>>)m.get("signedAttributes");
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
