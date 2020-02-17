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
package io.onemfive.core;

import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.bus.ServiceBus;
import io.onemfive.core.infovault.InfoVaultDB;
import io.onemfive.data.ManCon;
import io.onemfive.data.ManConStatus;
import io.onemfive.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Global router context.
 */
public class OneMFiveAppContext {

    private static final Logger LOG = Logger.getLogger(OneMFiveAppContext.class.getName());

    /** the context that components without explicit root are bound */
    protected static OneMFiveAppContext globalAppContext;
    // TODO: Move configurations to its own class
//    protected final OneMFiveConfig config;

    public static final Properties config = new Properties();

    private ServiceBus serviceBus;

    protected Set<Runnable> shutdownTasks;
    private File baseDir;
    private File configDir;
    private File libDir;
    private File pidDir;
    private File logDir;
    private File dataDir;
    private File cacheDir;
    private volatile File tmpDir;
    private File servicesDir;
    private final Random tmpDirRand = new Random();
    private static ClientAppManager clientAppManager;
    private final static Object lockA = new Object();
    private boolean initialize = false;
    private boolean configured = false;
    private static Locale locale;
    private static String version = null;
    // split up big lock on this to avoid deadlocks
    private final Object lock1 = new Object(), lock2 = new Object(), lock3 = new Object(), lock4 = new Object();

    /**
     * Pull the default context, creating a new one if necessary, else using
     * the first one created.
     *
     * Warning - do not save the returned value, or the value of any methods below,
     * in a static field, or you will get the old context if a new instance is
     * started in the same JVM after the first is shut down,
     * e.g. on Android.
     */
    public static synchronized OneMFiveAppContext getInstance() {
        return getInstance(null);
    }

    public static OneMFiveAppContext getInstance(Properties properties) {
        synchronized (lockA) {
            if (globalAppContext == null) {
                globalAppContext = new OneMFiveAppContext(false, properties);
                LOG.info("Created and returning new instance: " + globalAppContext);
            } else {
                LOG.info("Returning cached instance: " + globalAppContext);
            }
        }
        if(!globalAppContext.configured) {
            globalAppContext.configure();
        }
        return globalAppContext;
    }

    public static void clearGlobalContext() {
        globalAppContext = null;
    }

    /**
     * Create a new context.
     *
     * @param doInit should this context be used as the global one (if necessary)?
     *               Will only apply if there is no global context now.
     */
    private OneMFiveAppContext(boolean doInit, Properties properties) {
        this.initialize = doInit;
        config.putAll(properties);
    }

    public static void setLocale(Locale l) {
        locale = l;
    }

    public static Locale getLocale() {
        if(locale==null) {
            locale = Locale.US;
        }
        return locale;
    }

    public static String getVersion() {
        if(version==null) {
            if (config != null && config.get("1m5.version") != null) {
                version = config.getProperty("1m5.version");
            } else {
                version = "notset";
            }
        }
        return version;
    }

