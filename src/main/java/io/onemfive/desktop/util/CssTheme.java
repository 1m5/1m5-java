package io.onemfive.desktop.util;

import javafx.scene.Scene;

public class CssTheme {
    public static final int CSS_THEME_LIGHT = 0;
    public static final int CSS_THEME_DARK = 1;

    public static void loadSceneStyles(Scene scene, int cssTheme) {
        String cssThemeFolder = "/io/onemfive/desktop/";
        String cssThemeFile = "";

        switch (cssTheme) {

            case CSS_THEME_DARK:
                cssThemeFile = "theme-dark.css";
                break;

            case CSS_THEME_LIGHT:
            default:
                cssThemeFile = "theme-light.css";
                break;
        }

        scene.getStylesheets().setAll(
            // load base styles first
            cssThemeFolder + "1m5.css",
            cssThemeFolder + "images.css",

            // load theme last to allow override
            cssThemeFolder + cssThemeFile
        );
    }
}
