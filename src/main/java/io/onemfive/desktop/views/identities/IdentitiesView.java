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
package io.onemfive.desktop.views.identities;

import io.onemfive.DRouter;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;
import io.onemfive.desktop.DesktopApp;
import io.onemfive.desktop.UIService;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.did.DIDService;
import io.onemfive.util.DLC;
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
        StackPane rootContainer = (StackPane)root;

        HBox basePlane = new HBox();
        basePlane.setPadding(new Insets(5));
        basePlane.setSpacing(5);
        rootContainer.getChildren().add(basePlane);

        // My Identities
        VBox identitiesPane = new VBox();
        identitiesPane.setPadding(new Insets(5));
        identitiesPane.setSpacing(5);
        identitiesPane.setPrefWidth((DesktopApp.width/2)-10);
        basePlane.getChildren().add(identitiesPane);

        Text myIdentitiesText = new Text("My Identities");
        identitiesPane.getChildren().add(myIdentitiesText);

        HBox identitiesHeader = new HBox();
        identitiesHeader.setPadding(new Insets(5));
        identitiesHeader.setSpacing(5);
        identitiesPane.getChildren().add(identitiesHeader);

        Label identityAliasCol = new Label("Alias");
        identityAliasCol.setPrefWidth(100);
        identitiesHeader.getChildren().add(identityAliasCol);

        Label identityPwdCol = new Label("Password");
        identityPwdCol.setPrefWidth(100);
        identitiesHeader.getChildren().add(identityPwdCol);

        Label identityPwd2Col = new Label("Password Again");
        identityPwd2Col.setPrefWidth(100);
        identitiesHeader.getChildren().add(identityPwd2Col);

        Label identityAddressCol = new Label("Address");
        identityAddressCol.setPrefWidth(200);
        identitiesHeader.getChildren().add(identityAddressCol);

        HBox addIdentityBox = new HBox();
        addIdentityBox.setPadding(new Insets(5));
        addIdentityBox.setSpacing(5);
        identitiesPane.getChildren().add(addIdentityBox);

        TextField identityAliasTxt = new TextField();
        identityAliasTxt.setPrefWidth(100);
        addIdentityBox.getChildren().add(identityAliasTxt);

        TextField identityPwdText = new TextField();
        identityPwdText.setPrefWidth(100);
        addIdentityBox.getChildren().add(identityPwdText);

        TextField identityPwd2Text = new TextField();
        identityPwd2Text.setPrefWidth(100);
        addIdentityBox.getChildren().add(identityPwd2Text);

        TextField identityAddress = new TextField();
        identityAddress.setPrefWidth(200);
        addIdentityBox.getChildren().add(identityAddress);

        Button addIdemtity = new Button("Add");
        addIdemtity.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(!identityAliasTxt.getText().isEmpty()
                        && !identityAddress.getText().isEmpty()
                        && !identityPwdText.getText().isEmpty()
                        && !identityPwd2Text.getText().isEmpty()) {
                    Envelope e = Envelope.documentFactory();
                    DID did = new DID();
                    did.setUsername(identityAliasTxt.getText());
                    did.setPassphrase(identityPwdText.getText());
                    did.setPassphrase2(identityPwd2Text.getText());
                    did.getPublicKey().setAddress(identityAddress.getText());
                    did.getPublicKey().setAlias(identityAliasTxt.getText());
                    Envelope e1 = Envelope.documentFactory();
                    DLC.addData(DID.class, did, e1);
                    DLC.addRoute(UIService.class, UIService.OPERATION_UPDATE_IDENTITIES, e1);
                    DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_IDENTITIES, e1);
                    DLC.addRoute(DIDService.class, DIDService.OPERATION_SAVE_IDENTITY, e1);
                    DRouter.sendRequest(e1);
                }
            }
        });
        addIdentityBox.getChildren().add(addIdemtity);

        ListView<String> identitiesList = new ListView<>();
        identitiesList.setPrefSize(400, 500);
        identitiesList.setItems(identityAddresses);
        identitiesList.setEditable(true);
        identitiesPane.getChildren().add(identitiesList);

        Button deleteIdentity = new Button("Delete");
        deleteIdentity.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int index = identitiesList.getSelectionModel().getSelectedIndex();
                if(index >= 0) {
//                    identityAddresses.remove(index);
                }
            }
        });
        identitiesPane.getChildren().add(deleteIdentity);

        // My Contacts
        VBox contactsPane = new VBox();
        contactsPane.setPadding(new Insets(5));
        contactsPane.setSpacing(5);
        contactsPane.setPrefWidth((DesktopApp.width/2)-10);
        basePlane.getChildren().add(contactsPane);

        Text myContactsText = new Text("My Contacts");
        contactsPane.getChildren().add(myContactsText);

        HBox contactsHeader = new HBox();
        contactsHeader.setPadding(new Insets(5));
        contactsHeader.setSpacing(5);
        contactsPane.getChildren().add(contactsHeader);

        Label contactsAliasCol = new Label("Alias");
        contactsAliasCol.setPrefWidth(150);
        contactsHeader.getChildren().add(contactsAliasCol);

        Label contactsAddressCol = new Label("Address");
        contactsAddressCol.setPrefWidth(350);
        contactsHeader.getChildren().add(contactsAddressCol);

        HBox addContactBox = new HBox();
        addContactBox.setPadding(new Insets(5));
        addContactBox.setSpacing(5);
        contactsPane.getChildren().add(addContactBox);

        TextField contractAliasTxt = new TextField();
        contractAliasTxt.setPrefWidth(150);
        addContactBox.getChildren().add(contractAliasTxt);

        TextField contactAddress = new TextField();
        contactAddress.setPrefWidth(350);
        addContactBox.getChildren().add(contactAddress);

        Button addContact = new Button("Add");
        addContact.setOnAction(actionEvent -> {
            // TODO: Add error handling for alias and address
            if(contractAliasTxt.getText().isEmpty()) {
                LOG.info("Alias is required.");
                return;
            }
            if(contactAddress.getText().isEmpty()) {
                LOG.info("Address is required.");
                return;
            }
            DID did = new DID();
            did.setUsername(contractAliasTxt.getText());
            did.getPublicKey().setAddress(contactAddress.getText());
            Envelope e = Envelope.documentFactory();
            DLC.addRoute(UIService.class, UIService.OPERATION_NOTIFY_UI, e);
            DLC.addRoute(DIDService.class, DIDService.OPERATION_ADD_CONTACT, e);
            DLC.addEntity(did, e);
            DRouter.sendRequest(e);
        });
        addContactBox.getChildren().add(addContact);

        ListView<String> contactsList = new ListView<>();
        contactsList.setPrefSize(400,500);
        contactsList.setItems(contactAddresses);
        contactsPane.getChildren().add(contactsList);

        Button deleteContact = new Button("Delete");
        deleteContact.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int index = contactsList.getSelectionModel().getSelectedIndex();
                if(index >= 0) {

                }
            }
        });
        contactsPane.getChildren().add(deleteContact);

        // Get Identities
        Envelope e1 = Envelope.documentFactory();
        DLC.addRoute(UIService.class, UIService.OPERATION_UPDATE_IDENTITIES, e1);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_IDENTITIES, e1);
        DRouter.sendRequest(e1);

        // Get Contacts
        Envelope e2 = Envelope.documentFactory();
        DLC.addRoute(UIService.class, UIService.OPERATION_UPDATE_CONTACTS, e2);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_CONTACTS, e2);
        DRouter.sendRequest(e2);

        // Get Active Identity
        Envelope e3 = Envelope.documentFactory();
        DLC.addRoute(UIService.class, UIService.OPERATION_UPDATE_ACTIVE_IDENTITY, e3);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_ACTIVE_IDENTITY, e3);
        DRouter.sendRequest(e3);

        LOG.info("Initialized.");
    }

}
