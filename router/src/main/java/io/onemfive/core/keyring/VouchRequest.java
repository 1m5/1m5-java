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
package io.onemfive.core.keyring;

import io.onemfive.data.ServiceMessage;
import io.onemfive.data.DID;

import java.util.Map;

public class VouchRequest extends ServiceMessage {

    public static final int SIGNER_REQUIRED = 1;
    public static final int SIGNEE_REQUIRED = 2;
    public static final int ATTRIBUTES_REQUIRED = 3;

    public DID signer;
    public DID signee;
    public Map<String,Object> attributesToSign;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = super.toMap();
        if(signer!=null) m.put("signer",signer.toMap());
        if(signee!=null) m.put("signee",signee.toMap());
        if(attributesToSign!=null) m.put("attributesToSign", attributesToSign);
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m.get("signer")!=null) {
            signer = new DID();
            signer.fromMap((Map<String, Object>)m.get("signer"));
        }
        if(m.get("signee")!=null) {
            signee = new DID();
            signee.fromMap((Map<String, Object>)m.get("signee"));
        }
        if(m.get("attributesToSign")!=null) {
            attributesToSign = (Map<String, Object>)m.get("attributesToSign");
        }
    }

}
