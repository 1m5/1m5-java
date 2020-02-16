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

import io.onemfive.desktop.components.TitledGroupBg;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.TopicListener;
import io.onemfive.network.NetworkState;
import io.onemfive.network.sensors.SensorStatus;
import io.onemfive.util.Res;
import io.onemfive.util.StringUtil;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import net.i2p.router.Router;

import static io.onemfive.desktop.util.FormBuilder.*;

public class I2PSensorOpsView extends ActivatableView implements TopicListener {

    private GridPane pane;
    private int gridRow = 0;

    private SensorStatus sensorStatus = SensorStatus.NOT_INITIALIZED;
    private String sensorStatusField = StringUtil.capitalize(sensorStatus.name().toLowerCase().replace('_', ' '));
    private TextField sensorStatusTextField;

    private String i2PFingerprint = Res.get("ops.network.notKnownYet");
    private TextField i2PFingerprintTextField;

    private String i2PAddress = Res.get("ops.network.notKnownYet");
    private TextArea i2PAddressTextArea;

    private String i2PIPv6Address = Res.get("ops.network.notKnownYet");
    private TextField i2PIPv6AddressTextField;

    private String port = Res.get("ops.network.notKnownYet");
    private TextField portTextField;

    private String maxConnections = Res.get("ops.network.notKnownYet");
    private TextField maxConnectionsTextField;

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

        // Local Node
        TitledGroupBg localNodeGroup = addTitledGroupBg(pane, ++gridRow, 6, Res.get("ops.network.localNode"),Layout.FIRST_ROW_DISTANCE);
        GridPane.setColumnSpan(localNodeGroup, 1);
        i2PFingerprintTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.i2p.fingerprintLabel"), i2PFingerprint, Layout.TWICE_FIRST_ROW_DISTANCE).second;
        i2PAddressTextArea = addCompactTopLabelTextAreaWithText(pane, i2PAddress, ++gridRow, Res.get("ops.network.i2p.addressLabel"), true).second;
        i2PIPv6AddressTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.i2p.ipv6Label"), i2PIPv6Address).second;
        portTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.i2p.portLabel"), port).second;
        maxConnectionsTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.i2p.maxConnectionsLabel"), maxConnections).second;

        LOG.info("Initialized");
    }

    @Override
    protected void activate() {

    }

    @Override
    protected void deactivate() {

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
                i2PAddress = networkState.localPeer.getDid().getPublicKey().getAddress();
                i2PFingerprint = networkState.localPeer.getDid().getPublicKey().getFingerprint();
                if(i2PAddressTextArea!=null)
                    i2PAddressTextArea.setText(i2PAddress);
                if(i2PFingerprintTextField!=null)
                    i2PFingerprintTextField.setText(i2PFingerprint);
            }
            if(networkState.port != null) {
                port = String.valueOf(networkState.port);
                if(portTextField!=null) {
                    portTextField.setText(port);
                }
            }
            if(networkState.params.get("i2np.lastIPv6")!=null) {
                i2PIPv6Address = (String)networkState.params.get("i2np.lastIPv6");
                if(i2PIPv6AddressTextField!=null)
                    i2PIPv6AddressTextField.setText(i2PIPv6Address);
            }
            if(networkState.params.get("i2np.udp.maxConnections")!=null) {
                maxConnections = (String)networkState.params.get("i2np.udp.maxConnections");
                if(maxConnectionsTextField!=null)
                    maxConnectionsTextField.setText(maxConnections);
            }
        } else {
            LOG.warning("Received unknown model update with name: "+name);
        }
    }

}
