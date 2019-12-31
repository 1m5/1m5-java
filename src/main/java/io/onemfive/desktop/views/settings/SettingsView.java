package io.onemfive.desktop.views.settings;

import io.onemfive.desktop.views.InitializableView;

public class SettingsView extends InitializableView {

    public SettingsView() {
        model = new SettingsViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}
