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

import io.onemfive.data.KeyRingsRequest;

public class SignRequest extends KeyRingsRequest {
    public static int CONTENT_TO_SIGN_REQUIRED = 2;
    public static int ALIAS_REQUIRED = 3;
    public static int PASSPHRASE_REQUIRED = 4;
    public static int SECRET_KEY_NOT_FOUND = 5;
    public static int LOCATION_REQUIRED = 6;
    public static int LOCATION_INACCESSIBLE = 7;

    public String location;
    public String keyRingUsername;
    public String keyRingPassphrase;
    public byte[] contentToSign;
    public String alias;
    public String passphrase;
    // Respones
    public byte[] signature;
}
