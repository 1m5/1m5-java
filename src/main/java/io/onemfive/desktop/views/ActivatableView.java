package io.onemfive.desktop.views;

import javafx.scene.Node;

public abstract class ActivatableView<R extends Node, M> extends InitializableView<R, M> {

    public ActivatableView(M model) {
        super(model);
    }

    public ActivatableView() {
        this(null);
    }

    @Override
    protected void prepareInitialize() {
        if (root != null) {
            root.sceneProperty().addListener((ov, oldValue, newValue) -> {
                if (oldValue == null && newValue != null)
                    activate();
                else if (oldValue != null && newValue == null)
                    deactivate();
            });
        }
    }

    protected void activate() {
    }

    protected void deactivate() {
    }
}

