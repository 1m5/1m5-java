package io.onemfive.desktop.views.messenger;

import io.onemfive.desktop.views.InitializableView;

public class MessengerView extends InitializableView {

    public MessengerView() {
        model = new MessengerViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }

}
