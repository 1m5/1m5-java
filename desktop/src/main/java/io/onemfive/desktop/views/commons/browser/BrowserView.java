/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.desktop.views.commons.browser;

import io.onemfive.desktop.Resources;
import io.onemfive.desktop.util.KeystrokeUtil;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.InitializableView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

public class BrowserView extends ActivatableView {

    private Scene scene;

    private final WebView webView = new WebView();
    private final WebEngine engine = webView.getEngine();
    private final WebHistory history = engine.getHistory();

    private EventHandler<KeyEvent> keyEventEventHandler;

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        super.initialize();

        BorderPane rootContainer = (BorderPane) root;
        engine.setJavaScriptEnabled(true);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(5));
        vBox.setSpacing(5);
        rootContainer.setCenter(vBox);

        HBox nav = new HBox();
        nav.setPadding(new Insets(5));
        nav.setSpacing(5);
        vBox.getChildren().add(nav);

        TextField url = new TextField();
        url.setText("1m5://1m5.1m5");
        HBox.setHgrow(url, Priority.ALWAYS);
        nav.getChildren().add(url);

        Button go = new Button("Go");
        go.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String path = url.getText();
                LOG.info("Go: "+path);
                URL newURL;
                if(path.startsWith("1m5:")) {
                    path = path.substring("1m5://".length(), path.indexOf(".1m5"));
                    newURL = Resources.class.getResource("/web/"+path);
                    path = newURL.toString();
                    if(!path.endsWith(".html") || !path.endsWith(".htm")) {
                        path += "/index.html";
                    }
                }
                engine.load(path);
                url.setText(url.getText());
                LOG.info(path+" loaded");
            }
        });
        nav.getChildren().add(go);

        Button refresh = new Button("Refresh");
        refresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                LOG.info("Refresh: "+url.getText());
                engine.reload();
                LOG.info(url.getText()+" refreshed");
            }
        });
        nav.getChildren().add(refresh);

        Button back = new Button("Back");
        back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                LOG.info("Back...");
                history.go(-1);
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
        scrollPane.setContent(webView);
        // TODO: Change this to auto-fill space
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        webView.setPrefWidth(1200);

        vBox.getChildren().add(scrollPane);

        engine.getLoadWorker().stateProperty()
                .addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED) {
                            String loc = engine.getLocation();
                            if(!loc.startsWith("file:")) {
                                url.setText(loc);
                            }
                        }
                    }
                });

        engine.load(Resources.WEB_INDEX.toExternalForm());

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

        keyEventEventHandler = keyEvent -> {
            if(KeystrokeUtil.isAltOrCtrlPressed(KeyCode.ENTER, keyEvent)) {
                refresh.fire();
            } else if(keyEvent.getCode()==KeyCode.ENTER) {
                go.fire();
            }
        };

        LOG.info("Initialized.");
    }

    @Override
    protected void activate() {
        if (root.getScene() != null) {
            scene = root.getScene();
            scene.addEventHandler(KeyEvent.KEY_RELEASED, keyEventEventHandler);
        }
    }

    @Override
    protected void deactivate() {
        if (scene != null)
            scene.removeEventHandler(KeyEvent.KEY_RELEASED, keyEventEventHandler);
    }
}
