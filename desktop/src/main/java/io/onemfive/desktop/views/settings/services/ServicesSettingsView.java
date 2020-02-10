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
package io.onemfive.desktop.views.settings.services;

import io.onemfive.desktop.MVC;
import io.onemfive.desktop.Navigation;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.View;
import io.onemfive.desktop.views.home.HomeView;
import io.onemfive.desktop.views.settings.SettingsView;
import io.onemfive.desktop.views.settings.services.dcdn.DCDNSettingsView;
import io.onemfive.desktop.views.settings.services.identity.IdentitySettingsView;
import io.onemfive.desktop.views.settings.services.infovault.InfovaultSettingsView;
import io.onemfive.desktop.views.settings.services.keyring.KeyringSettingsView;
import io.onemfive.desktop.views.settings.services.monetary.MonetarySettingsView;
import io.onemfive.util.Res;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ServicesSettingsView extends ActivatableView {

    private TabPane pane;
    @FXML
    private Tab identityTab, infovaultTab, keyringTab, monetaryTab, dcdnTab;

    private Navigation.Listener navigationListener;
    private ChangeListener<Tab> tabChangeListener;

    @Override
    public void initialize() {
        LOG.info("Initializing...");
        pane = (TabPane)root;
        identityTab.setText(Res.get("settings.services.tab.identity").toUpperCase());
        infovaultTab.setText(Res.get("settings.services.tab.infovault").toUpperCase());
        keyringTab.setText(Res.get("settings.services.tab.keyring").toUpperCase());
        monetaryTab.setText(Res.get("settings.services.tab.monetary").toUpperCase());
        dcdnTab.setText(Res.get("settings.services.tab.dcdn").toUpperCase());

        navigationListener = viewPath -> {
            if (viewPath.size() == 4 && viewPath.indexOf(ServicesSettingsView.class) == 2)
                loadView(viewPath.tip());
        };

        tabChangeListener = (ov, oldValue, newValue) -> {
            if (newValue == identityTab)
                MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, IdentitySettingsView.class);
            else if (newValue == infovaultTab)
                MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, InfovaultSettingsView.class);
            else if (newValue == keyringTab)
                MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, KeyringSettingsView.class);
            else if (newValue == monetaryTab)
                MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, MonetarySettingsView.class);
            else if (newValue == dcdnTab)
                MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, DCDNSettingsView.class);
        };

        LOG.info("Initialized.");
    }

    @Override
    protected void activate() {
        pane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);
        MVC.navigation.addListener(navigationListener);

        Tab selectedItem = pane.getSelectionModel().getSelectedItem();
        if (selectedItem == identityTab)
            MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, IdentitySettingsView.class);
        else if (selectedItem == infovaultTab)
            MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, InfovaultSettingsView.class);
        else if (selectedItem == keyringTab)
            MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, KeyringSettingsView.class);
        else if (selectedItem == monetaryTab)
            MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, MonetarySettingsView.class);
        else if (selectedItem == dcdnTab)
            MVC.navigation.navigateTo(HomeView.class, SettingsView.class, ServicesSettingsView.class, DCDNSettingsView.class);
    }

    @Override
    protected void deactivate() {
        pane.getSelectionModel().selectedItemProperty().removeListener(tabChangeListener);
        MVC.navigation.removeListener(navigationListener);
    }

    private void loadView(Class<? extends View> viewClass) {
        final Tab tab;
        View view = MVC.loadView(viewClass);

        if (view instanceof IdentitySettingsView) tab = identityTab;
        else if (view instanceof InfovaultSettingsView) tab = infovaultTab;
        else if (view instanceof KeyringSettingsView) tab = keyringTab;
        else if (view instanceof MonetarySettingsView) tab = monetaryTab;
        else if (view instanceof DCDNSettingsView) tab = dcdnTab;
        else throw new IllegalArgumentException("Navigation to " + viewClass + " is not supported");

        if (tab.getContent() != null && tab.getContent() instanceof ScrollPane) {
            ((ScrollPane) tab.getContent()).setContent(view.getRoot());
        } else {
            tab.setContent(view.getRoot());
        }
        pane.getSelectionModel().select(tab);
    }

}
