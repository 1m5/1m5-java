package io.onemfive.network.sensors.radio.contacts;

import io.onemfive.data.util.FileUtil;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Translate QSignal into i18n/l10n
 */
public class QTranslator {

    private static Logger LOG = Logger.getLogger(QTranslator.class.getName());
    private static Map<String, String> translations;

    public static String translateQuery(QSignal querySignal, String ... args) {
        loadTranslations();
        return merge(translations.get(querySignal.name()+"-en-q"), args);
    }

    public static String translateResponse(QSignal responseSignal, String ... args) {
        loadTranslations();
        return merge(translations.get(responseSignal.name()+"-en-r"), args);
    }

    public static String translateQuery(String twoCharLang, QSignal querySignal, String ... args) {
        loadTranslations();
        return merge(translations.get(querySignal.name()+"-"+twoCharLang+"-q"), args);
    }

    public static String translateResponse(String twoCharLang, QSignal responseSignal, String ... args) {
        loadTranslations();
        return merge(translations.get(responseSignal.name()+"-"+twoCharLang+"-r"), args);
    }

    private static void loadTranslations() {
        if(translations == null) {
            translations = FileUtil.textFileToMap("translations.txt", 1000, "=", true, true);
        }
    }

    private static String merge(String t, String ... args) {
        int h = 1;
        for(String a : args) {
            t = t.replace(":"+(h++), a);
        }
        return t;
    }

}
