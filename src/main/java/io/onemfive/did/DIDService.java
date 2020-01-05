package io.onemfive.did;

import io.onemfive.core.*;
import io.onemfive.core.keyring.AuthNRequest;
import io.onemfive.core.keyring.GenerateKeyRingCollectionsRequest;
import io.onemfive.data.DID;
import io.onemfive.data.Hash;
import io.onemfive.data.TextMessage;
import io.onemfive.data.route.Route;
import io.onemfive.data.util.JSONParser;
import io.onemfive.data.Envelope;
import io.onemfive.data.util.DLC;
import io.onemfive.data.util.HashUtil;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static io.onemfive.did.HashRequest.UNKNOWN_HASH_ALGORITHM;

/**
 * Decentralized IDentifier (DID) Service
 *
 * Implementation of W3C Spec {@link "https://w3c-ccg.github.io/did-spec/} ongoing.
 *
 * @author objectorange
 */
public class DIDService extends BaseService {

    private static final Logger LOG = Logger.getLogger(DIDService.class.getName());

    public static final String OPERATION_GET_LOCAL_DID = "GET_LOCAL_DID"; // Read Local
    public static final String OPERATION_VERIFY = "VERIFY"; // Read/Verify
    public static final String OPERATION_SAVE = "SAVE"; // Create/Update
    public static final String OPERATION_REVOKE = "REVOKE"; // Deactivate

    public static final String OPERATION_AUTHENTICATE = "AUTHENTICATE";
    public static final String OPERATION_AUTHENTICATE_CREATE = "AUTHENTICATE_CREATE";

    public static final String OPERATION_HASH = "HASH";
    public static final String OPERATION_VERIFY_HASH = "VERIFY_HASH";

    public static final String OPERATION_ADD_CONTACT = "ADD_CONTACT";
    public static final String OPERATION_GET_CONTACT = "GET_CONTACT";
    public static final String OPERATION_GET_CONTACTS = "GET_CONTACTS";
    public static final String OPERATION_DELETE_CONTACT = "DELETE_CONTACT";

    private static final Pattern layout = Pattern.compile("\\$31\\$(\\d\\d?)\\$(.{43})");

    private static SecureRandom random = new SecureRandom();

    private DID nodeDID;
    private Map<String,DID> localUserDIDs = new HashMap<>();

    public DIDService() {}

