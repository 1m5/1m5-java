package io.onemfive.desktop.views;

import javafx.fxml.FXML;
import javafx.scene.Node;

import java.util.logging.Logger;

public abstract class BaseView<R extends Node, Model> implements View {

    protected final Logger LOG = Logger.getLogger(this.getClass().getName());

    @FXML
    protected R root;
    protected Model model;

    public BaseView() {
        this(null);
    }

    public BaseView(Model model) {
        this.model = model;
    }

    public BaseView(R root, Model model) {
        this.root = root;
        this.model = model;
    }

    @Override
    public R getRoot() {
        return root;
    }

    public void setRoote(R root) {
        this.root = root;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }
}
