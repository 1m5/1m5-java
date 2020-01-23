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
package io.onemfive.desktop;

import javafx.scene.Scene;

import java.util.List;

public class CssTheme {

    public static final int CSS_THEME_LIGHT = 0;
    public static final int CSS_THEME_DARK = 1;

    public static void loadSceneStyles(Scene scene, int cssTheme) {

        scene.getStylesheets().add(CssTheme.class.getResource("1m5.css").toExternalForm());
        scene.getStylesheets().add(CssTheme.class.getResource("images.css").toExternalForm());

        switch (cssTheme) {

            case CSS_THEME_DARK:
                scene.getStylesheets().add(CssTheme.class.getResource("theme-dark.css").toExternalForm());
                break;

            case CSS_THEME_LIGHT:
            default:
                scene.getStylesheets().add(CssTheme.class.getResource("theme-light.css").toExternalForm());
                break;
        }

//        List<String> families = javafx.scene.text.Font.getFamilies();
//        for(String f : families){
//            System.out.println(f);
//        }
    }
}