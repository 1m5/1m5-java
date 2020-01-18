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

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class Config {

    public static String PROP_OPERATING_SYSTEM = "OPERATING_SYSTEM";
    public static String PROP_UI = "1m5.ui"; // true | false
    public static String PROP_UI_LAUNCH_ON_START = "1m5.ui.launchOnStart"; // true | false

    public enum OS {Android,Linux,OSX,Windows}

    private static final Logger LOG = Logger.getLogger(Config.class.getName());

    public static void logProperties(Properties p) {
        Enumeration e = p.keys();
        while(e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String value = p.getProperty(key);
            LOG.info(key+":"+value);
        }
    }

    public static Properties loadFromClasspath(String name) throws Exception {
        return loadFromClasspath(name, null, false);
    }

    public static Properties loadFromClasspath(String name, Properties inProps, boolean overrideSupplied) throws Exception {
        LOG.info("Loading properties file "+name+"...");
        Properties p = new Properties();
        if(inProps != null && overrideSupplied)
            p.putAll(inProps);
        InputStream is = null;
        try {
            is = Config.class.getClassLoader().getResourceAsStream(name);
            p.load(is);
            Enumeration propNames = p.propertyNames();
            while(propNames.hasMoreElements()){
                String propName = (String)propNames.nextElement();
                p.put(propName, p.getProperty(propName));
            }
        } catch (Exception e) {
            LOG.warning("Failed to load properties file "+name);
            throw e;
        } finally {
            if(is!=null)
                try { is.close();} catch (IOException e) {}
        }
        if(inProps != null && !overrideSupplied)
            p.putAll(inProps);
        return p;
    }

    public static Properties loadFromBase(String name) throws IOException {
        LOG.info("Loading properties file "+name+"...");
        Properties p = new Properties();
        InputStream is = null;
        String path = OneMFiveAppContext.getInstance().getBaseDir()+"/"+name;
        LOG.info("Loading properties file from "+path+"...");
        File folder = new File(path);
        boolean pathExists = true;
        if(folder.exists()) {
            try {
                is = new FileInputStream(path);
                p.load(is);
                LOG.info("Loaded properties file " + path + " with following name-value pairs:");
                Enumeration propNames = p.propertyNames();
                while (propNames.hasMoreElements()) {
                    String propName = (String) propNames.nextElement();
                    LOG.info(propName + ":" + p.getProperty(propName));
                }
            } catch (Exception e) {
                LOG.warning("Failed to load properties file " + path);
                throw e;
            } finally {
                if (is != null)
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
            }
        } else {
            try {
                pathExists = folder.createNewFile();
            } catch (IOException e) {
                LOG.warning("Failed to create new file at: "+path);
                throw(e);
            }
        }
        if(!pathExists) {
            LOG.warning("Couldn't create path: "+path);
        }

        return p;
    }

    public static void saveToClasspath(String name, Properties props) throws IOException {
        LOG.info("Saving properties file "+name+"...");
        props.store(new FileWriter(name), null);
    }

    public static void saveToBase(String name, Properties props) throws IOException {
        LOG.info("Saving properties file "+name+"...");
        String path = OneMFiveAppContext.getInstance().getBaseDir()+"/"+name;
        props.store(new FileWriter(path), null);
    }

}
