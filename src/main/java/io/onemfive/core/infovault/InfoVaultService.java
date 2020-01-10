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
package io.onemfive.core.infovault;

import io.onemfive.core.*;
import io.onemfive.data.*;
import io.onemfive.data.route.Route;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Asynchronous access to persistence.
 * Access to an instance of LocalFSInfoVaultDB (InfoVaultDB) is provided in each Service too (by BaseService)
 * for synchronous access.
 * Developer's choice to which to use on a per-case basis by Services extending BaseService.
 * Clients always use this service as they do not have direct access to InfoVaultDB.
 * Consider using this service for heavier higher-latency work by Services extending BaseService vs using their
 * synchronous access instance in BaseService.
 *
 * InfoVaultDB:
 * Maintain thread-safe.
 * Use directly synchronously.
 * InfoVaultDB instances are singleton by type when instantiated through InfoVaultService.getInstance(String className).
 * Multiple types can be instantiated in parallel, e.g. LocalFSInfoVaultDB and Neo4jDB
 * Pass in class name (including package) to get an instance of it.
 * Make sure your class implements the InfoVaultDB interface.
 * Current implementations:
 *      io.onemfive.core.infovault.LocalFSInfoVaultDB (default)
 *      io.onemfive.infovault.neo4j.Neo4jDB
 *
 * @author objectorange
 */
public class InfoVaultService extends BaseService {

    private static final Logger LOG = Logger.getLogger(InfoVaultService.class.getName());

    protected static Map<String,InfoVaultDB> instances = new HashMap<>();
    private static final Object lock = new Object();

    public static final String OPERATION_EXECUTE = "EXECUTE";

    public InfoVaultService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route r = e.getRoute();
        switch(r.getOperation()) {
            case OPERATION_EXECUTE: {
                try {
                    execute(e);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                break;
            }
            default: deadLetter(e);
        }
    }

    private void execute(Envelope e) throws Exception {
        LOG.warning("Not yet implemented.");
    }

    public static InfoVaultDB getInfoVaultDBInstance(String infoVaultDBClass)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        InfoVaultDB instance = instances.get(infoVaultDBClass);
        if(instance == null) {
            synchronized (lock) {
                instance = (InfoVaultDB)Class.forName(infoVaultDBClass).getConstructor().newInstance();
                instances.put(infoVaultDBClass,instance);
            }
        }
        return instance;
    }

    @Override
    public boolean start(Properties properties) {
        super.start(properties);
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown.");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }

}
