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
package io.onemfive.desktop.views.settings.network.tor;

import io.onemfive.data.NetworkPeer;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.TopicListener;
import io.onemfive.network.NetworkState;
import io.onemfive.network.sensors.tor.TORSensor;
import io.onemfive.util.Res;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;

import static io.onemfive.desktop.util.FormBuilder.addSlideToggleButton;
import static io.onemfive.desktop.util.FormBuilder.addTopLabelListView;

public class TORSensorSettingsView extends ActivatableView implements TopicListener {

    private GridPane pane;
    private int gridRow = 0;

    private ToggleButton routerEmbedded;

    private ObservableList<String> seeds = FXCollections.observableArrayList();
    private ListView seedsListView;

    public TORSensorSettingsView() {
        super();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;

        routerEmbedded = addSlideToggleButton(pane, gridRow, Res.get("settings.network.tor.routerEmbedded"));
        seedsListView = addTopLabelListView(pane, ++gridRow, Res.get("settings.network.tor.seedsLabel")).second;

        LOG.info("Initialized");
    }

    @Override
    protected void activate() {
        routerEmbedded.setSelected(false);
        routerEmbedded.setOnAction(e -> {
            LOG.info("routerEmbedded="+routerEmbedded.isSelected());
        });
        routerEmbedded.disableProperty().setValue(true);

        seedsListView.setItems(seeds);
    }

    @Override
    protected void deactivate() {
        routerEmbedded.setOnAction(null);
    }

    @Override
    public void modelUpdated(String name, Object object) {
        if(object instanceof NetworkState) {
            LOG.info("NetworkState received to update model.");
            NetworkState networkState = (NetworkState)object;
            if(routerEmbedded!=null)
                routerEmbedded.setSelected("embedded".equals(networkState.params.get(TORSensor.TOR_ROUTER_EMBEDDED)));
            if(networkState.seeds.size() > 0) {
                seeds.clear();
                for(NetworkPeer seed : networkState.seeds) {
                    seeds.add(seed.getDid().getPublicKey().getFingerprint());
                }
            }
        } else {
            LOG.warning("Received unknown model update with name: "+name);
        }
    }
}
