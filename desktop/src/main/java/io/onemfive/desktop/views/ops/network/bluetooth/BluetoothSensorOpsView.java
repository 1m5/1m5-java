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
package io.onemfive.desktop.views.ops.network.bluetooth;

import io.onemfive.Cmd;
import io.onemfive.desktop.MVC;
import io.onemfive.desktop.components.TitledGroupBg;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.TopicListener;
import io.onemfive.network.NetworkState;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.network.sensors.bluetooth.BluetoothSensor;
import io.onemfive.util.Res;
import io.onemfive.util.StringUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;

import static io.onemfive.desktop.util.FormBuilder.*;
import static io.onemfive.desktop.util.FormBuilder.addCheckBox;

public class BluetoothSensorOpsView extends ActivatableView implements TopicListener {

    private GridPane pane;
    private int gridRow = 0;

    private SensorStatus sensorStatus = SensorStatus.NOT_INITIALIZED;
    private String sensorStatusField = StringUtil.capitalize(sensorStatus.name().toLowerCase().replace('_', ' '));
    private TextField sensorStatusTextField;

    private ToggleButton discoverButton;

    private String friendlyName = Res.get("ops.network.notKnownYet");
    private String address = Res.get("ops.network.notKnownYet");
    private TextField friendlynameTextField;
    private TextField addressTextField;

    public BluetoothSensorOpsView() {
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
        discoverButton = addSlideToggleButton(pane, ++gridRow, Res.get("ops.network.bluetooth.discoverButton"), Layout.TWICE_FIRST_ROW_DISTANCE);

        TitledGroupBg localNodeGroup = addTitledGroupBg(pane, ++gridRow, 3, Res.get("ops.network.localNode"), Layout.FIRST_ROW_DISTANCE);
        GridPane.setColumnSpan(localNodeGroup, 1);
        friendlynameTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.bluetooth.friendlyNameLabel"), friendlyName, Layout.TWICE_FIRST_ROW_DISTANCE).second;
        addressTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.bluetooth.addressLabel"), address).second;

        LOG.info("Initialized");
    }

    @Override
    protected void activate() {
        discoverButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                LOG.info("powerButton=" + discoverButton.isSelected());
                if (discoverButton.isSelected()) {
                    MVC.execute(new Runnable() {
                        @Override
                        public void run() {
                            Cmd.startBluetoothDiscovery();
                        }
                    });
                } else {
                    MVC.execute(new Runnable() {
                        @Override
                        public void run() {
                            Cmd.stopBluetoothDiscovery();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void deactivate() {
        discoverButton.setOnAction(null);
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
            if(sensorStatus==SensorStatus.NETWORK_CONNECTED) {
                if (networkState.localPeer != null) {
                    friendlyName = networkState.localPeer.getDid().getUsername();
                    if (friendlynameTextField != null) {
                        friendlynameTextField.setText(friendlyName);
                    }
                    address = networkState.localPeer.getDid().getPublicKey().getAddress();
                    if (addressTextField != null) {
                        addressTextField.setText(address);
                    }
                }
            }
        } else {
            LOG.warning("Received unknown model update with name: "+name);
        }
    }

}
