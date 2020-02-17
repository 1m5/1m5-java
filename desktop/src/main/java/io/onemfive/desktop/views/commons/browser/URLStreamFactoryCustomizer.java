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
package io.onemfive.desktop.views.commons.browser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class URLStreamFactoryCustomizer {

    private static Logger LOG = Logger.getLogger(URLStreamFactoryCustomizer.class.getName());

    public static void useDedicatedProxyForWebkit(Proxy proxy, String protocols) {

        forceInitializationOfOriginalUrlStreamHandlers();
        tryReplaceOriginalUrlStreamHandlersWithScopeProxyAwareVariants(proxy, protocols);
    }

    private static void tryReplaceOriginalUrlStreamHandlersWithScopeProxyAwareVariants(Proxy proxy, String protocols) {

        try {

            Hashtable handlers = tryExtractInternalHandlerTableFromUrl();
            //LOG.info(handlers);

            Consumer<String> wrapStreamHandlerWithScopedProxyHandler = protocol ->
            {
                URLStreamHandler originalHandler = (URLStreamHandler) handlers.get(protocol);
                handlers.put(protocol, new DelegatingScopedProxyAwareUrlStreamHandler(originalHandler, proxy));
            };

            Arrays.stream(protocols.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(wrapStreamHandlerWithScopedProxyHandler);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Hashtable tryExtractInternalHandlerTableFromUrl() {

        try {
            Field handlersField = URL.class.getDeclaredField("handlers");
            handlersField.setAccessible(true);
            return (Hashtable) handlersField.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void forceInitializationOfOriginalUrlStreamHandlers() {

        try {
            new URL("http://.");
            new URL("https://.");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    static class DelegatingScopedProxyAwareUrlStreamHandler extends URLStreamHandler {

        private static final Method openConnectionMethod;
        private static final Method openConnectionWithProxyMethod;

        static {

            try {
                openConnectionMethod = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
                openConnectionWithProxyMethod = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class, Proxy.class);

                openConnectionMethod.setAccessible(true);
                openConnectionWithProxyMethod.setAccessible(true);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private final URLStreamHandler delegatee;
        private final Proxy proxy;

        public DelegatingScopedProxyAwareUrlStreamHandler(URLStreamHandler delegatee, Proxy proxy) {

            this.delegatee = delegatee;
            this.proxy = proxy;
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {

            try {
                if (isWebKitURLLoaderThread(Thread.currentThread())) {

                    //WebKit requested loading the given url, use provided proxy.
                    return (URLConnection) openConnectionWithProxyMethod.invoke(delegatee, url, proxy);
                }

                //Invoke the standard url handler.
                return (URLConnection) openConnectionMethod.invoke(delegatee, url);

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean isWebKitURLLoaderThread(Thread thread) {

            StackTraceElement[] st = thread.getStackTrace();

            //TODO Add more robust stack-trace inspection.
            return st.length > 4 && st[4].getClassName().startsWith("com.sun.webkit.network");
        }
    }
}
