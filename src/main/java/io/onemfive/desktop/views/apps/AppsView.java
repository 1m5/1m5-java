package io.onemfive.desktop.views.apps;

import io.onemfive.desktop.views.InitializableView;

public class AppsView extends InitializableView {

    public AppsView(){
        model = new AppsViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}
