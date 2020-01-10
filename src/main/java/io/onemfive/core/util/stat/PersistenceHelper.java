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
package io.onemfive.core.util.stat;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.util.data.DataHelper;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *  Output rate data.
 *  This is used via ProfilePersistenceHelper and the output
 *  must be compatible.
 */
class PersistenceHelper {

    private static Logger LOG = Logger.getLogger(PersistenceHelper.class.getName());
    private final static String NL = System.getProperty("line.separator");

    public static void add(StringBuilder buf, String prefix, String name, String description, double value) {
        buf.append("# ").append(prefix).append(name).append(NL);
        buf.append("# ").append(description).append(NL);
        buf.append(prefix).append(name).append('=').append(value).append(NL).append(NL);
    }

    public static void addDate(StringBuilder buf, String prefix, String name, String description, long value) {
        String when = value > 0 ? (new Date(value)).toString() : "Never";
        add(buf, prefix, name, description + ' ' + when, value);
    }

    public static void addTime(StringBuilder buf, String prefix, String name, String description, long value) {
        String when = DataHelper.formatDuration(value);
        add(buf, prefix, name, description + ' ' + when, value);
    }

    public static void add(StringBuilder buf, String prefix, String name, String description, long value) {
        buf.append("# ").append(prefix).append(name).append(NL);
        buf.append("# ").append(description).append(NL);
        buf.append(prefix).append(name).append('=').append(value).append(NL).append(NL);
    }

    /**
     *  @return non-negative, returns 0 on error
     */
    public static long getLong(Properties props, String prefix, String name) {
        String val = props.getProperty(prefix + name);
        if (val != null) {
            try {
                long rv = Long.parseLong(val);
                return rv >= 0 ? rv : 0;
            } catch (NumberFormatException nfe) {
                LOG.warning("Error formatting " + val+": "+ nfe.getLocalizedMessage());
            }
        }
        return 0;
    }

    /**
     *  @return 0 on error
     */
    public static double getDouble(Properties props, String prefix, String name) {
        String val = props.getProperty(prefix + name);
        if (val != null) {
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException nfe) {
                LOG.warning("Error formatting " + val+": "+nfe.getLocalizedMessage());
            }
        }
        return 0;
    }

    /**
     *  @return non-negative, returns 0 on error
     */
    public static int getInt(Properties props, String prefix, String name) {
        String val = props.getProperty(prefix + name);
        if (val != null) {
            try {
                int rv = Integer.parseInt(val);
                return rv >= 0 ? rv : 0;
            } catch (NumberFormatException nfe) {
                LOG.warning("Error formatting " + val+": "+nfe.getLocalizedMessage());
            }
        }
        return 0;
    }

}
