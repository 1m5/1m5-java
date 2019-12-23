package io.onemfive.data;

import io.onemfive.data.util.HashUtil;

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
