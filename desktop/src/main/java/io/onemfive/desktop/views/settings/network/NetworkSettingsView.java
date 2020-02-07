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
package io.onemfive.desktop.views.settings.network;

import io.onemfive.desktop.components.AutoTooltipButton;
import io.onemfive.desktop.components.AutoTooltipLabel;
import io.onemfive.desktop.components.InputTextField;
import io.onemfive.desktop.components.TitledGroupBg;
import io.onemfive.desktop.components.overlays.popups.Popup;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.util.Res;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class NetworkSettingsView extends ActivatableView {

    @FXML
    TitledGroupBg p2pHeader, btcHeader;
    @FXML
    Label btcNodesLabel, bitcoinNodesLabel, localhostBtcNodeInfoLabel;
    @FXML
    InputTextField btcNodesInputTextField;
    @FXML
    TextField onionAddress, totalTrafficTextField;
    @FXML
    Label p2PPeersLabel, bitcoinPeersLabel;
    @FXML
    CheckBox useTorForBtcJCheckBox;
    @FXML
    RadioButton useProvidedNodesRadio, useCustomNodesRadio, usePublicNodesRadio;
    @FXML
    Label reSyncSPVChainLabel;
    @FXML
    AutoTooltipButton reSyncSPVChainButton, openTorSettingsButton;

    public NetworkSettingsView() {
        super();
    }

    public void initialize() {
        btcHeader.setText(Res.get("settings.net.btcHeader"));
        p2pHeader.setText(Res.get("settings.net.p2pHeader"));
        onionAddress.setPromptText(Res.get("settings.net.onionAddressLabel"));
        btcNodesLabel.setText(Res.get("settings.net.btcNodesLabel"));
        bitcoinPeersLabel.setText(Res.get("settings.net.bitcoinPeersLabel"));
        useTorForBtcJCheckBox.setText(Res.get("settings.net.useTorForBtcJLabel"));
        bitcoinNodesLabel.setText(Res.get("settings.net.bitcoinNodesLabel"));
        localhostBtcNodeInfoLabel.setText(Res.get("settings.net.localhostBtcNodeInfo"));
        useProvidedNodesRadio.setText(Res.get("settings.net.useProvidedNodesRadio"));
        useCustomNodesRadio.setText(Res.get("settings.net.useCustomNodesRadio"));
        usePublicNodesRadio.setText(Res.get("settings.net.usePublicNodesRadio"));
        reSyncSPVChainLabel.setText(Res.get("settings.net.reSyncSPVChainLabel"));
        reSyncSPVChainButton.updateText(Res.get("settings.net.reSyncSPVChainButton"));
        p2PPeersLabel.setText(Res.get("settings.net.p2PPeersLabel"));
        totalTrafficTextField.setPromptText(Res.get("settings.net.totalTrafficLabel"));
        openTorSettingsButton.updateText(Res.get("settings.net.openTorSettingsButton"));

        GridPane.setMargin(bitcoinPeersLabel, new Insets(4, 0, 0, 0));
        GridPane.setValignment(bitcoinPeersLabel, VPos.TOP);

        GridPane.setMargin(p2PPeersLabel, new Insets(4, 0, 0, 0));
        GridPane.setValignment(p2PPeersLabel, VPos.TOP);

    }

    @Override
    public void activate() {


    }

    @Override
    public void deactivate() {

    }

    private void showShutDownPopup() {
        new Popup()
                .information(Res.get("settings.net.needRestart"))
                .closeButtonText(Res.get("shared.cancel"))
                .useShutDownButton()
                .show();
    }

}

