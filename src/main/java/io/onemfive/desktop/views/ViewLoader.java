package io.onemfive.desktop.views;

import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

public class ViewLoader {

    private static final Logger LOG = Logger.getLogger(ViewLoader.class.getName());

    private static final HashMap<Object, View> cache = new HashMap<>();

    public static View load(Class<? extends View> viewClass) {
        View view = null;
        if (cache.containsKey(viewClass)) {
            view = cache.get(viewClass);
        } else {
            URL loc = viewClass.getResource(viewClass.getSimpleName()+".fxml");
            try {
                view = FXMLLoader.load(loc);
                cache.put(viewClass, view);
            } catch (IOException e) {
                LOG.warning(e.getLocalizedMessage());
            }
        }
        return view;
    }
}
