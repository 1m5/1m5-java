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
package onemfive.api;

import io.onemfive.data.AuthNRequest;
import io.onemfive.data.AuthenticateDIDRequest;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;
import io.onemfive.util.DLC;

import java.util.List;

public class DIDAPI {

    public static DID addIdentity(String username, String passphrase, String passphraseAgain, String location) {
        Envelope e = Envelope.documentFactory();
        // Remember to add routes backwards - execution of routes is performed using a stack.
        // Push Routes on the stack from last to first as routes are executed from the top.
        // 2. Authenticate/Save DID
        DID did = new DID();
        did.setUsername(username);
        did.setPassphrase(passphrase);
        did.setPassphrase2(passphraseAgain);
        AuthenticateDIDRequest adr = new AuthenticateDIDRequest();
        adr.did = did;
        adr.autogenerate = true;
        DLC.addData(AuthenticateDIDRequest.class, adr, e);
        DLC.addRoute(ServiceAPI.DID, "AUTHENTICATE",e);
        // 1. Load Public Key addresses for short and full addresses
        AuthNRequest ar = new AuthNRequest();
        ar.location = location;
        ar.keyRingUsername = did.getUsername();
        ar.keyRingPassphrase = did.getPassphrase();
        ar.alias = did.getUsername(); // use username as default alias
        ar.aliasPassphrase = did.getPassphrase(); // just use same passphrase
        ar.autoGenerate = true;
        DLC.addData(AuthNRequest.class, ar, e);
        DLC.addRoute(ServiceAPI.KEYRING, "AUTHN", e);
        return API.send(e).getDID();
    }

    public static boolean deleteIdentity(String fingerprint) {
        Envelope e = Envelope.documentFactory();
        DLC.addNVP("fingerprint", fingerprint, e);
        DLC.addRoute(ServiceAPI.DID, "DELETE", e);
        e = API.send(e);
        return "true".equals(DLC.getValue("delete-success",e));
    }

    public static DID authenticate(String username, String passphrase) {
        Envelope e = Envelope.documentFactory();
        // 2. Authenticate/Save DID
        DID did = new DID();
        did.setUsername(username);
        did.setPassphrase(passphrase);
        AuthenticateDIDRequest adr = new AuthenticateDIDRequest();
        adr.did = did;
        adr.autogenerate = false;
        DLC.addData(AuthenticateDIDRequest.class, adr, e);
        DLC.addRoute(ServiceAPI.DID, "AUTHENTICATE",e);
        // 1. Load Public Key addresses for short and full addresses
        AuthNRequest ar = new AuthNRequest();
        ar.keyRingUsername = did.getUsername();
        ar.keyRingPassphrase = did.getPassphrase();
        ar.alias = did.getUsername(); // use username as default alias
        ar.aliasPassphrase = did.getPassphrase(); // just use same passphrase
        ar.autoGenerate = false;
        DLC.addData(AuthNRequest.class, ar, e);
        DLC.addRoute(ServiceAPI.KEYRING, "AUTHN", e);
        return API.send(e).getDID();
    }

    public static List<DID> getIdentities() {
        Envelope e = Envelope.documentFactory();
        DLC.addRoute(ServiceAPI.DID, "GET_IDENTITIES", e);
        e = API.send(e);
        return (List<DID>)DLC.getValue("identities",e);
    }

    public static DID addContact(String username, String fingerprint, String address, String description) {
        Envelope e = Envelope.documentFactory();
        DID contact = new DID();
        contact.setUsername(username);
        contact.getPublicKey().setFingerprint(fingerprint);
        contact.getPublicKey().setAddress(address);
        contact.setDescription(description);
        DLC.addNVP("contact", contact, e);
        DLC.addRoute(ServiceAPI.DID, "ADD_CONTACT", e);
        e = API.send(e);
        return (DID)DLC.getValue("contact",e);
    }

    public static DID getContact(String fingerprint) {
        Envelope e = Envelope.documentFactory();
        DLC.addNVP("fingerprint", fingerprint, e);
        DLC.addRoute(ServiceAPI.DID, "GET_CONTACT",e);
        e = API.send(e);
        return (DID)DLC.getValue("contact",e);
    }

    public static boolean deleteContact(String fingerprint) {
        Envelope e = Envelope.documentFactory();
        DLC.addNVP("fingerprint", fingerprint, e);
        DLC.addRoute(ServiceAPI.DID, "GET_CONTACT",e);
        e = API.send(e);
        return true;
    }

    public static List<DID> getContacts() {
        Envelope e = Envelope.documentFactory();
        DLC.addRoute(ServiceAPI.DID, "GET_CONTACTS", e);
        e = API.send(e);
        return (List<DID>)DLC.getValue("contacts", e);
    }

}
