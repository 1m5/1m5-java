package io.onemfive.desktop.views;

import javafx.fxml.FXML;
import javafx.scene.Node;

import java.util.logging.Logger;

public abstract class BaseView implements View {

    protected final Logger LOG = Logger.getLogger(this.getClass().getName());

    @FXML
    protected Node root;

    @Override
    public Node getRoot() {
        return root;
    }

}
