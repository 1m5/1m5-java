package io.onemfive.desktop.views.identities;

import io.onemfive.data.DID;
import io.onemfive.desktop.DesktopApp;
import io.onemfive.desktop.views.ActivatableView;
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

public class IdentitiesView extends ActivatableView {

    private ObservableList<String> identityAddresses = FXCollections.observableArrayList();
    private ObservableList<String> contactAddresses = FXCollections.observableArrayList();

    private DID activeDID;

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
        identityAliasCol.setPrefWidth(150);
        identitiesHeader.getChildren().add(identityAliasCol);

        Label identityAddressCol = new Label("Address");
        identityAddressCol.setPrefWidth(350);
        identitiesHeader.getChildren().add(identityAddressCol);

        HBox addIdentityBox = new HBox();
        addIdentityBox.setPadding(new Insets(5));
        addIdentityBox.setSpacing(5);
        identitiesPane.getChildren().add(addIdentityBox);

        TextField identityAliasTxt = new TextField();
        identityAliasTxt.setPrefWidth(150);
        addIdentityBox.getChildren().add(identityAliasTxt);

        TextField identityAddress = new TextField();
        identityAddress.setPrefWidth(350);
        addIdentityBox.getChildren().add(identityAddress);

        Button addIdemtity = new Button("Add");
        addIdemtity.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(!identityAliasTxt.getText().isEmpty() && !identityAddress.getText().isEmpty()) {

                }
            }
        });
        addIdentityBox.getChildren().add(addIdemtity);

        ListView<String> identitiesList = new ListView<>();
        identitiesList.setPrefSize(400, 500);
        identitiesList.setItems(identityAddresses);
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
        addContact.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(!contractAliasTxt.getText().isEmpty() && !contactAddress.getText().isEmpty()) {
//                    contactAddresses.add(contractAliasTxt.getText() + ": " + contactAddress.getText());
//                    contractAliasTxt.clear();
//                    contactAddress.clear();
                }
            }
        });
        addContactBox.getChildren().add(addContact);

        ListView<String> contactsList = new ListView<>();
        contactsList.setPrefSize(400,500);

        // Get Contacts


//        addresses.add("Alice: f8j2kwjfdwjaf4faofrj4oif8qojfi4fjpq38f4pmf348fjf");
//        addresses.add("Bob: fo347ha7uihfu7h4yfk74uqfh7f43nffh747f2h473fh7f4n3");
//        addresses.add("Charlie: iufhqofi437ufn4oiufhaf4bafiuhlkfqh4flk43nffn4lfnquf4");
//        addresses.add("Danny: fiewufqhfeiufhefiluo7qlohf7ihefqeifhflukahnfaufhlwfe");

        contactsList.setItems(contactAddresses);
        contactsPane.getChildren().add(contactsList);

        Button deleteContact = new Button("Delete");
        deleteContact.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int index = contactsList.getSelectionModel().getSelectedIndex();
                if(index >= 0) {
//                    contactAddresses.remove(index);
                }
            }
        });
        contactsPane.getChildren().add(deleteContact);

        LOG.info("Initialized.");
    }

}
