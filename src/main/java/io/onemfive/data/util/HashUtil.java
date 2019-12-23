package io.onemfive.data.util;

import io.onemfive.data.DID;
import io.onemfive.data.Hash;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Logger;

public class HashUtil {

    private static Logger LOG = Logger.getLogger(HashUtil.class.getName());

    private static String DEL = "_";

    public static Hash generateFingerprint(byte[] contentToFingerprint, Hash.Algorithm algorithm) throws NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance(algorithm.getName());
            byte[] hash = md.digest(contentToFingerprint);
            return new Hash(toHex(hash), algorithm);
    }

    public static Hash generateHash(String contentToHash, Hash.Algorithm algorithm) throws NoSuchAlgorithmException {
        if(algorithm == Hash.Algorithm.PBKDF2WithHmacSHA1)
            return generatePasswordHash(contentToHash);
        else
            return generateHash(getSalt(), contentToHash.getBytes(), algorithm);
    }
    /**
     * Generate Hash using supplied bytes and specified Algorithm
     * @param contentToHash
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static Hash generateHash(byte[] contentToHash, Hash.Algorithm algorithm) throws NoSuchAlgorithmException {
        if(algorithm == Hash.Algorithm.PBKDF2WithHmacSHA1)
            return generatePasswordHash(new String(contentToHash));
        else
            return generateHash(getSalt(), contentToHash, algorithm);
    }

    private static Hash generateHash(byte[] salt, byte[] contentToHash, Hash.Algorithm algorithm) throws NoSuchAlgorithmException {
        if(algorithm == Hash.Algorithm.PBKDF2WithHmacSHA1)
            return generatePasswordHash(salt, new String(contentToHash));
        else {
            MessageDigest md = MessageDigest.getInstance(algorithm.getName());
            md.update(salt);
            byte[] hash = md.digest(contentToHash);
            return new Hash(Base64.encode(hash) + DEL + Base64.encode(salt), algorithm);
        }
    }

    public static Boolean verifyHash(String contentToVerify, Hash hashToVerify) throws NoSuchAlgorithmException {
        if(hashToVerify.getAlgorithm() == Hash.Algorithm.PBKDF2WithHmacSHA1)
            return verifyPasswordHash(contentToVerify, hashToVerify);
        else {
            String hashString = hashToVerify.getHash();
            String[] parts = hashString.split(DEL);
            byte[] hash = Base64.decode(parts[0]);
            byte[] salt = Base64.decode(parts[1]);
            Hash contentHash = generateHash(salt, contentToVerify.getBytes(), hashToVerify.getAlgorithm());
            String hashStringToVerify = contentHash.getHash();
            String[] partsToVerify = hashStringToVerify.split(DEL);
            byte[] hashToVerifyBytes = Base64.decode(partsToVerify[0]);
            return Arrays.equals(hash, hashToVerifyBytes);
        }
    }

    public static Hash generatePasswordHash(String passwordToHash) throws NoSuchAlgorithmException {
        return generatePasswordHash(getSalt(), passwordToHash);
    }

    public static Hash generatePasswordHash(byte[] salt, String passwordToHash) throws NoSuchAlgorithmException {
        int iterations = 1000;
        byte[] hash;
        try {
            PBEKeySpec spec = new PBEKeySpec(passwordToHash.toCharArray(), salt, iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(Hash.Algorithm.PBKDF2WithHmacSHA1.getName());
            hash = skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
//        String hashString = iterations + DEL + toHex(salt) + DEL + toHex(hash);
        String hashString = iterations + DEL + Base64.encode(salt) + DEL + Base64.encode(hash);
        return new Hash(hashString,Hash.Algorithm.PBKDF2WithHmacSHA1);
    }

    public static Boolean verifyPasswordHash(String contentToVerify, Hash hashToVerify) throws NoSuchAlgorithmException {
        if(hashToVerify.getAlgorithm() != Hash.Algorithm.PBKDF2WithHmacSHA1)
            throw new NoSuchAlgorithmException();
//        String hashString = Base64.decodeToString(hashToVerify.getHash());
        String hashString = hashToVerify.getHash();
        String[] parts = hashString.split(DEL);
        int iterations = Integer.parseInt(parts[0]);
//        byte[] salt = fromHex(parts[1]);
        byte[] salt = Base64.decode(parts[1]);
//        byte[] hash = fromHex(parts[2]);
        byte[] hash = Base64.decode(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(contentToVerify.toCharArray(), salt, iterations, 64 * 8);
        byte[] testHash;
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(Hash.Algorithm.PBKDF2WithHmacSHA1.getName());
            testHash = skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {

            return null;
        }

        int diff = hash.length ^ testHash.length;
        for(int i = 0; i < hash.length && i < testHash.length; i++)
        {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }

    public static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
            hex = String.format("%0"  +paddingLength + "d", 0) + hex;
        hex = hex.toUpperCase();
        StringBuilder sb = new StringBuilder();
        char[] ch = hex.toCharArray();
        int i = 1;
        for(char c : ch) {
            sb.append(c);
            if((i++%4)==0 && i<ch.length) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    public static byte[] fromHex(String hex)
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    public static void main(String[] args) {
        DID did = new DID();
        did.setUsername("Alice");
        did.setPassphrase("1234");
        try {
            Hash passwordHash = HashUtil.generatePasswordHash(did.getPassphrase());
            System.out.println("Alias Password Hash: "+passwordHash.getHash());
            Boolean aliasVerified = HashUtil.verifyPasswordHash("1234", passwordHash);
            System.out.println("Alias Password Hash Verified: "+aliasVerified);
            Hash aliasHash = HashUtil.generateHash(did.getUsername(), Hash.Algorithm.SHA1);
            System.out.println("Alias Hash: "+aliasHash.getHash());
            Boolean shortHashVerified = HashUtil.verifyHash(did.getUsername(), aliasHash);
            System.out.println("Alias Hash Verified: "+shortHashVerified);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
