package io.onemfive.desktop.views.dao;

import io.onemfive.desktop.views.InitializableView;

public class DaoView extends InitializableView {

    public DaoView() {
        model = new DaoViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}
