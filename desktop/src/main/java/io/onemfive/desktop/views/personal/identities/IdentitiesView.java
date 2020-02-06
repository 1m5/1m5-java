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
    private ObservableList<String> contactAddresses = FXCollections.observableArrayList();

    private DID activeDID;

    public void updateActiveDID(DID activeDID) {
        this.activeDID = activeDID;
    }

    public void updateIdentities(List<DID> identities) {
        identityAddresses.clear();
        for(DID i : identities) {
            identityAddresses.add(i.getUsername() + ": "+i.getPublicKey().getAddress());
        }
    }

    public void updateContacts(List<DID> contacts) {
        contactAddresses.clear();
        for(DID c : contacts) {
            contactAddresses.add(c.getUsername() + ": "+c.getPublicKey().getAddress());
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

        // My Identities
        VBox identitiesPane = new VBox();
        identitiesPane.setPadding(new Insets(5));
        identitiesPane.setSpacing(5);
        identitiesPane.setPrefWidth(DesktopApp.WIDTH-10);
        basePlane.getChildren().add(identitiesPane);

        Text currentIdentity = new Text("Current");
        identitiesPane.getChildren().add(currentIdentity);

        HBox identitiesHeader = new HBox();
        identitiesHeader.setPadding(new Insets(5));
        identitiesHeader.setSpacing(5);
        identitiesPane.getChildren().add(identitiesHeader);

        Label identityAliasCol = new Label(Res.get("shared.alias"));
        identityAliasCol.setPrefWidth(200);
        identitiesHeader.getChildren().add(identityAliasCol);

        Label identityPwdCol = new Label(Res.get("shared.passphrase"));
        identityPwdCol.setPrefWidth(200);
        identitiesHeader.getChildren().add(identityPwdCol);

        Label identityPwd2Col = new Label(Res.get("shared.passphraseAgain"));
        identityPwd2Col.setPrefWidth(200);
        identitiesHeader.getChildren().add(identityPwd2Col);

        Label identityLocationCol = new Label(Res.get("shared.location"));
        identityLocationCol.setPrefWidth(400);
        identitiesHeader.getChildren().add(identityLocationCol);

        HBox addIdentityBox = new HBox();
        addIdentityBox.setPadding(new Insets(5));
        addIdentityBox.setSpacing(5);
        identitiesPane.getChildren().add(addIdentityBox);

        TextField identityAliasTxt = new TextField();
        identityAliasTxt.setPrefWidth(200);
        addIdentityBox.getChildren().add(identityAliasTxt);

        TextField identityPwdText = new TextField();
        identityPwdText.setPrefWidth(200);
        addIdentityBox.getChildren().add(identityPwdText);

        TextField identityPwd2Text = new TextField();
        identityPwd2Text.setPrefWidth(200);
        addIdentityBox.getChildren().add(identityPwd2Text);

        TextField identityLocationText = new TextField();
        identityLocationText.setPrefWidth(400);
        addIdentityBox.getChildren().add(identityLocationText);

        Button addIdentity = new Button(Res.get("shared.generate"));
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
                    AuthenticateDIDRequest adr = new AuthenticateDIDRequest();
                    adr.did = did;
                    adr.autogenerate = true;
                    DLC.addData(AuthenticateDIDRequest.class, adr, e);
                    DLC.addRoute(DIDService.class, DIDService.OPERATION_AUTHENTICATE,e);
                    // 1. Load Public Key addresses for short and full addresses
                    AuthNRequest ar = new AuthNRequest();
                    ar.location = identityLocationText.getText();
                    ar.keyRingUsername = did.getUsername();
                    ar.keyRingPassphrase = did.getPassphrase();
                    ar.alias = did.getUsername(); // use username as default alias
                    ar.aliasPassphrase = did.getPassphrase(); // just use same passphrase
                    ar.autoGenerate = true;
                    DLC.addData(AuthNRequest.class, ar, e);
                    DLC.addRoute(KeyRingService.class, KeyRingService.OPERATION_AUTHN, e);
                    // Send
                    OneMFivePlatform.sendRequest(e);
                }
            }
        });
        addIdentityBox.getChildren().add(addIdentity);

        ListView<String> identitiesList = new ListView<>();
        identitiesList.setPrefSize(800, 500);
        identitiesList.setItems(identityAddresses);
        identitiesList.setEditable(true);
        identitiesPane.getChildren().add(identitiesList);

        Button deleteIdentity = new Button("Delete");
        deleteIdentity.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int index = identitiesList.getSelectionModel().getSelectedIndex();
                if(index >= 0) {
                    String itemStr = identityAddresses.get(index);
                    String[] item = itemStr.split(":");

                }
            }
        });
        identitiesPane.getChildren().add(deleteIdentity);

