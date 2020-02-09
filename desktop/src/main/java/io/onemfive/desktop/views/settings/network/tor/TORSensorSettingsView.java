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
import io.onemfive.desktop.components.TitledGroupBg;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.desktop.views.TopicListener;
import io.onemfive.util.Res;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import static io.onemfive.desktop.util.FormBuilder.*;

public class TORSensorSettingsView extends ActivatableView implements TopicListener {

    private GridPane pane;
    private int gridRow = 0;

    private String torFingerprint = Res.get("settings.network.notKnownYet");
    private String torAddress = Res.get("settings.network.notKnownYet");

    private TextField torFingerprintTextField;
    private TextArea torAddressTextArea;


    public TORSensorSettingsView() {
        super();
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;

        TitledGroupBg localNodeGroup = addTitledGroupBg(pane, gridRow, 3, Res.get("settings.network.localNode"));
        GridPane.setColumnSpan(localNodeGroup, 1);
        torFingerprintTextField = addCompactTopLabelTextField(pane, ++gridRow, Res.get("settings.network.tor.fingerprintLabel"), torFingerprint, Layout.FIRST_ROW_DISTANCE).second;
        torAddressTextArea = addCompactTopLabelTextAreaWithText(pane, torAddress, ++gridRow, Res.get("settings.network.tor.addressLabel"), true).second;

        // Config
//        TitledGroupBg configGroup = addTitledGroupBg(pane, ++gridRow, 1, Res.get("settings.network.config"), Layout.FIRST_ROW_DISTANCE);
//        GridPane.setColumnSpan(configGroup, 1);

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
        if(object instanceof NetworkPeer) {
            NetworkPeer peer = (NetworkPeer)object;
            torFingerprint = peer.getDid().getPublicKey().getFingerprint();
            torAddress = peer.getDid().getPublicKey().getAddress();
            if(torFingerprintTextField !=null) {
                torFingerprintTextField.setText(torFingerprint);
            }
            if(torAddressTextArea !=null) {
                torAddressTextArea.setText(torAddress);
            }
        }
    }

}
