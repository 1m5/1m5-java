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

import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.network.NetworkState;
import io.onemfive.network.NetworkStateUpdateListener;
import io.onemfive.network.sensors.i2p.I2PSensor;
import io.onemfive.util.Res;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;

import static io.onemfive.desktop.util.FormBuilder.addSlideToggleButton;

public class TORSensorSettingsView extends ActivatableView implements NetworkStateUpdateListener {

    private GridPane pane;
    private int gridRow = 0;

    private ToggleButton routerEmbedded;

    public TORSensorSettingsView() {
        super();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;

        routerEmbedded = addSlideToggleButton(pane, gridRow, Res.get("settings.network.tor.routerEmbedded"));

        LOG.info("Initialized");
    }

    @Override
    protected void activate() {
        routerEmbedded.setSelected(false);
        routerEmbedded.setOnAction(e -> {
            LOG.info("routerEmbedded="+routerEmbedded.isSelected());
        });
        routerEmbedded.disableProperty().setValue(true);
    }

    @Override
    protected void deactivate() {
        routerEmbedded.setOnAction(null);
    }

    @Override
    public void notify(NetworkState networkState) {
        if(routerEmbedded!=null)
            routerEmbedded.setSelected("embedded".equals(networkState.params.get(I2PSensor.ROUTER_LOCATION)));
    }
}