    private void configure() {
        // set early to ensure it's not called twice
        this.configured = true;
        try {
            config.putAll(Config.loadFromClasspath("1m5.config", config, false));
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }

        shutdownTasks = new ConcurrentHashSet<>(10);

        String version = getProperty("1m5.version");
        LOG.info("1M5 Version: "+version);

        System.setProperty("1m5.userTimeZone", TimeZone.getDefault().getID());

        String systemTimeZone = getProperty("1m5.systemTimeZone");
        LOG.info("1M5 System Time Zone: "+systemTimeZone);
        TimeZone.setDefault(TimeZone.getTimeZone(systemTimeZone));
        System.setProperty("1m5.systemTimeZone",systemTimeZone);

        String baseStr = getProperty("1m5.dir.base");
        if(baseStr!=null) {
            baseDir = new File(baseStr);
            if (!baseDir.exists() && !baseDir.mkdir()) {
                LOG.warning("Unable to create 1m5.dir.base: " + baseStr);
                return;
            }
        }  else {
            try {
                baseDir = SystemSettings.getUserAppHomeDir(".1m5","platform",true);
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
                return;
            }
            if(baseDir!=null) {
                config.put("1m5.dir.base", baseDir.getAbsolutePath());
            } else {
                baseDir = SystemSettings.getSystemApplicationDir(".1m5", "platform", true);
                if (baseDir == null) {
                    LOG.severe("Unable to create base system directory for 1M5 platform.");
                    return;
                } else {
                    baseStr = baseDir.getAbsolutePath();
                    config.put("1m5.dir.base", baseStr);
                }
            }
        }
        LOG.info("1M5 Base Directory: "+baseStr);

        configDir = new SecureFile(baseDir, "config");
        if(!configDir.exists() && !configDir.mkdir()) {
            LOG.severe("Unable to create config directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.config",configDir.getAbsolutePath());
        }

        libDir = new SecureFile(baseDir, "lib");
        if(!libDir.exists() && !libDir.mkdir()) {
            LOG.severe("Unable to create lib directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.lib",libDir.getAbsolutePath());
        }

        dataDir = new SecureFile(baseDir, "data");
        if(!dataDir.exists() && !dataDir.mkdir()) {
            LOG.severe("Unable to create data directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.data",dataDir.getAbsolutePath());
        }

        cacheDir = new SecureFile(baseDir, "cache");
        if(!cacheDir.exists() && !cacheDir.mkdir()) {
            LOG.severe("Unable to create cache directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.cache",cacheDir.getAbsolutePath());
        }

        pidDir = new SecureFile(baseDir, "pid");
        if (!pidDir.exists() && !pidDir.mkdir()) {
            LOG.severe("Unable to create pid directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.pid",pidDir.getAbsolutePath());
        }

        logDir = new SecureFile(baseDir, "logs");
        if (!logDir.exists() && !logDir.mkdir()) {
            LOG.severe("Unable to create logs directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.log",logDir.getAbsolutePath());
        }

        tmpDir = new SecureFile(baseDir, "tmp");
        if (!tmpDir.exists() && !tmpDir.mkdir()) {
            LOG.severe("Unable to create tmp directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.temp",tmpDir.getAbsolutePath());
        }

        servicesDir = new SecureFile(baseDir, "services");
        if (!servicesDir.exists() && !servicesDir.mkdir()) {
            LOG.severe("Unable to create services directory in 1M5 base directory.");
            return;
        } else {
            config.put("1m5.dir.services",servicesDir.getAbsolutePath());
        }

        LOG.info("1M5 Directories: " +
                "\n\tBase: "+baseDir.getAbsolutePath()+
                "\n\tConfig: "+configDir.getAbsolutePath()+
                "\n\tData: "+dataDir.getAbsolutePath()+
                "\n\tCache: "+cacheDir.getAbsolutePath()+
                "\n\tPID: "+pidDir.getAbsolutePath()+
                "\n\tLogs: "+logDir.getAbsolutePath()+
                "\n\tTemp: "+tmpDir.getAbsolutePath()+
                "\n\tServices: "+servicesDir.getAbsolutePath());

        clientAppManager = new ClientAppManager(false);
        // Instantiate Service Bus
        serviceBus = new ServiceBus(config, clientAppManager);

        if (initialize) {
            if (globalAppContext == null) {
                globalAppContext = this;
            } else {
                LOG.warning("Warning - New context not replacing old one, you now have an additional one");
                (new Exception("I did it")).printStackTrace();
            }
        }
        this.configured = true;
    }

    public ClientAppManager getClientAppManager(Properties props) {
        if(clientAppManager.getStatus() == ClientAppManager.Status.STOPPED)
            clientAppManager.initialize(props);
        return clientAppManager;
    }

    public ServiceBus getServiceBus() {
        return serviceBus;
    }

    /**
     *  This is the installation dir, often referred to as $1m5.
     *  Applications should consider this directory read-only and never
     *  attempt to write to it.
     *  It may actually be read-only on a multi-user installation.
     *
     *  In Linux, the path is: /home/[user]/1m5/platform
     *  In Mac, the path is: /home/[user]/Applications/1m5/platform
     *  in Windows, the path is: C:\\\\Program Files\\1m5\\platform
     *
     *  @return File constant for the life of the context
     */
    public File getBaseDir() { return baseDir; }

    /**
     *  The directory for config files.
     *  Dapps may use this to read router configuration files if necessary.
     *  There may also be config files in this directory as templates for user
     *  installations that should not be altered by dapps.
     *
     *  1m5/platform/config
     *
     *  @return File constant for the life of the context
     */
    public File getConfigDir() { return configDir; }

    /**
     *  The OS process id of the currently running instance.
     *  Dapps should not use this.
     *
     *  1m5/platform/pid
     *
     *  @return File constant for the life of the context
     */
    public File getPIDDir() { return pidDir; }

    /**
     *  Where the log directory is.
     *  Dapps should not use this.
     *
     *  1m5/platform/log
     *
     *  @return File constant for the life of the context
     */
    public File getLogDir() { return logDir; }

