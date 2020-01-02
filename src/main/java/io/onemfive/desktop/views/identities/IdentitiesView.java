package io.onemfive.desktop.views.identities;

import io.onemfive.desktop.views.ActivatableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class IdentitiesView extends ActivatableView {

    private ObservableList<String> addresses = FXCollections.observableArrayList();

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        StackPane rootContainer = (StackPane)root;

        ListView<String> contactsList = new ListView<>();
        contactsList.setPrefSize(400,400);
        contactsList.setEditable(true);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(5));
        vBox.setSpacing(5);
        rootContainer.getChildren().add(vBox);

        Text localNodeAddressText = new Text();
        localNodeAddressText.setText("Me: mlaiNilkIN348HNkklNIJK3423kjlr32kjnjk32h32ieu3f2sddsafs");
        vBox.getChildren().add(localNodeAddressText);

        HBox contactHeader = new HBox();
        contactHeader.setPadding(new Insets(5));
        contactHeader.setSpacing(5);
        Label aliasCol = new Label("Alias");
        aliasCol.setPrefWidth(200);
        contactHeader.getChildren().add(aliasCol);
        Label addressCol = new Label("Address");
        addressCol.setPrefWidth(500);
        contactHeader.getChildren().add(addressCol);
        vBox.getChildren().add(contactHeader);

        HBox addContactBox = new HBox();
        addContactBox.setPadding(new Insets(5));
        addContactBox.setSpacing(5);
        vBox.getChildren().add(addContactBox);

        TextField aliasTxt = new TextField();
        aliasTxt.setPrefWidth(200);
        addContactBox.getChildren().add(aliasTxt);

        TextField address = new TextField();
        address.setPrefWidth(500);
        addContactBox.getChildren().add(address);

        Button addContact = new Button("Add");
        addContact.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addresses.add(aliasTxt.getText()+": "+address.getText());
                aliasTxt.clear();
                address.clear();
            }
        });
        addContactBox.getChildren().add(addContact);

        addresses.add("Alice: f8j2kwjfdwjaf4faofrj4oif8qojfi4fjpq38f4pmf348fjf");
        addresses.add("Bob: fo347ha7uihfu7h4yfk74uqfh7f43nffh747f2h473fh7f4n3");
        addresses.add("Charlie: iufhqofi437ufn4oiufhaf4bafiuhlkfqh4flk43nffn4lfnquf4");
        addresses.add("Danny: fiewufqhfeiufhefiluo7qlohf7ihefqeifhflukahnfaufhlwfe");
        contactsList.setItems(addresses);
        vBox.getChildren().add(contactsList);

        Button deleteContact = new Button("Delete");
        deleteContact.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int index = contactsList.getSelectionModel().getSelectedIndex();
                if(index >= 0) {
                    addresses.remove(index);
                }
            }
        });
        vBox.getChildren().add(deleteContact);

        LOG.info("Initialized.");
    }

}
