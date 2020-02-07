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
package io.onemfive.desktop.user;

import io.onemfive.desktop.CssTheme;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Preferences {

    public static Locale locale;

    static {
        locale = Locale.getDefault();

        // On some systems there is no country defined, in that case we use en_US
        if (locale.getCountry() == null || locale.getCountry().isEmpty())
            locale = Locale.US;
    }

    public static Boolean useAnimations = true;
    public static Integer cssTheme = CssTheme.CSS_THEME_LIGHT;
    public static Map<String,Boolean> showAgainMap = new HashMap<>();

    public static Boolean showAgain(String key) {
        return !showAgainMap.containsKey(key) || !showAgainMap.get(key);
    }
    public static void showAgain(String key, Boolean show) {
        showAgainMap.put(key, show);
    }

    private Preferences() {}

}
