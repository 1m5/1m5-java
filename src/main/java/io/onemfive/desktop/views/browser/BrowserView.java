package io.onemfive.desktop.views.browser;

import io.onemfive.desktop.Navigation;
import io.onemfive.desktop.views.InitializableView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

public class BrowserView extends InitializableView {

    private final WebView browser = new WebView();
    private final WebEngine engine = browser.getEngine();
    private final WebHistory history = engine.getHistory();

    private VBox rootContainer;
    private HBox nav;
    private String lastUrl;

    public BrowserView() {
        model = new BrowserViewModel();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        super.initialize();

        rootContainer = (VBox)root;
        rootContainer.setPadding(new Insets(5));
        rootContainer.setSpacing(5);

        nav = new HBox();
        nav.setPadding(new Insets(5));
        nav.setSpacing(5);
        rootContainer.getChildren().add(nav);

        TextField url = new TextField();
        url.setText("https://1m5.io");
        nav.getChildren().add(url);

        Button go = new Button("Go");
        go.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                LOG.info("Go: "+url.getText());
                engine.load(url.getText());
                LOG.info(url.getText()+" loaded");
            }
        });
        nav.getChildren().add(go);

        Button refresh = new Button("Refresh");
        refresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                LOG.info("Refresh: "+url.getText());
                engine.reload();
                LOG.info(url.getText()+" resfreshed");
            }
        });
        nav.getChildren().add(refresh);

        Button back = new Button("Back");
        back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                LOG.info("Back...");
                history.go(history.getCurrentIndex() -1);
                LOG.info("Backed");
            }
        });
        nav.getChildren().add(back);

        Button stop = new Button("Stop");
        stop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                LOG.info("Cancel...");
                engine.getLoadWorker().cancel();
                LOG.info("Canceled");
            }
        });
        nav.getChildren().add(stop);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);
        rootContainer.getChildren().add(scrollPane);

        engine.getLoadWorker().stateProperty()
                .addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED) {
                            url.setText(engine.getLocation());
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

        LOG.info("Initialized.");
    }

}
