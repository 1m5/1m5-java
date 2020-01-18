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
package io.onemfive.network.sensors.i2p;

import io.onemfive.core.OneMFiveAppContext;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class I2PRouterUtil {

    private static final Logger LOG = Logger.getLogger(I2PRouterUtil.class.getName());

    public static Router getGlobalI2PRouter(Properties properties, boolean autoStart) {
        Router globalRouter = null;
        RouterContext routerContext = RouterContext.listContexts().get(0);
        if(routerContext != null) {
            globalRouter = routerContext.router();
            if(globalRouter == null) {
                LOG.info("Instantiating I2P Router...");
                File baseDir = OneMFiveAppContext.getInstance().getBaseDir();
                String baseDirPath = baseDir.getAbsolutePath();
                System.setProperty("i2p.dir.base", baseDirPath);
                System.setProperty("i2p.dir.config", baseDirPath);
                System.setProperty("wrapper.logfile", baseDirPath + "/wrapper.log");
                globalRouter = new Router(properties);
            }
            if(autoStart && !globalRouter.isAlive()) {
                LOG.info("Starting I2P Router...");
                globalRouter.setKillVMOnEnd(false);
                globalRouter.runRouter();
            }
        }
        return globalRouter;
    }

}
