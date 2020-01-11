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

import java.math.BigInteger;

public class Base32 {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final BigInteger BASE = BigInteger.valueOf(32L);

    public Base32() {
    }

    public static String encode(byte[] input) {
        BigInteger bi = new BigInteger(1, input);

        StringBuffer s;
        BigInteger mod;
        for(s = new StringBuffer(); bi.compareTo(BASE) >= 0; bi = bi.subtract(mod).divide(BASE)) {
            mod = bi.mod(BASE);
            s.insert(0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".charAt(mod.intValue()));
        }

        s.insert(0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".charAt(bi.intValue()));
        byte[] var7 = input;
        int var4 = input.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            byte anInput = var7[var5];
            if(anInput != 0) {
                break;
            }

            s.insert(0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".charAt(0));
        }

        return s.toString();
    }

    public static byte[] decode(String input) {
        byte[] bytes = decodeToBigInteger(input).toByteArray();
        boolean stripSignByte = bytes.length > 1 && bytes[0] == 0 && bytes[1] < 0;
        int leadingZeros = 0;

        for(int tmp = 0; input.charAt(tmp) == "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".charAt(0); ++tmp) {
            ++leadingZeros;
        }

        byte[] var5 = new byte[bytes.length - (stripSignByte?1:0) + leadingZeros];
        System.arraycopy(bytes, stripSignByte?1:0, var5, leadingZeros, var5.length - leadingZeros);
        return var5;
    }

    public static BigInteger decodeToBigInteger(String input) {
        BigInteger bi = BigInteger.valueOf(0L);

        for(int i = input.length() - 1; i >= 0; --i) {
            int alphaIndex = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".indexOf(input.charAt(i));
            if(alphaIndex == -1) {
                throw new IllegalStateException("Illegal character " + input.charAt(i) + " at " + i);
            }

            bi = bi.add(BigInteger.valueOf((long)alphaIndex).multiply(BASE.pow(input.length() - 1 - i)));
        }

        return bi;
    }
}
