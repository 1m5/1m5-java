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
package io.onemfive.core.util;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.util.resources.TextResource;

/**
 * Translate strings efficiently.
 * We don't include an English or default ResourceBundle, we simply check
 * for "en" and return the original string.
 * Support real-time language changing with the routerconsole.lang
 * and routerconsole.country properties.
 *
 * To change language in router context, set the context properties PROP_LANG and PROP_COUNTRY.
 * To change language in app context, set the System properties PROP_LANG and PROP_COUNTRY.
 */
public abstract class Translate {
    public static final String PROP_LANG = "sc.lang";
    public static final String PROP_COUNTRY = "sc.country";
    /** non-null, two- or three-letter lower case, may be "" */
    private static final String _localeLang = Locale.getDefault().getLanguage();
    /** non-null, two-letter upper case, may be "" */
    private static final String _localeCountry = Locale.getDefault().getCountry();
    private static final Map<String, ResourceBundle> _bundles = new ConcurrentHashMap<>(16);
    private static final Set<String> _missing = new ConcurrentHashSet<>(16);
    /** use to look for untagged strings */
    private static final String TEST_LANG = "xx";
    private static final String TEST_STRING = "XXXX";

    /** lang in routerconsole.lang property, else current locale */
    public static String getString(String key, OneMFiveAppContext ctx, String bun) {
        String lang = getLanguage(ctx);
        if (lang.equals("en"))
            return key;
        else if (lang.equals(TEST_LANG))
            return TEST_STRING;
        // shouldnt happen but dont dump the po headers if it does
        if (key.equals(""))
            return key;
        ResourceBundle bundle = findBundle(bun, lang, getCountry(ctx));
        if (bundle == null)
            return key;
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     *  translate a string with a parameter
     *  This is a lot more expensive than getString(s, ctx), so use sparingly.
     *
     *  @param s string to be translated containing {0}
     *    The {0} will be replaced by the parameter.
     *    Single quotes must be doubled, i.e. ' -&gt; '' in the string.
     *  @param o parameter, not translated.
     *    To translate parameter also, use _t("foo {0} bar", _t("baz"))
     *    Do not double the single quotes in the parameter.
     *    Use autoboxing to call with ints, longs, floats, etc.
     */
    public static String getString(String s, Object o, OneMFiveAppContext ctx, String bun) {
        return getString(s, ctx, bun, o);
    }

    /** for {0} and {1} */
    public static String getString(String s, Object o, Object o2, OneMFiveAppContext ctx, String bun) {
        return getString(s, ctx, bun, o, o2);
    }

    /**
     *  Varargs
     *  @param oArray parameters
     */
    public static String getString(String s, OneMFiveAppContext ctx, String bun, Object... oArray) {
        String lang = getLanguage(ctx);
        if (lang.equals(TEST_LANG))
            return TEST_STRING + Arrays.toString(oArray) + TEST_STRING;
        String x = getString(s, ctx, bun);
        try {
            MessageFormat fmt = new MessageFormat(x, new Locale(lang));
            return fmt.format(oArray, new StringBuffer(), null).toString();
        } catch (IllegalArgumentException iae) {
            System.err.println("Bad format: orig: \"" + s +
                    "\" trans: \"" + x +
                    "\" params: " + Arrays.toString(oArray) +
                    " lang: " + lang);
            return "FIXME: " + x + ' ' + Arrays.toString(oArray);
        }
    }

    /**
     *  Use GNU ngettext
     *  For .po file format see http://www.gnu.org/software/gettext/manual/gettext.html.gz#Translating-plural-forms
     *
     *  @param n how many
     *  @param s singular string, optionally with {0} e.g. "one tunnel"
     *  @param p plural string optionally with {0} e.g. "{0} tunnels"
     */
    public static String getString(int n, String s, String p, OneMFiveAppContext ctx, String bun) {
        String lang = getLanguage(ctx);
        if (lang.equals(TEST_LANG))
            return TEST_STRING + '(' + n + ')' + TEST_STRING;
        ResourceBundle bundle = null;
        if (!lang.equals("en"))
            bundle = findBundle(bun, lang, getCountry(ctx));
        String x;
        if (bundle == null)
            x = n == 1 ? s : p;
        else
            x = TextResource.getText(bundle, s, p, n);
        Object[] oArray = new Object[1];
        oArray[0] = Integer.valueOf(n);
        try {
            MessageFormat fmt = new MessageFormat(x, new Locale(lang));
            return fmt.format(oArray, new StringBuffer(), null).toString();
        } catch (IllegalArgumentException iae) {
            System.err.println("Bad format: sing: \"" + s +
                    "\" plural: \"" + p +
                    "\" lang: " + lang);
            return "FIXME: " + s + ' ' + p + ',' + n;
        }
    }

    /**
     *  Two- or three-letter lower case
     *  @return lang in sc.lang property, else current locale
     */
    public static String getLanguage(OneMFiveAppContext ctx) {
        String lang = ctx.getProperty(PROP_LANG);
        if (lang == null || lang.length() <= 0)
            lang = _localeLang;
        return lang;
    }

    /**
     *  Two-letter upper case or ""
     *  @return country in sc.country property, else current locale
     */
    public static String getCountry(OneMFiveAppContext ctx) {
        // property may be empty so we don't have a non-default
        // language and a default country
        return ctx.getProperty(PROP_COUNTRY, _localeCountry);
    }

    /**
     *  Only for use by standalone apps in App Context.
     *  NOT for use in Conscious Context.
     *  Does not persist, apps must implement their own persistence.
     *  Does NOT override context properties.
     *
     *  @param lang Two- or three-letter lower case, or null for default
     *  @param country Two-letter upper case, or null for default, or "" for none
     */
    public static void setLanguage(String lang, String country) {
        if (lang != null)
            System.setProperty(PROP_LANG, lang);
        else
            System.clearProperty(PROP_LANG);
        if (country != null)
            System.setProperty(PROP_COUNTRY, country);
        else
            System.clearProperty(PROP_COUNTRY);
    }

    /**
     * cache both found and not found for speed
     * @param lang non-null, if "" returns null
     * @param country non-null, may be ""
     * @return null if not found
     */
    private static ResourceBundle findBundle(String bun, String lang, String country) {
        String key = bun + '-' + lang + '-' + country;
        ResourceBundle rv = _bundles.get(key);
        if (rv == null && !_missing.contains(key)) {
            if ("".equals(lang)) {
                _missing.add(key);
                return null;
            }
            try {
                Locale loc;
                if ("".equals(country))
                    loc = new Locale(lang);
                else
                    loc = new Locale(lang, country);
                // We must specify the class loader so that a webapp can find the bundle in the .war
                rv = ResourceBundle.getBundle(bun, loc, Thread.currentThread().getContextClassLoader());
                if (rv != null)
                    _bundles.put(key, rv);
            } catch (MissingResourceException e) {
                _missing.add(key);
            }
        }
        return rv;
    }

    /**
     *  Return the "display language", e.g. "English" for the language specified
     *  by langCode, using the current language.
     *  Uses translation if available, then JVM Locale.getDisplayLanguage() if available, else default param.
     *
     *  @param langCode two- or three-letter lower-case
     *  @param dflt e.g. "English"
     */
    public static String getDisplayLanguage(String langCode, String dflt, OneMFiveAppContext ctx, String bun) {
        String curLang = getLanguage(ctx);
        if (!"en".equals(curLang)) {
            String rv = getString(dflt, ctx, bun);
            if (!rv.equals(dflt))
                return rv;
            Locale curLocale = new Locale(curLang);
            rv = (new Locale(langCode)).getDisplayLanguage(curLocale);
            if (rv.length() > 0 && !rv.equals(langCode))
                return rv;
        }
        return dflt;
    }

    /**
     *  Clear the cache.
     *  Call this after adding new bundles to the classpath.
     */
    public static void clearCache() {
        _missing.clear();
        _bundles.clear();
        ResourceBundle.clearCache();
    }
}
