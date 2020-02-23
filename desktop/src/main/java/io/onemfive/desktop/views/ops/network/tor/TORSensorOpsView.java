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
package io.onemfive.desktop.views.ops.network.tor;

import io.onemfive.Cmd;
import io.onemfive.desktop.MVC;
import io.onemfive.desktop.components.HyperlinkWithIcon;
import io.onemfive.desktop.components.TitledGroupBg;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.TopicListener;
import io.onemfive.desktop.views.ViewPath;
import io.onemfive.desktop.views.commons.CommonsView;
import io.onemfive.desktop.views.commons.browser.BrowserView;
import io.onemfive.desktop.views.home.HomeView;
import io.onemfive.network.NetworkState;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.network.sensors.tor.TORSensor;
import io.onemfive.util.Res;
import io.onemfive.util.StringUtil;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;

import static io.onemfive.desktop.util.FormBuilder.*;

public class TORSensorOpsView extends ActivatableView implements TopicListener {

    private GridPane pane;
    private int gridRow = 0;

    private SensorStatus sensorStatus = SensorStatus.NOT_INITIALIZED;
    private String sensorStatusField = StringUtil.capitalize(sensorStatus.name().toLowerCase().replace('_', ' '));
    private TextField sensorStatusTextField;

    private ToggleButton powerButton;
    private CheckBox hardStop;

    private String address = Res.get("ops.network.notKnownYet");
    private TextField addressTextField;

    private String virtualPort = Res.get("ops.network.notKnownYet");
    private TextField virtualPortTextField;

    private String targetPort = Res.get("ops.network.notKnownYet");
    private TextField targetPortTextField;

    private String hiddenServiceURL = "http://127.0.0.1";
    private HyperlinkWithIcon hiddenServiceHyperLink;

    public TORSensorOpsView() {
        super();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;

        TitledGroupBg statusGroup = addTitledGroupBg(pane, gridRow, 2, Res.get("ops.network.status"));
        GridPane.setColumnSpan(statusGroup, 1);
        sensorStatusTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.status.sensor"), sensorStatusField, Layout.FIRST_ROW_DISTANCE).second;

        TitledGroupBg sensorPower = addTitledGroupBg(pane, ++gridRow, 3, Res.get("ops.network.sensorControls"),Layout.FIRST_ROW_DISTANCE);
        GridPane.setColumnSpan(sensorPower, 1);
        powerButton = addSlideToggleButton(pane, ++gridRow, Res.get("ops.network.sensorPowerButton"), Layout.TWICE_FIRST_ROW_DISTANCE);
        hardStop = addCheckBox(pane, ++gridRow, Res.get("ops.network.hardStop"));

        TitledGroupBg localNodeGroup = addTitledGroupBg(pane, ++gridRow, 5, Res.get("ops.network.localNode"),Layout.FIRST_ROW_DISTANCE);
        GridPane.setColumnSpan(localNodeGroup, 1);
        addressTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.tor.addressLabel"), address, Layout.TWICE_FIRST_ROW_DISTANCE).second;
        virtualPortTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.tor.vPortLabel"), virtualPort).second;
        targetPortTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.tor.tPortLabel"), targetPort).second;
        hiddenServiceHyperLink = addHyperlinkWithIcon(pane, ++gridRow, Res.get("ops.network.tor.hiddenServiceTestLabel"), hiddenServiceURL);
        GridPane.setColumnSpan(hiddenServiceHyperLink, 2);
        hiddenServiceHyperLink.disableProperty().setValue(true);

        LOG.info("Initialized");
    }

    @Override
    protected void activate() {
        // Power Button
        updateComponents();
        powerButton.setOnAction(e -> {
            LOG.info("powerButton="+powerButton.isSelected());
            if(powerButton.isSelected()) {
                MVC.execute(new Runnable() {
                    @Override
                    public void run() {
                        Cmd.startSensor(TORSensor.class.getName());
                    }
                });
            } else {
                MVC.execute(new Runnable() {
                    @Override
                    public void run() {
                        Cmd.stopSensor(TORSensor.class.getName(), hardStop.isSelected());
                    }
                });
            }
            powerButton.disableProperty().setValue(true);
            hardStop.disableProperty().setValue(true);
        });
    }

    @Override
    protected void deactivate() {
        powerButton.setOnAction(null);
    }

    @Override
    public void modelUpdated(String name, Object object) {
        if(object instanceof NetworkState) {
            LOG.info("NetworkState received to update model.");
            NetworkState networkState = (NetworkState)object;
            if(this.sensorStatus != networkState.sensorStatus) {
                this.sensorStatus = networkState.sensorStatus;
                if(sensorStatusField != null) {
                    sensorStatusTextField.setText(StringUtil.capitalize(sensorStatus.name().toLowerCase().replace('_', ' ')));
                }
            }
            if(networkState.localPeer!=null) {
                address = networkState.localPeer.getDid().getPublicKey().getAddress();
                if(addressTextField!=null)
                    addressTextField.setText(address);
            }
            if(networkState.virtualPort != null) {
                virtualPort = String.valueOf(networkState.virtualPort);
                if(virtualPortTextField !=null) {
                    virtualPortTextField.setText(virtualPort);
                }
            }
            if(networkState.targetPort != null) {
                targetPort = String.valueOf(networkState.targetPort);
                if(targetPortTextField !=null) {
                    targetPortTextField.setText(targetPort);
                }
                hiddenServiceURL = "http://127.0.0.1:"+targetPort+"/test";
                if(hiddenServiceHyperLink!=null) {
                    hiddenServiceHyperLink.setOnAction(e -> MVC.navigation.navigateTo(ViewPath.to(HomeView.class, CommonsView.class, BrowserView.class), hiddenServiceURL));
                    hiddenServiceHyperLink.disableProperty().set(false);
                }
            }
            updateComponents();
        } else {
            LOG.warning("Received unknown model update with name: "+name);
        }
    }

    private void updateComponents() {
        if(sensorStatus==SensorStatus.NOT_INITIALIZED
                || sensorStatus==SensorStatus.NETWORK_PORT_CONFLICT
                || sensorStatus==SensorStatus.SHUTDOWN
                || sensorStatus==SensorStatus.GRACEFULLY_SHUTDOWN) {
            powerButton.setSelected(false);
            powerButton.disableProperty().setValue(false);
            hardStop.setVisible(false);
        } else if(sensorStatus==SensorStatus.INITIALIZING
                || sensorStatus==SensorStatus.WAITING
                || sensorStatus==SensorStatus.STARTING
                || sensorStatus==SensorStatus.NETWORK_CONNECTING) {
            powerButton.setSelected(true);
            powerButton.disableProperty().setValue(true);
            hardStop.setVisible(false);
        } else if(sensorStatus==SensorStatus.SHUTTING_DOWN
                || sensorStatus==SensorStatus.GRACEFULLY_SHUTTING_DOWN
                || sensorStatus==SensorStatus.UNREGISTERED
                || sensorStatus==SensorStatus.NETWORK_UNAVAILABLE
                || sensorStatus==SensorStatus.ERROR
                || sensorStatus==SensorStatus.NETWORK_ERROR) {
            powerButton.setSelected(false);
            powerButton.disableProperty().setValue(true);
            hardStop.setVisible(true);
            hardStop.disableProperty().setValue(true);
        } else if(sensorStatus==SensorStatus.NETWORK_CONNECTED) {
            powerButton.setSelected(true);
            powerButton.disableProperty().setValue(false);
            hardStop.setVisible(true);
            hardStop.disableProperty().setValue(false);
        }
    }

}
