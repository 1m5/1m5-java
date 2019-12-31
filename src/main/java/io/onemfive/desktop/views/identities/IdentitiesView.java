package io.onemfive.desktop.views.identities;

import io.onemfive.desktop.views.InitializableView;

public class IdentitiesView extends InitializableView {

    public IdentitiesView() {
        model = new IdentitiesViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}
