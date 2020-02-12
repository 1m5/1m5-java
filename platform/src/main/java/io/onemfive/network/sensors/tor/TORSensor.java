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
package io.onemfive.network.sensors.tor;

import io.onemfive.data.*;
import io.onemfive.network.*;
import io.onemfive.network.sensors.*;
import io.onemfive.network.sensors.i2p.I2PSensorSession;
import io.onemfive.network.sensors.tor.embedded.TOREmbedded;
import io.onemfive.network.sensors.tor.external.TORExternal;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Sets up an HttpClientSensor with the local Tor instance as a proxy (127.0.0.1:9150).
 *
 * TODO: Add local node as Tor Hidden Service accepting external Tor calls
 *
 * @author objectorange
 */
public final class TORSensor extends BaseSensor {

    private static final Logger LOG = Logger.getLogger(TORSensor.class.getName());

    private NetworkPeerDiscovery discovery;

    private TOR tor;

    private File sensorDir;
    protected final Map<String, SensorSession> sessions = new HashMap<>();

    public TORSensor() {
        super(Network.TOR);
    }

    public TORSensor(SensorManager sensorManager) {
        super(sensorManager, Network.TOR);
    }

    public String[] getOperationEndsWith() {
        return new String[]{".onion"};
    }

    @Override
    public String[] getURLBeginsWith() {
        return new String[]{"tor"};
    }

    @Override
    public String[] getURLEndsWith() {
        return new String[]{".onion"};
    }

    @Override
    public boolean sendOut(NetworkPacket packet) {
        LOG.info("Tor Sensor sending request...");
        boolean successful = tor.sendOut(packet);
        if (successful) {
            LOG.info("Tor Sensor successful response received.");
            if (!getStatus().equals(SensorStatus.NETWORK_CONNECTED)) {
                LOG.info("Tor Network status changed back to CONNECTED.");
                updateStatus(SensorStatus.NETWORK_CONNECTED);
            }
        }
        return successful;
    }

    @Override
    public SensorSession establishSession(String address, Boolean autoConnect) {
        if(sessions.get("default")==null) {
            SensorSession sensorSession = new TORSensorSession(this);
            sensorSession.init(properties);
            sensorSession.open(null);
            if (autoConnect) {
                sensorSession.connect();
            }
            sessions.put("default", sensorSession);
        }
        return sessions.get("default");
    }

    @Override
    public boolean sendIn(Envelope envelope) {
        // TODO: Implement as Tor Hidden Service
        return super.sendIn(envelope);
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting TOR Sensor...");
        updateStatus(SensorStatus.STARTING);
        String sensorsDirStr = properties.getProperty("1m5.dir.sensors");
        if(sensorsDirStr==null) {
            LOG.warning("1m5.dir.sensors property is null. Please set prior to instantiating Tor Sensor.");
            return false;
        }
        try {
            sensorDir = new File(new File(sensorsDirStr),"tor");
            if(!sensorDir.exists() && !sensorDir.mkdir()) {
                LOG.warning("Unable to create Tor sensor directory.");
                return false;
            } else {
                properties.put("1m5.dir.sensors.tor",sensorDir.getCanonicalPath());
            }
        } catch (IOException e) {
            LOG.warning("IOException caught while building Tor sensor directory: \n"+e.getLocalizedMessage());
            return false;
        }

        if("true".equals(properties.getProperty("settings.network.tor.routerEmbedded")))
            tor = new TOREmbedded(this, networkState, sensorManager);
        else
            tor = new TORExternal(this, networkState, sensorManager);

        if(tor.start(properties)) {
            updateStatus(SensorStatus.NETWORK_CONNECTED);
        } else {
            return false;
        }

            // Setup Discovery
//            discovery = new NetworkPeerDiscovery(taskRunner, this, Network.TOR, config);
//            NetworkPeer seedA1M5 = new NetworkPeer();
//            seedA1M5.setId("+sKVViuz2FPsl/XQ+Da/ivbNfOI=");
//            seedA1M5.getDid().getPublicKey().setAddress("mQENBF43FaEDCACtMtZJu3oSchRgtaUzTmMJRbJmdfSpEaG2nW7U2YinHeMUkIpFCQGu2/OgmCuE4kVEQ4y6kKvqCiMvahtv+OqID0Lk7JEofFpwH8UUUis+p99qnw7RYy1q4IrjBpFSZHLi/nCyZOp4L7jG0CgJEFoZZEd2Uby1vnmePxts7srWkBjlmUWj+e/G89r+ZYpRN7dwdwl69Qk2s3UWTq1xyVyMqg/RuFC9kUgsmkL8vIpO4KYX7DfRKmYT29gfwjrvbVd18oeFECFVU/E6118N4P/8zIj0vhOiuar5hdKiq3oU5ka1hlQqP3IrQz2+feh2Q34+TP/BBEKOvbSv6V/6/6T/ABEBAAG0BUFsaWNliQEuBBMDAgAYBQJeNxWkAhsDBAsJCAcGFQgCCQoLAh4BAAoJEPg2v4r2zXzihH8H/iKc0ZBoWbeP/FykApYjG9m8ze54Pr9noRUw7JDAs6a7Y4IjNuE42NLMMwcxCoekzVmUwMyLrQDW+pLMaZupX2i8yU720F9WMh4f9eC4lXg64IMTnNUZqI4U52wZV22nxiGdGqacHwSSRcG5rHBskdrOJ8BX0QQ7Qt+iw4xyaxMPSPnULiJv3Z+kwLVLbxMQsmtLy7BZW6Pn848oONRNodg9tWn3PA/jTFg4ak+9lzfc1HnAWe/FeQ7O6jZ3h5eAbC4Y9KQqxVI7QzOkwIpRHMbkrVHdEcZMOa36wznC6SCXxpB/uGNrVnCJ0og9RN701QbxOu0XcevMjAOcE5dsC3g=");
//            seedA1M5.getDid().getPublicKey().setFingerprint(seedA1M5.getId());
//            seedA1M5.getDid().getPublicKey().setType("RSA2048");
//            seedA1M5.getDid().getPublicKey().isIdentityKey(true);
//            seedA1M5.getDid().getPublicKey().setBase64Encoded(true);
//
//            NetworkPeer seedATOR = new NetworkPeer(Network.TOR);
//            seedATOR.setId("+sKVViuz2FPsl/XQ+Da/ivbNfOI=");
//            seedATOR.getDid().getPublicKey().setAddress("localonionaddresshere.onion");
//            seedATOR.getDid().getPublicKey().setFingerprint("onionaddresshashhere");
//            seedATOR.getDid().getPublicKey().setType("addressgenerationalgorithmhere");
//            seedATOR.getDid().getPublicKey().isIdentityKey(true);
//            seedATOR.getDid().getPublicKey().setBase64Encoded(true);
//            if(sensorManager.getPeerManager().savePeer(seedATOR, true)) {
//                discovery.seeds.add(seedATOR);
//            }
//            taskRunner.addTask(discovery);
            return true;
    }

    @Override
    public boolean pause() {
        return tor.pause();
    }

    @Override
    public boolean unpause() {
        return tor.unpause();
    }

    @Override
    public boolean restart() {
        return tor.restart();
    }

    @Override
    public boolean shutdown() {
        return tor.shutdown();
    }

    @Override
    public boolean gracefulShutdown() {
        return tor.gracefulShutdown();
    }
}
