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
package io.onemfive.desktop.views.ops.network.i2p;

import io.onemfive.Cmd;
import io.onemfive.desktop.MVC;
import io.onemfive.desktop.components.TitledGroupBg;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.TopicListener;
import io.onemfive.network.NetworkState;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.network.sensors.i2p.I2PSensor;
import io.onemfive.network.sensors.tor.TORSensor;
import io.onemfive.util.Res;
import io.onemfive.util.StringUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;

import static io.onemfive.desktop.util.FormBuilder.*;

public class I2PSensorOpsView extends ActivatableView implements TopicListener {

    private GridPane pane;
    private int gridRow = 0;

    private SensorStatus sensorStatus = SensorStatus.NOT_INITIALIZED;
    private String sensorStatusField = StringUtil.capitalize(sensorStatus.name().toLowerCase().replace('_', ' '));
    private TextField sensorStatusTextField;

    private ToggleButton powerButton;
    private CheckBox hardStop;

    private String i2PFingerprint = Res.get("ops.network.notKnownYet");
    private TextField i2PFingerprintTextField;

    private String i2PAddress = Res.get("ops.network.notKnownYet");
    private TextArea i2PAddressTextArea;

    private String i2PIPv6Address = Res.get("ops.network.notKnownYet");
    private TextField i2PIPv6AddressTextField;

    private String port = Res.get("ops.network.notKnownYet");
    private TextField portTextField;

    public I2PSensorOpsView() {
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

        TitledGroupBg localNodeGroup = addTitledGroupBg(pane, ++gridRow, 6, Res.get("ops.network.localNode"),Layout.FIRST_ROW_DISTANCE);
        GridPane.setColumnSpan(localNodeGroup, 1);
        i2PFingerprintTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.i2p.fingerprintLabel"), i2PFingerprint, Layout.TWICE_FIRST_ROW_DISTANCE).second;
        i2PAddressTextArea = addCompactTopLabelTextAreaWithText(pane, i2PAddress, ++gridRow, Res.get("ops.network.i2p.addressLabel"), true).second;
        i2PIPv6AddressTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.i2p.ipv6Label"), i2PIPv6Address).second;
        portTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.i2p.portLabel"), port).second;

        LOG.info("Initialized");
    }

    @Override
    protected void activate() {
        updateComponents();
        powerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                LOG.info("powerButton=" + powerButton.isSelected());
                if (powerButton.isSelected()) {
                    MVC.execute(new Runnable() {
                        @Override
                        public void run() {
                            Cmd.startSensor(I2PSensor.class.getName());
                        }
                    });
                } else {
                    reset();
                    MVC.execute(new Runnable() {
                        @Override
                        public void run() {
                            Cmd.stopSensor(I2PSensor.class.getName(), hardStop.isSelected());
                        }
                    });
                }
                powerButton.disableProperty().setValue(true);
                hardStop.disableProperty().setValue(true);
            };
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
            if(sensorStatus==SensorStatus.NETWORK_CONNECTED) {
                if (networkState.localPeer != null) {
                    i2PAddress = networkState.localPeer.getDid().getPublicKey().getAddress();
                    i2PFingerprint = networkState.localPeer.getDid().getPublicKey().getFingerprint();
                    if (i2PAddressTextArea != null)
                        i2PAddressTextArea.setText(i2PAddress);
                    if (i2PFingerprintTextField != null)
                        i2PFingerprintTextField.setText(i2PFingerprint);
                }
                if (networkState.virtualPort != null) {
                    port = String.valueOf(networkState.virtualPort);
                    if (portTextField != null) {
                        portTextField.setText(port);
                    }
                }
                if (networkState.params.get("i2np.lastIPv6") != null) {
                    i2PIPv6Address = (String) networkState.params.get("i2np.lastIPv6");
                    if (i2PIPv6AddressTextField != null)
                        i2PIPv6AddressTextField.setText(i2PIPv6Address);
                }
            }
            updateComponents();
        } else {
            LOG.warning("Received unknown model update with name: "+name);
        }
    }

    private void reset() {
        i2PAddress = Res.get("ops.network.notKnownYet");
        i2PFingerprint = Res.get("ops.network.notKnownYet");
        if(i2PAddressTextArea!=null)
            i2PAddressTextArea.setText(i2PAddress);
        if(i2PFingerprintTextField!=null)
            i2PFingerprintTextField.setText(i2PFingerprint);
        port = Res.get("ops.network.notKnownYet");
        if(portTextField!=null) {
            portTextField.setText(port);
        }
        i2PIPv6Address = Res.get("ops.network.notKnownYet");
        if(i2PIPv6AddressTextField!=null)
            i2PIPv6AddressTextField.setText(i2PIPv6Address);
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