    /**
     *  Where the core stores core-specific data.
     *  Applications should create their own data directory within their base directory.
     *
     *  1m5/platform/data
     *
     *  @return File constant for the life of the context
     */
    public File getDataDir() { return dataDir; }

    /**
     *  Where the core may store cache.
     *  Applications should create their own cache directory within their base directory.
     *
     *  1m5/platform/cache
     *
     *  @return File constant for the life of the context
     */
    public File getCacheDir() { return cacheDir; }

    /**
     *  Where the core stores temporary data.
     *  This directory is created on the first call in this context and is deleted on JVM exit.
     *  Applications should create their own temp directory within their base directory.
     *
     *  1m5/platform/tmp
     *
     *  @return File constant for the life of the context
     */
    public File getTempDir() {
        // fixme don't synchronize every time
        synchronized (lock1) {
            if (tmpDir == null) {
                String d = getProperty("1m5.dir.temp", System.getProperty("java.io.tmpdir"));
                // our random() probably isn't warmed up yet
                byte[] rand = new byte[6];
                tmpDirRand.nextBytes(rand);
                String f = "1m5-" + Base64.getEncoder().encodeToString(rand) + ".tmp";
                tmpDir = new SecureFile(d, f);
                if (tmpDir.exists()) {
                    // good or bad ? loop and try again?
                } else if (tmpDir.mkdir()) {
                    tmpDir.deleteOnExit();
                } else {
                    LOG.warning("WARNING: Could not create temp dir " + tmpDir.getAbsolutePath());
                    tmpDir = new SecureFile(baseDir, "tmp");
                    tmpDir.mkdirs();
                    if (!tmpDir.exists())
                        LOG.severe("ERROR: Could not create temp dir " + tmpDir.getAbsolutePath());
                }
            }
        }
        return tmpDir;
    }

    /** don't rely on deleteOnExit() */
    public void deleteTempDir() {
        synchronized (lock1) {
            if (tmpDir != null) {
                FileUtil.rmdir(tmpDir, false);
                tmpDir = null;
            }
        }
    }

    /**
     * Access the configuration attributes of this context, using properties
     * provided during the context construction, or falling back on
     * System.getProperty if no properties were provided during construction
     * (or the specified prop wasn't included).
     *
     */
    public String getProperty(String propName) {
        String rv = config.getProperty(propName);
        if (rv != null)
            return rv;
        return System.getProperty(propName);
    }

    /**
     * Access the configuration attributes of this context, using properties
     * provided during the context construction, or falling back on
     * System.getProperty if no properties were provided during construction
     * (or the specified prop wasn't included).
     *
     */
    public String getProperty(String propName, String defaultValue) {
        if (config.containsKey(propName))
            return config.getProperty(propName, defaultValue);
        return System.getProperty(propName, defaultValue);
    }

    /**
     * Return an int with an int default
     */
    public int getProperty(String propName, int defaultVal) {
        String val = config.getProperty(propName);
        if (val == null)
            val = System.getProperty(propName);
        int ival = defaultVal;
        if (val != null) {
            try {
                ival = Integer.parseInt(val);
            } catch (NumberFormatException nfe) {LOG.warning(nfe.getLocalizedMessage());}
        }
        return ival;
    }

    /**
     * Return a long with a long default
     */
    public long getProperty(String propName, long defaultVal) {
        String val  = config.getProperty(propName);
        if (val == null)
            val = System.getProperty(propName);
        long rv = defaultVal;
        if (val != null) {
            try {
                rv = Long.parseLong(val);
            } catch (NumberFormatException nfe) {LOG.warning(nfe.getLocalizedMessage());}
        }
        return rv;
    }

    /**
     * Return a boolean with a boolean default
     */
    public boolean getProperty(String propName, boolean defaultVal) {
        String val = getProperty(propName);
        if (val == null)
            return defaultVal;
        return Boolean.parseBoolean(val);
    }

    /**
     * Access the configuration attributes of this context, listing the properties
     * provided during the context construction, as well as the ones included in
     * System.getProperties.
     *
     * @return new Properties with system and context properties
     */
    public Properties getProperties() {
        // clone to avoid ConcurrentModificationException
        Properties props = new Properties();
        props.putAll((java.util.Properties)System.getProperties().clone());
        props.putAll(config);
        return props;
    }

    /**
     *  Is the wrapper present?
     */
    public boolean hasWrapper() {
        return System.getProperty("wrapper.version") != null;
    }

}
