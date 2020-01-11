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
package io.onemfive.network.sensors.clearnet;

import io.onemfive.util.SystemVersion;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

public class ClearnetServerUtil {

    private static Logger LOG = Logger.getLogger(ClearnetServerUtil.class.getName());

    public static void launchBrowser(String url) {
        String[] cmd = null;
        if(SystemVersion.isLinux()) {
            LOG.info("OS is Linux.");
            String[] browsers = { "purebrowser", "epiphany", "firefox", "mozilla", "konqueror",
                    "netscape", "opera", "links", "lynx" };
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < browsers.length; i++)
                if(i == 0)
                    sb.append(String.format(    "%s \"%s\"", browsers[i], url));
                else
                    sb.append(String.format(" || %s \"%s\"", browsers[i], url));
            // If the first didn't work, try the next browser and so on
            cmd = new String[]{"sh", "-c", sb.toString()};
        } else if(SystemVersion.isMac()) {
            LOG.info("OS is Mac.");
            cmd = new String[]{"open " + url};
        } else if(SystemVersion.isWindows()) {
            LOG.info("OS is Windows.");
            try {
                Desktop.getDesktop().browse(new URL(url).toURI());
            } catch (MalformedURLException e) {
                LOG.severe("MalformedURLException caught while launching browser for windows. Error message: "+e.getLocalizedMessage());
            } catch (IOException e) {
                LOG.severe("IOException caught while launching browser for windows. Error message: "+e.getLocalizedMessage());
            } catch (URISyntaxException e) {
                LOG.severe("URISyntaxException caught while launching browser for windows. Error message: "+e.getLocalizedMessage());
            }
            return;
        } else {
            LOG.warning("Unable to determine OS therefore unable to launch a browser.");
            return;
        }

        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
