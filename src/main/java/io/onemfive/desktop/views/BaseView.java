package io.onemfive.desktop.views;

import javafx.fxml.FXML;
import javafx.scene.Node;

import java.util.logging.Logger;

public abstract class BaseView<R extends Node, M> implements View {

    protected final Logger LOG = Logger.getLogger(this.getClass().getName());

    @FXML
    protected R root;
    protected M model;

    public BaseView(M model) {
        this.model = model;
    }

    public BaseView() {
        this(null);
    }

    @Override
    public R getRoot() {
        return root;
    }

    public void setModel(M model) {
        this.model = model;
    }
}
