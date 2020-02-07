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
package io.onemfive.desktop.views.personal.identities;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import io.onemfive.OneMFivePlatform;
import io.onemfive.core.keyring.KeyRingService;
import io.onemfive.data.*;
import io.onemfive.desktop.DesktopApp;
import io.onemfive.desktop.DesktopService;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.did.DIDService;
import io.onemfive.util.DLC;
import io.onemfive.util.Res;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

public class IdentitiesView extends ActivatableView {

    private ObservableList<String> identityAddresses = FXCollections.observableArrayList();

    private DID activeDID;

    public void updateActiveDID(DID activeDID) {
        this.activeDID = activeDID;
    }

    public void updateIdentities(List<DID> identities) {
        identityAddresses.clear();
        for(DID i : identities) {
            identityAddresses.add(i.getUsername() + ": "+i.getPublicKey().getFingerprint());
        }
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        StackPane pane = (StackPane)root;

        HBox basePlane = new HBox();
        basePlane.setPadding(new Insets(5));
        basePlane.setSpacing(5);
        pane.getChildren().add(basePlane);

        // Identities
        VBox identitiesPane = new VBox();
        identitiesPane.setPadding(new Insets(5));
        identitiesPane.setSpacing(5);
        identitiesPane.setPrefWidth(DesktopApp.WIDTH-10);
        basePlane.getChildren().add(identitiesPane);

        Text currentIdentity = new Text(Res.get("personalIdentitiesView.current"));
        if(activeDID!=null)
            currentIdentity.setText(activeDID.getUsername()+": "+activeDID.getPublicKey().getFingerprint());
        identitiesPane.getChildren().add(currentIdentity);

        HBox addIdentityBox = new HBox();
        addIdentityBox.setPadding(new Insets(5));
        addIdentityBox.setSpacing(5);
        identitiesPane.getChildren().add(addIdentityBox);

        JFXTextField identityAliasTxt = new JFXTextField();
        identityAliasTxt.setLabelFloat(true);
        identityAliasTxt.setPromptText(Res.get("shared.alias"));
        identityAliasTxt.setPrefWidth(200);
        addIdentityBox.getChildren().add(identityAliasTxt);

        JFXTextField identityPwdText = new JFXTextField();
        identityPwdText.setLabelFloat(true);
        identityPwdText.setPromptText(Res.get("shared.passphrase"));
        identityPwdText.setPrefWidth(200);
        addIdentityBox.getChildren().add(identityPwdText);

        JFXTextField identityPwd2Text = new JFXTextField();
        identityPwd2Text.setLabelFloat(true);
        identityPwd2Text.setPromptText(Res.get("shared.passphraseAgain"));
        identityPwd2Text.setPrefWidth(200);
        addIdentityBox.getChildren().add(identityPwd2Text);

        JFXTextField identityDescription = new JFXTextField();
        identityDescription.setLabelFloat(true);
        identityDescription.setPromptText(Res.get("shared.description"));
        identityDescription.setPrefWidth(400);
        addIdentityBox.getChildren().add(identityDescription);

        JFXButton addIdentity = new JFXButton(Res.get("shared.generate"));
        addIdentity.getStyleClass().add("button-raised");
        addIdentity.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(!identityAliasTxt.getText().isEmpty()
                        && !identityPwdText.getText().isEmpty()
                        && !identityPwd2Text.getText().isEmpty()) {
                    Envelope e = Envelope.documentFactory();
                    // 4. Update UI
                    DLC.addRoute(DesktopService.class, DesktopService.OPERATION_UPDATE_IDENTITIES, e);
                    // 3. Load ordered Identities
                    DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_IDENTITIES, e);
                    // 2. Authenticate/Save DID
                    DID did = new DID();
                    did.setUsername(identityAliasTxt.getText());
                    did.setPassphrase(identityPwdText.getText());
                    did.setPassphrase2(identityPwd2Text.getText());
                    if(!identityDescription.getText().isEmpty())
                        did.setDescription(identityDescription.getText());
                    AuthenticateDIDRequest adr = new AuthenticateDIDRequest();
                    adr.did = did;
                    adr.autogenerate = true;
                    DLC.addData(AuthenticateDIDRequest.class, adr, e);
                    DLC.addRoute(DIDService.class, DIDService.OPERATION_AUTHENTICATE,e);
                    // 1. Load Public Key addresses for short and full addresses
                    AuthNRequest ar = new AuthNRequest();
                    ar.keyRingUsername = did.getUsername();
                    ar.keyRingPassphrase = did.getPassphrase();
                    ar.alias = did.getUsername(); // use username as default alias
                    ar.aliasPassphrase = did.getPassphrase(); // just use same passphrase
                    ar.autoGenerate = true;
                    DLC.addData(AuthNRequest.class, ar, e);
                    DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_AUTHN, e);
                    // Send
                    OneMFivePlatform.sendRequest(e);
                } else {
                    // TODO: show in pop up
                    LOG.warning("Alias, pwd, pwd again required.");
                }
            }
        });
        addIdentityBox.getChildren().add(addIdentity);

        JFXListView<String> identitiesList = new JFXListView<>();
        identitiesList.setPrefSize(800, 500);
        identitiesList.setItems(identityAddresses);
        identitiesList.setEditable(true);
        identitiesList.getStyleClass().add("listView");
        identitiesPane.getChildren().add(identitiesList);

        JFXButton deleteIdentity = new JFXButton(Res.get("shared.delete"));
        deleteIdentity.getStyleClass().add("button-raised");
        deleteIdentity.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int index = identitiesList.getSelectionModel().getSelectedIndex();
                if(index >= 0) {
                    String itemStr = identityAddresses.get(index);
                    LOG.info(itemStr);
//                    String[] item = itemStr.split(":");

                }
            }
        });
        identitiesPane.getChildren().add(deleteIdentity);

        // Get Identities
        Envelope e1 = Envelope.documentFactory();
        DLC.addRoute(DesktopService.class, DesktopService.OPERATION_UPDATE_IDENTITIES, e1);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_IDENTITIES, e1);
        OneMFivePlatform.sendRequest(e1);

        // Get Active Identity
        Envelope e3 = Envelope.documentFactory();
        DLC.addRoute(DesktopService.class, DesktopService.OPERATION_UPDATE_ACTIVE_IDENTITY, e3);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_ACTIVE_IDENTITY, e3);
        OneMFivePlatform.sendRequest(e3);

        LOG.info("Initialized.");
    }

}
