package io.onemfive.desktop.views;

import io.onemfive.desktop.Navigation;
import javafx.fxml.FXML;
import javafx.scene.Node;

import java.util.logging.Logger;

public abstract class BaseView implements View {

    protected final Logger LOG = Logger.getLogger(this.getClass().getName());

    @FXML
    protected Node root;
    protected Navigation navigation;
    protected Navigation.Listener navigationListener;

    public BaseView() {}

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void setRoot(Node root) {
        this.root = root;
    }

    @Override
    public void setNavigation(Navigation navigation) {
        this.navigation = navigation;
    }

    @Override
    public Navigation getNavigation() {
        return navigation;
    }
}
