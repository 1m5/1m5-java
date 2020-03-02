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

import java.util.Map;

public class AuthenticateDIDRequest extends ServiceMessage {
    public static final int DID_REQUIRED = 1;
    public static final int DID_FINGERPRINT_REQUIRED = 2;
    public static final int DID_PASSPHRASE_REQUIRED = 3;
    public static final int DID_USERNAME_UNKNOWN = 4;
    public static final int DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN = 5;
    public static final int DID_PASSPHRASE_HASH_ALGORITHM_MISMATCH = 6;
    public static final int DID_PASSPHRASE_MISMATCH = 7;
    public static final int DID_TOKEN_FORMAT_MISMATCH = 8;

    public boolean autogenerate = false;
    public DID did;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = super.toMap();
        if(did!=null) m.put("did",did.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m.get("did")!=null) {
            did = new DID();
            did.fromMap((Map<String, Object>)m.get("did"));
        }
    }
}
