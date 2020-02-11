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

import io.onemfive.desktop.user.Preferences;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.network.NetworkConfig;
import io.onemfive.network.sensors.i2p.I2PSensor;
import io.onemfive.util.Res;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import net.i2p.router.Router;

import static io.onemfive.desktop.util.FormBuilder.addSlideToggleButton;

public class I2PSensorSettingsView extends ActivatableView  {

    private NetworkConfig config;

    private GridPane pane;
    private int gridRow = 0;

    public I2PSensorSettingsView() {
        super();
    }

    private ToggleButton hiddenMode;
    private ToggleButton routerEmbedded;

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;

        routerEmbedded = addSlideToggleButton(pane, ++gridRow, Res.get("settings.network.i2p.routerEmbedded"));
        hiddenMode = addSlideToggleButton(pane, gridRow, Res.get("settings.network.i2p.hiddenMode"));

        // TODO: Request NetworkConfig for I2P Sensor.

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

    public void setConfig(NetworkConfig config) {
        this.hiddenMode.setSelected("true".equals(config.params.get(Router.PROP_HIDDEN)));
        this.routerEmbedded.setSelected("embedded".equals(config.params.get(I2PSensor.ROUTER_LOCATION)));
        config.params.get(Router.PROP_BANDWIDTH_SHARE_PERCENTAGE);
    }

}
