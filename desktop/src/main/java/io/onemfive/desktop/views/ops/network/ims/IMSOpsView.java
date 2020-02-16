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
package io.onemfive.desktop.views.ops.network.ims;

import io.onemfive.core.ServiceStatus;
import io.onemfive.desktop.components.TitledGroupBg;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.TopicListener;
import io.onemfive.network.NetworkState;
import io.onemfive.util.Res;
import io.onemfive.util.StringUtil;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import static io.onemfive.desktop.util.FormBuilder.*;

public class IMSOpsView extends ActivatableView implements TopicListener {

    private GridPane pane;
    private int gridRow = 0;

    private ServiceStatus serviceStatus = ServiceStatus.NOT_INITIALIZED;
    private String serviceStatusField = StringUtil.capitalize(serviceStatus.name().toLowerCase().replace('_', ' '));
    private TextField serviceStatusTextField;

    private String imsFingerprint = Res.get("ops.network.notKnownYet");
    private String imsAddress = Res.get("ops.network.notKnownYet");

    private TextField imsFingerprintTextField;
    private TextArea imsAddressTextField;

    public IMSOpsView() {
        super();
    }

    public void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;

        TitledGroupBg statusGroup = addTitledGroupBg(pane, gridRow, 2, Res.get("ops.network.status"));
        GridPane.setColumnSpan(statusGroup, 1);
        serviceStatusTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.status.network"), serviceStatusField, Layout.FIRST_ROW_DISTANCE).second;

        TitledGroupBg localNodeGroup = addTitledGroupBg(pane, ++gridRow, 3, Res.get("ops.network.localNode"),Layout.FIRST_ROW_DISTANCE);
        GridPane.setColumnSpan(localNodeGroup, 1);
        imsFingerprintTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("ops.network.1m5.fingerprintLabel"), imsFingerprint, Layout.TWICE_FIRST_ROW_DISTANCE).second;
        imsAddressTextField = addCompactTopLabelTextAreaWithText(pane, imsAddress, ++gridRow, Res.get("ops.network.1m5.addressLabel"), true).second;

        LOG.info("Initialized");
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void modelUpdated(String name, Object object) {
        if(object instanceof NetworkState) {
            LOG.info("NetworkState received to update model.");
            NetworkState networkState = (NetworkState)object;
            if(this.serviceStatus != networkState.serviceStatus) {
                this.serviceStatus = networkState.serviceStatus;
                if(serviceStatusField != null) {
                    serviceStatusTextField.setText(StringUtil.capitalize(serviceStatus.name().toLowerCase().replace('_', ' ')));
                }
            }
            if(networkState.localPeer!=null) {
                imsFingerprint = networkState.localPeer.getDid().getPublicKey().getFingerprint();
                imsAddress = networkState.localPeer.getDid().getPublicKey().getAddress();
                if(imsFingerprintTextField !=null) {
                    imsFingerprintTextField.setText(imsFingerprint);
                }
                if(imsAddressTextField !=null) {
                    imsAddressTextField.setText(imsAddress);
                }
            }
        } else {
            LOG.warning("Received unknown model update with name: "+name);
        }
    }

}

