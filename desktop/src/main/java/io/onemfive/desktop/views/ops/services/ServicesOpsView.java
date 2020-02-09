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
package io.onemfive.desktop.views.ops.services;

import io.onemfive.desktop.MVC;
import io.onemfive.desktop.Navigation;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.View;
import io.onemfive.desktop.views.home.HomeView;
import io.onemfive.desktop.views.ops.OpsView;
import io.onemfive.desktop.views.ops.services.identity.IdentityOpsView;
import io.onemfive.desktop.views.ops.services.infovault.InfovaultOpsView;
import io.onemfive.desktop.views.ops.services.keyring.KeyringOpsView;
import io.onemfive.desktop.views.ops.services.monetary.MonetaryOpsView;
import io.onemfive.util.Res;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ServicesOpsView extends ActivatableView {

    private TabPane pane;
    @FXML
    private Tab identityTab, infovaultTab, keyringTab, monetaryTab;

    private Navigation.Listener navigationListener;
    private ChangeListener<Tab> tabChangeListener;

    @Override
    public void initialize() {
        LOG.info("Initializing...");
        pane = (TabPane)root;
        identityTab.setText(Res.get("ops.services.tab.identity").toUpperCase());
        infovaultTab.setText(Res.get("ops.services.tab.infovault").toUpperCase());
        keyringTab.setText(Res.get("ops.services.tab.keyring").toUpperCase());
        monetaryTab.setText(Res.get("ops.services.tab.monetary").toUpperCase());

        navigationListener = viewPath -> {
            if (viewPath.size() == 4 && viewPath.indexOf(ServicesOpsView.class) == 2)
                loadView(viewPath.tip());
        };

        tabChangeListener = (ov, oldValue, newValue) -> {
            if (newValue == identityTab)
                MVC.navigation.navigateTo(HomeView.class, OpsView.class, ServicesOpsView.class, IdentityOpsView.class);
            else if (newValue == infovaultTab)
                MVC.navigation.navigateTo(HomeView.class, OpsView.class, ServicesOpsView.class, InfovaultOpsView.class);
            else if (newValue == keyringTab)
                MVC.navigation.navigateTo(HomeView.class, OpsView.class, ServicesOpsView.class, KeyringOpsView.class);
            else if (newValue == monetaryTab)
                MVC.navigation.navigateTo(HomeView.class, OpsView.class, ServicesOpsView.class, MonetaryOpsView.class);
        };

        LOG.info("Initialized.");
    }

    @Override
    protected void activate() {
        pane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);
        MVC.navigation.addListener(navigationListener);

        Tab selectedItem = pane.getSelectionModel().getSelectedItem();
        if (selectedItem == identityTab)
            MVC.navigation.navigateTo(HomeView.class, OpsView.class, ServicesOpsView.class, IdentityOpsView.class);
        else if (selectedItem == infovaultTab)
            MVC.navigation.navigateTo(HomeView.class, OpsView.class, ServicesOpsView.class, InfovaultOpsView.class);
        else if (selectedItem == keyringTab)
            MVC.navigation.navigateTo(HomeView.class, OpsView.class, ServicesOpsView.class, KeyringOpsView.class);
        else if (selectedItem == monetaryTab)
            MVC.navigation.navigateTo(HomeView.class, OpsView.class, ServicesOpsView.class, MonetaryOpsView.class);
    }

    @Override
    protected void deactivate() {
        pane.getSelectionModel().selectedItemProperty().removeListener(tabChangeListener);
        MVC.navigation.removeListener(navigationListener);
    }

    private void loadView(Class<? extends View> viewClass) {
        final Tab tab;
        View view = MVC.loadView(viewClass);

        if (view instanceof IdentityOpsView) tab = identityTab;
        else if (view instanceof InfovaultOpsView) tab = infovaultTab;
        else if (view instanceof KeyringOpsView) tab = keyringTab;
        else if (view instanceof MonetaryOpsView) tab = monetaryTab;
        else throw new IllegalArgumentException("Navigation to " + viewClass + " is not supported");

        if (tab.getContent() != null && tab.getContent() instanceof ScrollPane) {
            ((ScrollPane) tab.getContent()).setContent(view.getRoot());
        } else {
            tab.setContent(view.getRoot());
        }
        pane.getSelectionModel().select(tab);
    }

}
