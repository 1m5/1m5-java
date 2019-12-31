package io.onemfive.desktop.views.dashboard;

import io.onemfive.desktop.views.InitializableView;

public class DashboardView extends InitializableView {

    public DashboardView() {
        model = new DashboardViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}

