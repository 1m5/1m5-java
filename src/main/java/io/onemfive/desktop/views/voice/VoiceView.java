package io.onemfive.desktop.views.voice;

import io.onemfive.desktop.views.InitializableView;

public class VoiceView extends InitializableView {

    public VoiceView() {
        model = new VoiceViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}
