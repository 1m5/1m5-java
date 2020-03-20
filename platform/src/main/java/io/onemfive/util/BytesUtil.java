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
package io.onemfive.util;

public class BytesUtil {

    public static int packBigEndian(byte[] b) {
        return (b[0] & 0xFF) << 24
                | (b[1] & 0xFF) << 16
                | (b[2] & 0xFF) <<  8
                | (b[3] & 0xFF);
    }

    public static byte[] unpackBigEndian(int x) {
        return new byte[] {
                (byte)(x >>> 24),
                (byte)(x >>> 16),
                (byte)(x >>>  8),
                (byte)(x)
        };
    }

    public static int packLittleEndian(byte[] b) {
        return (b[0] & 0xFF)
                | (b[1] & 0xFF) <<  8
                | (b[2] & 0xFF) << 16
                | (b[3] & 0xFF) << 24;
    }

    public static byte[] unpackLittleEndian(int x) {
        return new byte[]{
                (byte) (x),
                (byte) (x >>> 8),
                (byte) (x >>> 16),
                (byte) (x >>> 24)
        };
    }
}
