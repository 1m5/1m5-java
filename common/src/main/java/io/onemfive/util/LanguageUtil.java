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
package io.onemfive.util;

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
        return Locale.getDefault().getLanguage();
    }


    public static String getEnglishLanguageLocaleCode() {
        return new Locale(Locale.ENGLISH.getLanguage()).getLanguage();
    }

    public static String getDisplayName(String code) {
        Locale locale = Locale.forLanguageTag(code);
        return locale.getDisplayName(locale);
    }

    public static boolean isDefaultLanguageRTL() {
        return rtlLanguagesCodes.contains(LanguageUtil.getDefaultLanguage());
    }

    public static List<String> getUserLanguageCodes() {
        return userLanguageCodes;
    }

}
