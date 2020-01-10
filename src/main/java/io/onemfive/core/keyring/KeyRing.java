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

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;

import java.io.IOException;
import java.util.Properties;

/**
 * Interface for implementing all KeyRings in 1M5.
 * Ensure they are thread safe a they are cached in {@link KeyRingService} on startup and shared across all incoming threads.
 *
 * @author objectorange
 */
public interface KeyRing {

    void init(Properties properties);

    void generateKeyRingCollections(GenerateKeyRingCollectionsRequest r) throws IOException, PGPException;

    PGPPublicKeyRingCollection getPublicKeyRingCollection(String location, String username, String passphrase) throws IOException, PGPException;

    PGPPublicKey getPublicKey(PGPPublicKeyRingCollection c, String keyAlias, boolean master) throws PGPException;

    void createKeyRings(String location, String keyRingUsername, String keyRingPassphrase, String alias, String aliasPassphrase, int hashStrength) throws IOException, PGPException;

    void encrypt(EncryptRequest r) throws IOException, PGPException;

    void decrypt(DecryptRequest r) throws IOException, PGPException;

    void sign(SignRequest r) throws IOException, PGPException;

    void verifySignature(VerifySignatureRequest r) throws IOException, PGPException;

}
