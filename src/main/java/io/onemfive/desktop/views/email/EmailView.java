package io.onemfive.desktop.views.email;

import io.onemfive.desktop.views.InitializableView;

public class EmailView extends InitializableView {

    public EmailView() {
        model = new EmailViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}
