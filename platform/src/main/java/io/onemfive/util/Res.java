package io.onemfive.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Res {

    private static Logger LOG = Logger.getLogger(Res.class.getName());

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.displayStrings", LocaleUtil.currentLocale, new UTF8Control());

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static String get(String key, Object... arguments) {
        return MessageFormat.format(Res.get(key), arguments);
    }

    public static String get(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            LOG.warning("Missing resource for key: "+key);
            return key;
        }
    }
}

class UTF8Control extends ResourceBundle.Control {

    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {
        final String bundleName = toBundleName(baseName, locale);
        final String resourceName = toResourceName(bundleName, "properties");
        ResourceBundle bundle = null;
        InputStream stream = null;
        if (reload) {
            final URL url = loader.getResource(resourceName);
            if (url != null) {
                final URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }
        if (stream != null) {
            try {
                bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
            } finally {
                stream.close();
            }
        }
        return bundle;
    }
}
