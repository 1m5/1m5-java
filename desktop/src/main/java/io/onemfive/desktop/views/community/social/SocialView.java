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
package io.onemfive.desktop.views.community.social;

import io.onemfive.OneMFivePlatform;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;
import io.onemfive.desktop.DesktopApp;
import io.onemfive.desktop.DesktopService;
import io.onemfive.desktop.views.InitializableView;
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

public class SocialView extends InitializableView {

    private ObservableList<String> contactAddresses = FXCollections.observableArrayList();

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

        // My Contacts
        VBox contactsPane = new VBox();
        contactsPane.setPadding(new Insets(5));
        contactsPane.setSpacing(5);
        contactsPane.setPrefWidth(DesktopApp.WIDTH-10);
        basePlane.getChildren().add(contactsPane);

        Text myContactsText = new Text("My Contacts");
        contactsPane.getChildren().add(myContactsText);

        HBox contactsHeader = new HBox();
        contactsHeader.setPadding(new Insets(5));
        contactsHeader.setSpacing(5);
        contactsPane.getChildren().add(contactsHeader);

        Label contactsAliasCol = new Label("Alias");
        contactsAliasCol.setPrefWidth(200);
        contactsHeader.getChildren().add(contactsAliasCol);

        Label contactsFingerprintCol = new Label("Fingerprint");
        contactsFingerprintCol.setPrefWidth(200);
        contactsHeader.getChildren().add(contactsFingerprintCol);

        Label contactsAddressCol = new Label("Address");
        contactsAddressCol.setPrefWidth(200);
        contactsHeader.getChildren().add(contactsAddressCol);

        Label contactsDescriptionCol = new Label("Description");
        contactsDescriptionCol.setPrefWidth(400);
        contactsHeader.getChildren().add(contactsDescriptionCol);

        HBox addContactBox = new HBox();
        addContactBox.setPadding(new Insets(5));
        addContactBox.setSpacing(5);
        contactsPane.getChildren().add(addContactBox);

        TextField contactAliasTxt = new TextField();
        contactAliasTxt.setPrefWidth(200);
        addContactBox.getChildren().add(contactAliasTxt);

        TextField contactFingerprintTxt = new TextField();
        contactFingerprintTxt.setPrefWidth(200);
        addContactBox.getChildren().add(contactFingerprintTxt);

        TextField contactAddressTxt = new TextField();
        contactAddressTxt.setPrefWidth(200);
        addContactBox.getChildren().add(contactAddressTxt);

        TextField contactDescriptiontxt = new TextField();
        contactDescriptiontxt.setPrefWidth(400);
        addContactBox.getChildren().add(contactDescriptiontxt);

        Button addContact = new Button("Add");
        addContact.setOnAction(actionEvent -> {
            // TODO: Add error handling for alias and address
            if(contactAliasTxt.getText().isEmpty()) {
                LOG.info("Alias is required.");
                return;
            }
            if(contactFingerprintTxt.getText().isEmpty()) {
                LOG.info("Fingerprint is required.");
                return;
            }
            if(contactAddressTxt.getText().isEmpty()) {
                LOG.info("Address is required.");
                return;
            }
            DID did = new DID();
            did.setUsername(contactAliasTxt.getText());
            did.getPublicKey().setAddress(contactAddressTxt.getText());
            Envelope e = Envelope.documentFactory();
            DLC.addRoute(DesktopService.class, DesktopService.OPERATION_NOTIFY_UI, e);
            DLC.addRoute(DIDService.class, DIDService.OPERATION_ADD_CONTACT, e);
            DLC.addEntity(did, e);
            OneMFivePlatform.sendRequest(e);
        });
        addContactBox.getChildren().add(addContact);

        ListView<String> contactsList = new ListView<>();
        contactsList.setPrefSize(800,500);
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

        // Get Contacts
        Envelope e2 = Envelope.documentFactory();
        DLC.addRoute(DesktopService.class, DesktopService.OPERATION_UPDATE_CONTACTS, e2);
        DLC.addRoute(DIDService.class, DIDService.OPERATION_GET_CONTACTS, e2);
        OneMFivePlatform.sendRequest(e2);

        LOG.info("Initialized.");
    }

}
