package io.onemfive.core.locale;

import io.onemfive.core.OneMFiveAppContext;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Resources {

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.displayStrings", OneMFiveAppContext.getLocale(), new UTF8Control());

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static String getWithCol(String key, Object... arguments) throws Exception {
        return get(key, arguments) + ":";
    }

    public static String get(String key, Object... arguments) throws Exception {
        return MessageFormat.format(Resources.get(key), arguments);
    }

    public static String get(String key) throws Exception {
        if(resourceBundle==null) throw new Exception(key+" key not found in resource bundle.");
        return resourceBundle.getString(key);
    }
}
