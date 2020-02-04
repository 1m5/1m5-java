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
package io.onemfive.desktop.views.personal;

import io.onemfive.desktop.MVC;
import io.onemfive.desktop.Navigation;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.View;
import io.onemfive.desktop.views.home.HomeView;
import io.onemfive.desktop.views.personal.agora.AgoraView;
import io.onemfive.desktop.views.personal.blog.BlogView;
import io.onemfive.desktop.views.personal.calendar.CalendarView;
import io.onemfive.desktop.views.personal.dashboard.DashboardView;
import io.onemfive.desktop.views.personal.identities.IdentitiesView;
import io.onemfive.desktop.views.personal.wallet.WalletView;
import io.onemfive.util.Res;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class PersonalView extends ActivatableView {

    private Scene scene;
    private TabPane pane;
    @FXML
    private Tab blogTab, agoraTab, calendarTab, dashboardTab, identitiesTab, walletTab;

    private Navigation.Listener navigationListener;
    private ChangeListener<Tab> tabChangeListener;
//    private EventHandler<KeyEvent> keyEventEventHandler;

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        pane = (TabPane)root;
        blogTab.setText(Res.get("personalView.tabs.blog").toUpperCase());
        agoraTab.setText(Res.get("personalView.tabs.agora").toUpperCase());
        calendarTab.setText(Res.get("personalView.tabs.calendar").toUpperCase());
        dashboardTab.setText(Res.get("personalView.tabs.dashboard").toUpperCase());
        identitiesTab.setText(Res.get("personalView.tabs.identities").toUpperCase());
        walletTab.setText(Res.get("personalView.tabs.wallet").toUpperCase());

        navigationListener = viewPath -> {
            if (viewPath.size() == 3 && viewPath.indexOf(PersonalView.class) == 1)
                loadView(viewPath.tip());
        };

        tabChangeListener = (ov, oldValue, newValue) -> {
            if(newValue == blogTab)
                MVC.navigation.navigateTo(HomeView.class, PersonalView.class, BlogView.class);
            else if (newValue == agoraTab)
                MVC.navigation.navigateTo(HomeView.class, PersonalView.class, AgoraView.class);
            else if (newValue == calendarTab)
                MVC.navigation.navigateTo(HomeView.class, PersonalView.class, CalendarView.class);
            else if (newValue == dashboardTab)
                MVC.navigation.navigateTo(HomeView.class, PersonalView.class, DashboardView.class);
            else if (newValue == identitiesTab)
                MVC.navigation.navigateTo(HomeView.class, PersonalView.class, IdentitiesView.class);
            else if (newValue == walletTab)
                MVC.navigation.navigateTo(HomeView.class, PersonalView.class, WalletView.class);
        };

        LOG.info("Initialized.");
    }

    @Override
    protected void activate() {
        pane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);
        MVC.navigation.addListener(navigationListener);

        if(pane.getSelectionModel().getSelectedItem() == blogTab)
            MVC.navigation.navigateTo(HomeView.class, PersonalView.class, BlogView.class);
        else if (pane.getSelectionModel().getSelectedItem() == agoraTab)
            MVC.navigation.navigateTo(HomeView.class, PersonalView.class, AgoraView.class);
        else if (pane.getSelectionModel().getSelectedItem() == calendarTab)
            MVC.navigation.navigateTo(HomeView.class, PersonalView.class, CalendarView.class);
        else if (pane.getSelectionModel().getSelectedItem() == identitiesTab)
            MVC.navigation.navigateTo(HomeView.class, PersonalView.class, IdentitiesView.class);
        else if (pane.getSelectionModel().getSelectedItem() == walletTab)
            MVC.navigation.navigateTo(HomeView.class, PersonalView.class, WalletView.class);
        else
            MVC.navigation.navigateTo(HomeView.class, PersonalView.class, DashboardView.class);

//        if (root.getScene() != null) {
//            scene = root.getScene();
//            scene.addEventHandler(KeyEvent.KEY_RELEASED, keyEventEventHandler);
//        }
    }

    @Override
    protected void deactivate() {
        pane.getSelectionModel().selectedItemProperty().removeListener(tabChangeListener);
        MVC.navigation.removeListener(navigationListener);

//        if (scene != null)
//            scene.removeEventHandler(KeyEvent.KEY_RELEASED, keyEventEventHandler);
    }


    private void loadView(Class<? extends View> viewClass) {
        final Tab tab;
        View view = MVC.loadView(viewClass);

        if (view instanceof BlogView) tab = blogTab;
        else if (view instanceof AgoraView) tab = agoraTab;
        else if (view instanceof CalendarView) tab = calendarTab;
        else if (view instanceof DashboardView) tab = dashboardTab;
        else if (view instanceof IdentitiesView) tab = identitiesTab;
        else if (view instanceof WalletView) tab = walletTab;
        else throw new IllegalArgumentException("Navigation to " + viewClass + " is not supported");

        if (tab.getContent() != null && tab.getContent() instanceof ScrollPane) {
            ((ScrollPane) tab.getContent()).setContent(view.getRoot());
        } else {
            tab.setContent(view.getRoot());
        }
        pane.getSelectionModel().select(tab);
    }
}