    public DIDService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        handleAll(e);
    }

    @Override
    public void handleEvent(Envelope e) {
        handleAll(e);
    }

    @Override
    public void handleHeaders(Envelope e) {
        handleAll(e);
    }

    private void handleAll(Envelope e) {
        Route route = e.getRoute();
        String operation = route.getOperation();
        switch(operation) {
            case OPERATION_GET_LOCAL_DID: {
                LOG.info("Received get DID request.");
                GetLocalDIDRequest r = (GetLocalDIDRequest)DLC.getData(GetLocalDIDRequest.class,e);
                if(r == null) {
                    r = new GetLocalDIDRequest();
                    r.statusCode = GetLocalDIDRequest.REQUEST_REQUIRED;
                    DLC.addData(GetLocalDIDRequest.class,r,e);
                    break;
                }
                if(r.did == null) {
                    r.statusCode = GetLocalDIDRequest.DID_REQUIRED;
                    break;
                }
                if(r.did.getUsername() == null) {
                    r.statusCode = GetLocalDIDRequest.DID_USERNAME_REQUIRED;
                    break;
                }
                r.did = getLocalDID(r);
                break;
            }
            case OPERATION_ADD_CONTACT: {
                addContact(e);
                break;
            }
            case OPERATION_GET_CONTACT: {
                getContact(e);
                break;
            }
            case OPERATION_GET_CONTACTS: {
                getContacts(e);
                break;
            }
            case OPERATION_DELETE_CONTACT: {
                deleteContact(e);
                break;
            }
            case OPERATION_VERIFY: {
                e.setDID(verify(e.getDID()));
                break;
            }
            case OPERATION_AUTHENTICATE: {
                LOG.info("Received authn DID request.");
                AuthenticateDIDRequest r = (AuthenticateDIDRequest)DLC.getData(AuthenticateDIDRequest.class,e);
                if(r == null) {
                    LOG.warning("Request required.");
                    r = new AuthenticateDIDRequest();
                    r.statusCode = AuthenticateDIDRequest.REQUEST_REQUIRED;
                    DLC.addData(AuthenticateDIDRequest.class,r,e);
                    break;
                }
                if(r.did == null) {
                    LOG.warning("DID required.");
                    r.statusCode = AuthenticateDIDRequest.DID_REQUIRED;
                    break;
                }
                if(r.did.getUsername() == null) {
                    LOG.info("Username required.");
                    r.statusCode = AuthenticateDIDRequest.DID_USERNAME_REQUIRED;
                    break;
                }
                if(r.did.getPassphrase() == null) {
                    LOG.info("Passphrase required.");
                    r.statusCode = AuthenticateDIDRequest.DID_PASSPHRASE_REQUIRED;
                    break;
                }
                AuthNRequest ar = (AuthNRequest)DLC.getData(AuthNRequest.class,e);
                GenerateKeyRingCollectionsRequest gkr = (GenerateKeyRingCollectionsRequest) DLC.getData(GenerateKeyRingCollectionsRequest.class,e);
                if(ar!=null && ar.identityPublicKey!=null)
                    r.did.addPublicKey(ar.identityPublicKey);
                else if(gkr!=null && gkr.identityPublicKey!=null)
                    r.did.addPublicKey(gkr.identityPublicKey);
                authenticate(r);
                if(r.did.getAuthenticated()) {
                    LOG.info("DID Authenticated, setting DID in header.");
                    e.setDID(r.did);
                    if(nodeDID==null || nodeDID.equals(r.did)) {
                        // first authentication is the node itself
                        LOG.info("First authn is node or this is node authn - caching.");
                        nodeDID = r.did;
                    } else if(!nodeDID.equals(r.did))
                        LOG.info("Local user DID cached.");
                        localUserDIDs.put(r.did.getUsername(),r.did);
                    break;
                } else if(r.statusCode == AuthenticateDIDRequest.DID_USERNAME_UNKNOWN && r.autogenerate) {
                    LOG.info("Username unknown and autogenerate is true so save DID as authenticated...");
                    r.did.setAuthenticated(true); // true because we're going to create it
                    save(r.did, r.autogenerate);
                    if(nodeDID==null || nodeDID.equals(r.did)) {
                        // first authentication is the node itself
                        LOG.info("First authn is node or this is node authn - caching.");
                        nodeDID = r.did;
                    } else
                        LOG.info("Local user DID cached.");
                        localUserDIDs.put(r.did.getUsername(),r.did);
                    break;
                }
                break;
            }
            case OPERATION_SAVE: {
                LOG.info("Received save DID request.");
                DID did = (DID)DLC.getData(DID.class,e);
                if(did!=null) {
                    e.setDID(save(did, true));
                }
                break;
            }
            case OPERATION_AUTHENTICATE_CREATE: {
                AuthenticateDIDRequest r = (AuthenticateDIDRequest)DLC.getData(AuthenticateDIDRequest.class,e);
                authenticateOrCreate(r);
                break;
            }
            case OPERATION_REVOKE: {
                LOG.warning("REVOKE not implemented.");
                break;
            }
            case OPERATION_HASH: {
                HashRequest r = (HashRequest)DLC.getData(HashRequest.class,e);
                try {
                    if(r.generateHash)
                        r.hash = HashUtil.generateHash(r.contentToHash, Hash.Algorithm.SHA256);
                    if(r.generateFingerprint && r.hash != null) {
                        r.fingerprint = HashUtil.generateHash(r.hash.getHash(), Hash.Algorithm.SHA1);
                    }
                } catch (NoSuchAlgorithmException e1) {
                    r.statusCode = UNKNOWN_HASH_ALGORITHM;
                }
                break;
            }
            case OPERATION_VERIFY_HASH:{
                VerifyHashRequest r = (VerifyHashRequest)DLC.getData(VerifyHashRequest.class,e);
                try {
                    r.isAMatch = HashUtil.verifyHash(r.content, r.hashToVerify);
                } catch (NoSuchAlgorithmException e1) {
                    r.statusCode = UNKNOWN_HASH_ALGORITHM;
                }
                break;
            }
            default: deadLetter(e); // Operation not supported
        }
    }

    private DID getLocalDID(GetLocalDIDRequest r) {
        if(nodeDID!=null)
            return nodeDID;
        if(localUserDIDs.containsKey(r.did.getUsername()))
            return localUserDIDs.get(r.did.getUsername());
        if(r.did.getPassphrase() == null) {
            r.statusCode = GetLocalDIDRequest.DID_PASSPHRASE_REQUIRED;
            return r.did;
        }
        if(r.did.getPassphraseHashAlgorithm() == null) {
            r.statusCode = GetLocalDIDRequest.DID_PASSPHRASE_HASH_ALGORITHM_UNKNOWN;
            return r.did;
        }
        return save(r.did, true);
    }

    private void addContact(Envelope e) {
        LOG.info("Received add Contact request.");
    }

    private void getContact(Envelope e) {
        LOG.info("Received get Contact request.");
        String alias = ((TextMessage)e.getMessage()).getText();
        DID loadedDID = loadDID(alias);

    }

    private void getContacts(Envelope e) {
        LOG.info("Received get Contacts request.");

    }

    private void deleteContact(Envelope e) {
        LOG.info("Received delete Contact request.");
    }

    private DID verify(DID did) {
        LOG.info("Received verify DID request.");
        DID didLoaded = loadDID(did.getUsername());
        if(didLoaded != null && did.getUsername() != null && did.getUsername().equals(didLoaded.getUsername())) {
            didLoaded.setVerified(true);
            LOG.info("DID verification successful.");
            return didLoaded;
        } else {
            did.setVerified(false);
            LOG.info("DID verification unsuccessful.");
            return did;
        }
    }

    /**
     * Saves and returns DID generating passphrase hash if none exists.
     * @param did DID
     */
    private DID save(DID did, boolean autoCreate) {
        LOG.info("Saving DID...");
        if(did.getPassphraseHash() == null) {
            LOG.info("Hashing passphrase...");
            try {
                did.setPassphraseHash(HashUtil.generatePasswordHash(did.getPassphrase()));
                // ensure passphrase is cleared
                did.setPassphrase(null);
            } catch (NoSuchAlgorithmException ex) {
                LOG.warning("Hashing Algorithm not supported while saving DID\n" + ex.getLocalizedMessage());
                return did;
            }
        }
        try {
            infoVaultDB.save(
                    DID.class.getName(),
                    did.getUsername(),
                    JSONParser.toString(did.toMap()).getBytes(),
                    autoCreate);
        } catch (FileNotFoundException e) {
            LOG.warning(e.getLocalizedMessage());
        }
        LOG.info("DID saved.");
        return did;
    }

    /**
     * Authenticates passphrase
     * @param r AuthenticateDIDRequest
     */
    private void authenticate(AuthenticateDIDRequest r) {
        DID loadedDID = loadDID(r.did.getUsername());
        if(loadedDID.getPassphraseHash()==null) {
            if(r.autogenerate) {
                r.did.setVerified(true);
                r.did.setAuthenticated(true);
                loadedDID = save(r.did, true);
                LOG.info("Saved DID: " + loadedDID);
            } else {
                LOG.warning("Unable to load DID and autogenerate=false. Authentication failed.");
                r.statusCode = AuthenticateDIDRequest.DID_USERNAME_UNKNOWN;
                return;
            }
        } else {
            LOG.info("Loaded DID: "+loadedDID);
            Boolean authN = null;
            LOG.info("Verifying password hash...");
            try {
                authN = HashUtil.verifyPasswordHash(r.did.getPassphrase(), loadedDID.getPassphraseHash());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                LOG.warning(e.getLocalizedMessage());
            }
            LOG.info("AuthN: "+(authN != null && authN));
            r.did.setAuthenticated(authN != null && authN);
        }
        if(r.did.getAuthenticated()) {
            r.did = loadedDID;
            localUserDIDs.put(r.did.getUsername(),r.did);
        }
    }

    private void authenticateOrCreate(AuthenticateDIDRequest r) {
        r.did = verify(r.did);
        if(!r.did.getVerified()) {
            save(r.did, true);
        } else {
            authenticate(r);
        }
    }

    private boolean isNew(String alias) {
        DID loadedDID = loadDID(alias);
        return loadedDID == null || loadedDID.getUsername() == null || loadedDID.getUsername().isEmpty();
    }

    private DID loadDID(String alias) {
        DID loadedDID = new DID();
        byte[] content;
        try {
            content = infoVaultDB.load(DID.class.getName(), alias);
        } catch (FileNotFoundException e) {
            return null;
        }
        String jsonBody = new String(content);
        LOG.info("JSON loaded: "+jsonBody);
        loadedDID.fromMap((Map<String,Object>) JSONParser.parse(jsonBody));
        LOG.info("DID Loaded from map.");
        return loadedDID;
    }

    @Override
    public boolean start(Properties properties) {
        super.start(properties);
        LOG.info("Starting....");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down....");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }

//    public static void main(String[] args) {
//        DIDService service = new DIDService();
//        DID did = new DID();
//        did.setAlias("Alice");
//        did.setPassphrase("1234");
//        service.create(did);
//    }

}
