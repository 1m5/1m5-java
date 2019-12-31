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

    private static final HashMap<Object, View> cache = new HashMap<>();

    public static View load(Class<? extends View> viewClass) {
        // Caching on by default
        return load(viewClass, true);
    }

    public static View load(Class<? extends View> viewClass, boolean useCache) {
        View view = null;
        if (cache.containsKey(viewClass) && useCache) {
            view = cache.get(viewClass);
        } else {
            URL loc = viewClass.getResource(viewClass.getSimpleName()+".fxml");
            try {
                Node n = FXMLLoader.load(loc);
                if(n!=null) {
                    try {
                        view = (View)Class.forName(viewClass.getName()).getConstructor().newInstance();
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
