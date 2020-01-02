package io.onemfive.desktop.views;

import io.onemfive.desktop.Navigation;
import javafx.scene.Node;

public interface View {
    void setRoot(Node node);
    Node getRoot();
    void setNavigation(Navigation navigation);
    Navigation getNavigation();
}
