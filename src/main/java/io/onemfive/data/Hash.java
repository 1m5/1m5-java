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

import io.onemfive.util.HashUtil;

/**
 * A hash with its algorithm.
 *
 * @author objectorange
 */
public class Hash extends Data {

    public enum Algorithm {

        SHA1("SHA-1"),
        SHA256("SHA-256"),
        SHA512("SHA-512"),
        PBKDF2WithHmacSHA1("PBKDF2WithHmacSHA1");

        private String name;

        Algorithm(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Algorithm value(String name) {
            switch(name){
                case "SHA-1": return SHA1;
                case "SHA-256": return SHA256;
                case "SHA-512": return SHA512;
                case "PBKDF2WithHmacSHA1": return PBKDF2WithHmacSHA1;
                default: return null;
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private String hash;
    private Algorithm algorithm;

    public Hash(String hash, Algorithm algorithm) {
        this.hash = hash;
        this.algorithm = algorithm;
    }

    public String getHash() {
        return hash;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public String toHex() {
        if(hash != null)
            return HashUtil.toHex(hash.getBytes());
        else
            return null;
    }

    public byte[] fromHex(String hex) {
        return HashUtil.fromHex(hex);
    }

    @Override
    public boolean equals(Object obj) {
        return hash != null && obj instanceof Hash && hash.equals(((Hash)obj).getHash());
    }

    @Override
    public int hashCode() {
        if(hash==null)
            return super.hashCode();
        else
            return hash.hashCode();
    }

    @Override
    public String toString() {
        return hash;
    }
}
