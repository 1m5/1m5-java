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
package io.onemfive.desktop.views.settings.network.i2p;

import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.network.NetworkState;
import io.onemfive.network.NetworkStateUpdateListener;
import io.onemfive.network.sensors.i2p.I2PSensor;
import io.onemfive.util.Res;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import net.i2p.router.Router;

import static io.onemfive.desktop.util.FormBuilder.addCompactTopLabelTextField;
import static io.onemfive.desktop.util.FormBuilder.addSlideToggleButton;

public class I2PSensorSettingsView extends ActivatableView implements NetworkStateUpdateListener {

    private GridPane pane;
    private int gridRow = 0;

    public I2PSensorSettingsView() {
        super();
    }

    private ToggleButton hiddenMode;
    private ToggleButton routerEmbedded;
    private TextField sharePercentage;

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;

        routerEmbedded = addSlideToggleButton(pane, gridRow, Res.get("settings.network.i2p.routerEmbedded"));
        hiddenMode = addSlideToggleButton(pane, ++gridRow, Res.get("settings.network.i2p.hiddenMode"));
        sharePercentage = addCompactTopLabelTextField(pane, ++gridRow, Res.get("settings.network.i2p.sharePercentage"), String.valueOf(Router.DEFAULT_SHARE_PERCENTAGE)).second;

        // TODO: Register as NetworkConfig listener

        LOG.info("Initialized");
    }

    @Override
    protected void activate() {
        hiddenMode.setSelected(false);
        hiddenMode.setOnAction(e -> {
            LOG.info("hiddenMode="+hiddenMode.isSelected());
        });
        routerEmbedded.setSelected(true);
        routerEmbedded.setOnAction(e -> {
            LOG.info("routerEmbedded="+routerEmbedded.isSelected());
        });
        routerEmbedded.disableProperty().setValue(true);
    }

    @Override
    protected void deactivate() {
        hiddenMode.setOnAction(null);
        routerEmbedded.setOnAction(null);
    }

    @Override
    public void notify(NetworkState state) {
        if(hiddenMode!=null)
            hiddenMode.setSelected("true".equals(state.params.get(Router.PROP_HIDDEN)));
        if(routerEmbedded!=null)
            routerEmbedded.setSelected("embedded".equals(state.params.get(I2PSensor.ROUTER_LOCATION)));
        if(sharePercentage!=null)
            sharePercentage.setText((String)state.params.get(Router.PROP_BANDWIDTH_SHARE_PERCENTAGE));
    }

}
