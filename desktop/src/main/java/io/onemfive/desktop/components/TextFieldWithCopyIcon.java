package io.onemfive.desktop.components;

import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import io.onemfive.desktop.util.GUIUtil;
import io.onemfive.util.Res;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;

public class TextFieldWithCopyIcon extends AnchorPane {

    private final StringProperty text = new SimpleStringProperty();
    private final TextField textField;
    private boolean copyWithoutCurrencyPostFix;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    public TextFieldWithCopyIcon() {
        this(null);
    }

    public TextFieldWithCopyIcon(String customStyleClass) {
        Label copyIcon = new Label();
        copyIcon.setLayoutY(3);
        copyIcon.getStyleClass().addAll("icon", "highlight");
        copyIcon.setTooltip(new Tooltip(Res.get("shared.copyToClipboard")));
        AwesomeDude.setIcon(copyIcon, AwesomeIcon.COPY);
        copyIcon.setOnMouseClicked(e -> {
            String text = getText();
            if (text != null && text.length() > 0) {
                String copyText;
                if (copyWithoutCurrencyPostFix) {
                    String[] strings = text.split(" ");
                    if (strings.length > 1)
                        copyText = strings[0]; // exclude the BTC postfix
                    else
                        copyText = text;
                } else {
                    copyText = text;
                }
                GUIUtil.copyToClipboard(copyText);
            }
        });
        textField = new JFXTextField();
        textField.setEditable(false);
        if (customStyleClass != null) textField.getStyleClass().add(customStyleClass);
        textField.textProperty().bindBidirectional(text);
        AnchorPane.setRightAnchor(copyIcon, 5.0);
        AnchorPane.setRightAnchor(textField, 30.0);
        AnchorPane.setLeftAnchor(textField, 0.0);
        textField.focusTraversableProperty().set(focusTraversableProperty().get());
        //TODO app wide focus
        //focusedProperty().addListener((ov, oldValue, newValue) -> textField.requestFocus());

        getChildren().addAll(textField, copyIcon);
    }

    public void setPromptText(String value) {
        textField.setPromptText(value);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Getter/Setter
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public void setTooltip(Tooltip toolTip) {
        textField.setTooltip(toolTip);
    }

    public void setCopyWithoutCurrencyPostFix(boolean copyWithoutCurrencyPostFix) {
        this.copyWithoutCurrencyPostFix = copyWithoutCurrencyPostFix;
    }

}
