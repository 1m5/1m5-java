package io.onemfive.desktop.views.browser;

import io.onemfive.desktop.Navigation;
import io.onemfive.desktop.views.InitializableView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

public class BrowserView extends InitializableView {

    private final WebView browser = new WebView();
    private final WebEngine engine = browser.getEngine();
    private final WebHistory history = engine.getHistory();

    private HBox rootContainer;

    public BrowserView() {
        model = new BrowserViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        super.initialize();

        rootContainer = (HBox)root;

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);

        engine.getLoadWorker().stateProperty()
                .addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED) {
//                            root.getScene().setTitle(engine.getLocation());
                        }
                    }
                });
        engine.load("https://1m5.io");

        history.getEntries().addListener(new ListChangeListener<WebHistory.Entry>() {
             @Override
             public void onChanged(Change<? extends WebHistory.Entry> c) {
                 c.next();
                 for (WebHistory.Entry e : c.getRemoved()) {
                     LOG.info(e.getUrl());
                 }
                 for (WebHistory.Entry e : c.getAddedSubList()) {
                     LOG.info(e.getUrl());
                 }
             }
            }
        );
        history.go(0);

        rootContainer.getChildren().add(scrollPane);

        LOG.info("Initialized.");
    }



}
