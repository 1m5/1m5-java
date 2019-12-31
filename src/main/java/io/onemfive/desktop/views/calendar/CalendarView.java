package io.onemfive.desktop.views.calendar;

import io.onemfive.desktop.views.InitializableView;

public class CalendarView extends InitializableView {

    public CalendarView() {
        model = new CalendarViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");

        LOG.info("Initialized.");
    }
}
