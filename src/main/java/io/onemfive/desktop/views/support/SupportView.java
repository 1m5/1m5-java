package io.onemfive.desktop.views.support;

import io.onemfive.desktop.views.InitializableView;

public class SupportView extends InitializableView {

    public SupportView() {
        model = new SupportViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}
