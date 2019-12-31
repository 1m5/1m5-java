package io.onemfive.desktop.views.video;

import io.onemfive.desktop.views.InitializableView;

public class VideoView extends InitializableView {

    public VideoView() {
        model = new VideoViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}
