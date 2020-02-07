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
package io.onemfive.desktop.views.settings.about;

import io.onemfive.desktop.components.HyperlinkWithIcon;
import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.util.Res;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import static io.onemfive.desktop.util.FormBuilder.addCompactTopLabelTextField;
import static io.onemfive.desktop.util.FormBuilder.addHyperlinkWithIcon;
import static io.onemfive.desktop.util.FormBuilder.addLabel;
import static io.onemfive.desktop.util.FormBuilder.addTitledGroupBg;

public class AboutView extends ActivatableView {

    private int gridRow = 0;

    public AboutView() {
        super();
    }

    @Override
    public void initialize() {
        LOG.info("Initializing...");
        GridPane pane = (GridPane)root;
        addTitledGroupBg(pane, gridRow, 4, Res.get("setting.about.aboutBisq"));

        Label label = addLabel(pane, gridRow, Res.get("setting.about.about"), Layout.TWICE_FIRST_ROW_DISTANCE);
        label.setWrapText(true);
        GridPane.setColumnSpan(label, 2);
        GridPane.setHalignment(label, HPos.LEFT);
        HyperlinkWithIcon hyperlinkWithIcon = addHyperlinkWithIcon(pane, ++gridRow, Res.get("setting.about.web"), "https://1m5.io");
        GridPane.setColumnSpan(hyperlinkWithIcon, 2);
        hyperlinkWithIcon = addHyperlinkWithIcon(pane, ++gridRow, Res.get("setting.about.code"), "https://github.com/1m5");
        GridPane.setColumnSpan(hyperlinkWithIcon, 2);
        hyperlinkWithIcon = addHyperlinkWithIcon(pane, ++gridRow, Res.get("setting.about.license"), "https://github.com/1m5/1m5/blob/master/LICENSE");
        GridPane.setColumnSpan(hyperlinkWithIcon, 2);

        addTitledGroupBg(pane, ++gridRow, 2, Res.get("setting.about.support"), Layout.GROUP_DISTANCE);

        label = addLabel(pane, gridRow, Res.get("setting.about.def"), Layout.TWICE_FIRST_ROW_AND_GROUP_DISTANCE);
        label.setWrapText(true);
        GridPane.setColumnSpan(label, 2);
        GridPane.setHalignment(label, HPos.LEFT);
        hyperlinkWithIcon = addHyperlinkWithIcon(pane, ++gridRow, Res.get("setting.about.contribute"), "https://1m5.io/collaborate.html");
        GridPane.setColumnSpan(hyperlinkWithIcon, 2);


        label.setWrapText(true);
        GridPane.setHalignment(label, HPos.LEFT);

        addTitledGroupBg(pane, ++gridRow, 2, Res.get("setting.about.versionDetails"), Layout.GROUP_DISTANCE);
        addCompactTopLabelTextField(pane, gridRow, Res.get("setting.about.version"), System.getProperty("1m5.version"), Layout.TWICE_FIRST_ROW_AND_GROUP_DISTANCE);

        addTitledGroupBg(pane, ++gridRow, 20, Res.get("setting.about.shortcuts"), Layout.GROUP_DISTANCE);

        // basics
        addCompactTopLabelTextField(pane, gridRow, Res.get("setting.about.shortcuts.menuNav"),
                Res.get("setting.about.shortcuts.menuNav.value"),
                Layout.TWICE_FIRST_ROW_AND_GROUP_DISTANCE);

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.close"),
                Res.get("setting.about.shortcuts.close.value", "q", "w"));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.closePopup"),
                Res.get("setting.about.shortcuts.closePopup.value"));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.chatSendMsg"),
                Res.get("setting.about.shortcuts.chatSendMsg.value"));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.openDispute"),
                Res.get("setting.about.shortcuts.openDispute.value",
                        Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "o")));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.walletDetails"),
                Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "j"));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.openEmergencyBtcWalletTool"),
                Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "e"));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.openEmergencyBsqWalletTool"),
                Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "b"));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.showDisputeStatistics"),
                Res.get("setting.about.shortcuts.showDisputeStatistics.value",
                        Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "l")));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.showTorLogs"),
                Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "t"));

        // special
        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.removeStuckTrade"),
                Res.get("setting.about.shortcuts.removeStuckTrade.value",
                        Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "y")));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.manualPayoutTxWindow"),
                Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "g"));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.reRepublishAllGovernanceData"),
                Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "h"));

        // for arbitrators
        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.registerArbitrator"),
                Res.get("setting.about.shortcuts.registerArbitrator.value",
                        Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "n")));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.registerMediator"),
                Res.get("setting.about.shortcuts.registerMediator.value",
                        Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "d")));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.reOpenDispute"),
                Res.get("setting.about.shortcuts.reOpenDispute.value",
                        Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "u")));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.openSignPaymentAccountsWindow"),
                Res.get("setting.about.shortcuts.openSignPaymentAccountsWindow.value",
                        Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "s")));

        // only for maintainers
        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.sendAlertMsg"),
                Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "m"));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.sendFilter"),
                Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "f"));

        addCompactTopLabelTextField(pane, ++gridRow, Res.get("setting.about.shortcuts.sendPrivateNotification"),
                Res.get("setting.about.shortcuts.sendPrivateNotification.value",
                        Res.get("setting.about.shortcuts.ctrlOrAltOrCmd", "r")));

        // Not added:
        // allTradesWithReferralId, allOffersWithReferralId -> ReferralId is not used yet
        // revert tx -> not tested well, high risk
        // debug window -> not maintained, only for devs working on trade protocol relevant
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }

}

