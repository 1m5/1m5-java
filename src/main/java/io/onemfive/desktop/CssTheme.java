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
