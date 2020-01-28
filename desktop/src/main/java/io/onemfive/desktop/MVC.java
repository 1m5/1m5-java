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
package io.onemfive.desktop;

import io.onemfive.desktop.views.BaseView;
import io.onemfive.desktop.views.View;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class MVC {

    private static final Logger LOG = Logger.getLogger(MVC.class.getName());

    public static final Properties preferences = new Properties();
    public static final Navigation navigation = new Navigation();
    public static final Map<String,Object> model = new HashMap<>();

    private static final HashMap<Object, BaseView> viewCache = new HashMap<>();

    public static View loadView(Class<? extends View> viewClass) {
        // Caching on by default
        return loadView(viewClass, true);
    }

    public static View loadView(Class<? extends View> viewClass, boolean useCache) {
        BaseView view = null;
        if (viewCache.containsKey(viewClass) && useCache) {
            view = viewCache.get(viewClass);
        } else {
            URL loc = viewClass.getResource(viewClass.getSimpleName()+".fxml");
            try {
                Node n = FXMLLoader.load(loc);
                if(n!=null) {
                    try {
                        view = (BaseView)Class.forName(viewClass.getName()).getConstructor().newInstance();
                        view.setRoot(n);
                        if(useCache) {
                            viewCache.put(viewClass, view);
                        }
                    } catch (InstantiationException e) {
                        LOG.warning(e.getLocalizedMessage());
                    } catch (IllegalAccessException e) {
                        LOG.warning(e.getLocalizedMessage());
                    } catch (InvocationTargetException e) {
                        LOG.warning(e.getLocalizedMessage());
                    } catch (NoSuchMethodException e) {
                        LOG.warning(e.getLocalizedMessage());
                    } catch (ClassNotFoundException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
                }
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        return view;
    }

}
