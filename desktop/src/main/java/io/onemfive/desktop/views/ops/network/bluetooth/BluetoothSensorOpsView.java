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

import io.onemfive.data.NetworkPeer;
import io.onemfive.desktop.components.TitledGroupBg;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.TopicListener;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.network.sensors.SensorStatusListener;
import io.onemfive.util.Res;
import io.onemfive.util.StringUtil;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import static io.onemfive.desktop.util.FormBuilder.addCompactTopLabelTextField;
import static io.onemfive.desktop.util.FormBuilder.addTitledGroupBg;

public class BluetoothSensorOpsView extends ActivatableView implements SensorStatusListener, TopicListener {

    private GridPane pane;
    private int gridRow = 0;

    private String friendlyName = Res.get("ops.network.notKnownYet");
    private String address = Res.get("ops.network.notKnownYet");
    private TextField friendlynameTextField;
    private TextField addressTextField;

    private SensorStatus sensorStatus = SensorStatus.NOT_INITIALIZED;
    private String sensorStatusField = StringUtil.capitalize(sensorStatus.name().toLowerCase().replace('_', ' '));
    private TextField sensorStatusTextField;

    public BluetoothSensorOpsView() {
        super();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;

        TitledGroupBg localNodeGroup = addTitledGroupBg(pane, gridRow, 3, Res.get("ops.network.localNode"));
        GridPane.setColumnSpan(localNodeGroup, 1);
        friendlynameTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.bluetooth.friendlyNameLabel"), friendlyName, Layout.FIRST_ROW_DISTANCE).second;
        addressTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.bluetooth.addressLabel"), address).second;

        TitledGroupBg statusGroup = addTitledGroupBg(pane, ++gridRow, 2, Res.get("ops.network.status"), Layout.FIRST_ROW_DISTANCE);
        GridPane.setColumnSpan(statusGroup, 1);
        sensorStatusTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.status.sensor"), sensorStatusField, Layout.TWICE_FIRST_ROW_DISTANCE).second;

        LOG.info("Initialized");
    }

    @Override
    protected void activate() {

    }

    @Override
    protected void deactivate() {

    }

    @Override
    public void statusUpdated(SensorStatus sensorStatus) {
        if(this.sensorStatus != sensorStatus) {
            this.sensorStatus = sensorStatus;
            if(sensorStatusField != null) {
                sensorStatusTextField.setText(StringUtil.capitalize(sensorStatus.name().toLowerCase().replace('_', ' ')));
            }
        }
    }

    @Override
    public void modelUpdated(String name, Object object) {
        if(object instanceof NetworkPeer) {
            NetworkPeer peer = (NetworkPeer)object;
            friendlyName = peer.getDid().getUsername();
            address = peer.getDid().getPublicKey().getAddress();
            if(friendlynameTextField !=null) {
                friendlynameTextField.setText(friendlyName);
            }
            if(addressTextField !=null) {
                addressTextField.setText(address);
            }
        }
    }

}
