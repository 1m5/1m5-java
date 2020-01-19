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
package io.onemfive.desktop.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

public class ViewLoader {

    private static final Logger LOG = Logger.getLogger(ViewLoader.class.getName());
    private static final HashMap<Object, BaseView> cache = new HashMap<>();

    public static View load(Class<? extends View> viewClass) {
        // Caching on by default
        return load(viewClass, true);
    }

    public static View load(Class<? extends View> viewClass, boolean useCache) {
        BaseView view = null;
        if (cache.containsKey(viewClass) && useCache) {
            view = cache.get(viewClass);
        } else {
            URL loc = viewClass.getResource(viewClass.getSimpleName()+".fxml");
            try {
                Node n = FXMLLoader.load(loc);
                if(n!=null) {
                    try {
                        view = (BaseView)Class.forName(viewClass.getName()).getConstructor().newInstance();
                        view.setRoot(n);
                        if(useCache) {
                            cache.put(viewClass, view);
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