//        // My Contacts
//        VBox contactsPane = new VBox();
//        contactsPane.setPadding(new Insets(5));
//        contactsPane.setSpacing(5);
//        contactsPane.setPrefWidth((DesktopApp.WIDTH /2)-10);
//        basePlane.getChildren().add(contactsPane);
//
//        Text myContactsText = new Text("My Contacts");
//        contactsPane.getChildren().add(myContactsText);
//
//        HBox contactsHeader = new HBox();
//        contactsHeader.setPadding(new Insets(5));
//        contactsHeader.setSpacing(5);
//        contactsPane.getChildren().add(contactsHeader);
//
//        Label contactsAliasCol = new Label("Alias");
//        contactsAliasCol.setPrefWidth(100);
//        contactsHeader.getChildren().add(contactsAliasCol);
//
//        Label contactsFingerprintCol = new Label("Fingerprint");
//        contactsFingerprintCol.setPrefWidth(100);
//        contactsHeader.getChildren().add(contactsFingerprintCol);
//
//        Label contactsAddressCol = new Label("Address");
//        contactsAddressCol.setPrefWidth(100);
//        contactsHeader.getChildren().add(contactsAddressCol);
//
//        Label contactsDescriptionCol = new Label("Description");
//        contactsDescriptionCol.setPrefWidth(200);
//        contactsHeader.getChildren().add(contactsDescriptionCol);
//
//        HBox addContactBox = new HBox();
//        addContactBox.setPadding(new Insets(5));
//        addContactBox.setSpacing(5);
//        contactsPane.getChildren().add(addContactBox);
//
//        TextField contactAliasTxt = new TextField();
//        contactAliasTxt.setPrefWidth(100);
//        addContactBox.getChildren().add(contactAliasTxt);
//
//        TextField contactFingerprintTxt = new TextField();
//        contactFingerprintTxt.setPrefWidth(100);
//        addContactBox.getChildren().add(contactFingerprintTxt);
//
//        TextField contactAddressTxt = new TextField();
//        contactAddressTxt.setPrefWidth(100);
//        addContactBox.getChildren().add(contactAddressTxt);
//
//        TextField contactDescriptiontxt = new TextField();
//        contactDescriptiontxt.setPrefWidth(200);
//        addContactBox.getChildren().add(contactDescriptiontxt);
//
//        Button addContact = new Button("Add");
//        addContact.setOnAction(actionEvent -> {
//            // TODO: Add error handling for alias and address
//            if(contactAliasTxt.getText().isEmpty()) {
//                LOG.info("Alias is required.");
//                return;
//            }
//            if(contactFingerprintTxt.getText().isEmpty()) {
//                LOG.info("Fingerprint is required.");
//                return;
//            }
//            if(contactAddressTxt.getText().isEmpty()) {
//                LOG.info("Address is required.");
//                return;
//            }
//            DID did = new DID();
//            did.setUsername(contactAliasTxt.getText());
//            did.getPublicKey().setAddress(contactAddressTxt.getText());
//            Envelope e = Envelope.documentFactory();
//            DLC.addRoute(DesktopService.class, DesktopService.OPERATION_NOTIFY_UI, e);
//            DLC.addRoute(DIDService.class, DIDService.OPERATION_ADD_CONTACT, e);
//            DLC.addEntity(did, e);
//            Platform.sendRequest(e);
//        });
//        addContactBox.getChildren().add(addContact);
//
//        ListView<String> contactsList = new ListView<>();
//        contactsList.setPrefSize(400,500);
//        contactsList.setItems(contactAddresses);
//        contactsPane.getChildren().add(contactsList);
//
//        Button deleteContact = new Button("Delete");
//        deleteContact.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                int index = contactsList.getSelectionModel().getSelectedIndex();
//                if(index >= 0) {
//
//                }
//            }
//        });
//        contactsPane.getChildren().add(deleteContact);

        // Get Identities
        Envelope e1 = Envelope.documentFactory();
        DLC.addRoute(DesktopService.class, DesktopService.OPERATION_UPDATE_IDENTITIES, e1);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_IDENTITIES, e1);
        OneMFivePlatform.sendRequest(e1);

        // Get Contacts
//        Envelope e2 = Envelope.documentFactory();
//        DLC.addRoute(DesktopService.class, DesktopService.OPERATION_UPDATE_CONTACTS, e2);
//        DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_CONTACTS, e2);
//        Platform.sendRequest(e2);

        // Get Active Identity
        Envelope e3 = Envelope.documentFactory();
        DLC.addRoute(DesktopService.class, DesktopService.OPERATION_UPDATE_ACTIVE_IDENTITY, e3);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_ACTIVE_IDENTITY, e3);
        OneMFivePlatform.sendRequest(e3);

        LOG.info("Initialized.");
    }

}
