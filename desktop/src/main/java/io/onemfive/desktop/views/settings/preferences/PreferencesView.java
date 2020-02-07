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
package io.onemfive.desktop.views.settings.preferences;

import io.onemfive.desktop.CssTheme;
import io.onemfive.desktop.DesktopApp;
import io.onemfive.desktop.components.TitledGroupBg;
import io.onemfive.desktop.components.overlays.popups.Popup;
import io.onemfive.desktop.user.Preferences;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.util.LanguageUtil;
import io.onemfive.util.Res;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.Locale;

import static io.onemfive.desktop.util.FormBuilder.*;

public class PreferencesView extends ActivatableView {

    private GridPane pane;
    private ComboBox<String> userLanguageComboBox;

    private ToggleButton useAnimations, useDarkMode;
    private int gridRow = 0;

    private ObservableList<String> languageCodes;

    public PreferencesView() {

    }

    @Override
    public void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;
        languageCodes = FXCollections.observableArrayList(LanguageUtil.getUserLanguageCodes());

        TitledGroupBg titledGroupBg = addTitledGroupBg(pane, gridRow, 2, Res.get("setting.preferences.general"));
        GridPane.setColumnSpan(titledGroupBg, 1);

        userLanguageComboBox = addComboBox(pane, ++gridRow, Res.get("shared.language"), Layout.FIRST_ROW_DISTANCE);

        TitledGroupBg titledGroupBg1 = addTitledGroupBg(pane, ++gridRow, 3, Res.get("setting.preferences.displayOptions"), Layout.FIRST_ROW_DISTANCE);
        GridPane.setColumnSpan(titledGroupBg1, 1);

        useAnimations = addSlideToggleButton(pane, ++gridRow, Res.get("setting.preferences.useAnimations"), Layout.TWICE_FIRST_ROW_DISTANCE);
        useDarkMode = addSlideToggleButton(pane, ++gridRow, Res.get("setting.preferences.useDarkMode"));

        LOG.info("Initialized");
    }

    @Override
    protected void activate() {
        activateGeneralOptions();
        activateDisplayPreferences();
    }

    @Override
    protected void deactivate() {
        deactivateGeneralOptions();
        deactivateDisplayPreferences();
    }

    private void activateGeneralOptions() {

        userLanguageComboBox.setItems(languageCodes);
        userLanguageComboBox.getSelectionModel().select(Preferences.locale.getLanguage());
        userLanguageComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String code) {
                return LanguageUtil.getDisplayName(code);
            }

            @Override
            public String fromString(String string) {
                return null;
            }
        });

        userLanguageComboBox.setOnAction(e -> {
            String selectedItem = userLanguageComboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Preferences.locale = Locale.forLanguageTag(selectedItem);
                new Popup().information(Res.get("settings.preferences.languageChange"))
                        .closeButtonText(Res.get("shared.ok"))
                        .show();
            }
        });
    }

    private void activateDisplayPreferences() {

        useAnimations.setSelected(Preferences.useAnimations);
        useAnimations.setOnAction(e -> Preferences.useAnimations = useAnimations.isSelected());

        useDarkMode.setSelected(Preferences.cssTheme == 1);
        useDarkMode.setOnAction(e -> {
            Preferences.cssTheme = useDarkMode.isSelected() ? 1 : 0;
            CssTheme.loadSceneStyles(DesktopApp.scene, Preferences.cssTheme);
        });

    }

    private void deactivateGeneralOptions() {
        userLanguageComboBox.setOnAction(null);
    }

    private void deactivateDisplayPreferences() {
        useAnimations.setOnAction(null);
        useDarkMode.setOnAction(null);
    }

}
