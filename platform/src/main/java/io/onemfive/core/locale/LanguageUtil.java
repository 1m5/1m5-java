package io.onemfive.core.locale;

import io.onemfive.core.OneMFiveAppContext;

import java.util.*;
import java.util.stream.Collectors;

public class LanguageUtil {

    private static final List<String> userLanguageCodes = Arrays.asList(
            "en", // English
            "de", // German
            "el", // Greek
            "es", // Spanish
            "pt", // Portuguese
            "pt-BR", // Portuguese (Brazil)
            "zh-Hans", // Chinese [Han Simplified]
            "zh-Hant", // Chinese [Han Traditional]
            "ru", // Russian
            "fr", // French
            "vi", // Vietnamese
            "th", // Thai
            "ja", // Japanese
            "fa" // Persian
            /*
            // not translated yet
            "sr-Latn-RS", // Serbian [Latin] (Serbia)
            "hu", // Hungarian
            "ro", // Romanian
            "tr" // Turkish
            "it", // Italian
            "iw", // Hebrew
            "hi", // Hindi
            "ko", // Korean
            "pl", // Polish
            "sv", // Swedish
            "no", // Norwegian
            "nl", // Dutch
            "be", // Belarusian
            "fi", // Finnish
            "bg", // Bulgarian
            "lt", // Lithuanian
            "lv", // Latvian
            "hr", // Croatian
            "uk", // Ukrainian
            "sk", // Slovak
            "sl", // Slovenian
            "ga", // Irish
            "sq", // Albanian
            "ca", // Catalan
            "mk", // Macedonian
            "kk", // Kazakh
            "km", // Khmer
            "sw", // Swahili
            "in", // Indonesian
            "ms", // Malay
            "is", // Icelandic
            "et", // Estonian
            "cs", // Czech
            "ar", // Arabic
            "vi", // Vietnamese
            "th", // Thai
            "da", // Danish
            "mt"  // Maltese
            */
    );

    private static final List<String> rtlLanguagesCodes = Arrays.asList(
            "fa", // Persian
            "ar", // Arabic
            "iw" // Hebrew
    );

    public static List<String> getAllLanguageCodes() {
        List<Locale> allLocales = LocaleUtil.getAllLocales();

        // Filter duplicate locale entries
        Set<String> allLocalesAsSet = allLocales.stream().filter(locale -> !locale.getLanguage().isEmpty() &&
                !locale.getDisplayLanguage().isEmpty())
                .map(Locale::getLanguage)
                .collect(Collectors.toSet());

        List<String> allLanguageCodes = new ArrayList<>();
        allLanguageCodes.addAll(allLocalesAsSet);
        allLanguageCodes.sort((o1, o2) -> getDisplayName(o1).compareTo(getDisplayName(o2)));
        return allLanguageCodes;
    }

    public static String getDefaultLanguage() {
        // might be set later in pref or config, so not use defaultLocale anywhere in the code
        return getLocale().getLanguage();
    }

    public static String getDefaultLanguageLocaleAsCode() {
        return new Locale(LanguageUtil.getDefaultLanguage()).getLanguage();
    }

    public static String getEnglishLanguageLocaleCode() {
        return new Locale(Locale.ENGLISH.getLanguage()).getLanguage();
    }

    public static String getDisplayName(String code) {
        Locale locale = Locale.forLanguageTag(code);
        return locale.getDisplayName(locale);
    }

    public static boolean isDefaultLanguageRTL() {
        return rtlLanguagesCodes.contains(LanguageUtil.getDefaultLanguageLocaleAsCode());
    }

    public static List<String> getUserLanguageCodes() {
        return userLanguageCodes;
    }

    private static Locale getLocale() {
        return OneMFiveAppContext.getLocale();
    }
}

