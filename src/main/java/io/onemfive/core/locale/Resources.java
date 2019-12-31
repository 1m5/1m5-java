package io.onemfive.core.locale;

import io.onemfive.core.OneMFiveAppContext;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Resources {

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.displayStrings", OneMFiveAppContext.getLocale(), new UTF8Control());

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static String getWithCol(String key, Object... arguments) {
        return get(key, arguments) + ":";
    }

    public static String get(String key, Object... arguments) {
        return MessageFormat.format(Resources.get(key), arguments);
    }

    public static String get(String key) {
        if(resourceBundle==null) return null;
        return resourceBundle.getString(key);
    }
}
