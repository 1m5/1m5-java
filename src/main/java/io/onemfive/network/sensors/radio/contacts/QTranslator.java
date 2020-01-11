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
package io.onemfive.network.sensors.radio.contacts;

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
            // TODO: 1m5-radio-qtranslations.txt to map
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
