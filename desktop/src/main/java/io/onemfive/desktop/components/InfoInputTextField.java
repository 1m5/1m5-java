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
package io.onemfive.desktop.components;

import de.jensd.fx.fontawesome.AwesomeIcon;
import io.onemfive.desktop.components.controlsfx.control.PopOver;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import static io.onemfive.desktop.util.FormBuilder.getIcon;

public class InfoInputTextField extends AnchorPane {

    private final StringProperty text = new SimpleStringProperty();

    private final InputTextField inputTextField;
    private final Label infoIcon;
    private final Label warningIcon;
    private final Label privacyIcon;

    private Label currentIcon;
    private PopOverWrapper popoverWrapper = new PopOverWrapper();

    public InfoInputTextField() {
        this(0);
    }

    public InfoInputTextField(double inputLineExtension) {
        super();

        inputTextField = new InputTextField(inputLineExtension);

        infoIcon = getIcon(AwesomeIcon.INFO_SIGN);
        infoIcon.setLayoutY(3);
        infoIcon.getStyleClass().addAll("icon", "info");

        warningIcon = getIcon(AwesomeIcon.WARNING_SIGN);
        warningIcon.setLayoutY(3);
        warningIcon.getStyleClass().addAll("icon", "warning");

        privacyIcon = getIcon(AwesomeIcon.EYE_CLOSE);
        privacyIcon.setLayoutY(3);
        privacyIcon.getStyleClass().addAll("icon", "info");

        AnchorPane.setLeftAnchor(infoIcon, 7.0);
        AnchorPane.setLeftAnchor(warningIcon, 7.0);
        AnchorPane.setLeftAnchor(privacyIcon, 7.0);
        AnchorPane.setRightAnchor(inputTextField, 0.0);
        AnchorPane.setLeftAnchor(inputTextField, 0.0);

        hideIcons();

        getChildren().addAll(inputTextField, infoIcon, warningIcon, privacyIcon);
    }

    public InputTextField getInputTextField() {
        return inputTextField;
    }

    private void hideIcons() {
        infoIcon.setManaged(false);
        infoIcon.setVisible(false);
        warningIcon.setManaged(false);
        warningIcon.setVisible(false);
        privacyIcon.setManaged(false);
        privacyIcon.setVisible(false);
    }

    public void setContentForInfoPopOver(Node node) {
        currentIcon = infoIcon;

        hideIcons();
        setActionHandlers(node);
    }

    public void setContentForWarningPopOver(Node node) {
        currentIcon = warningIcon;

        hideIcons();
        setActionHandlers(node);
    }

    public void setContentForPrivacyPopOver(Node node) {
        currentIcon = privacyIcon;

        hideIcons();
        setActionHandlers(node);
    }

    public void setIconsRightAligned() {
        AnchorPane.clearConstraints(infoIcon);
        AnchorPane.clearConstraints(warningIcon);
        AnchorPane.clearConstraints(privacyIcon);
        AnchorPane.clearConstraints(inputTextField);

        AnchorPane.setRightAnchor(infoIcon, 7.0);
        AnchorPane.setRightAnchor(warningIcon, 7.0);
        AnchorPane.setRightAnchor(privacyIcon, 7.0);
        AnchorPane.setLeftAnchor(inputTextField, 0.0);
        AnchorPane.setRightAnchor(inputTextField, 0.0);
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public String getText() {
        return text.get();
    }

    public final StringProperty textProperty() {
        return text;
    }

    private void setActionHandlers(Node node) {

        if (node != null) {
            currentIcon.setManaged(true);
            currentIcon.setVisible(true);

            // As we don't use binding here we need to recreate it on mouse over to reflect the current state
            currentIcon.setOnMouseEntered(e -> popoverWrapper.showPopOver(() -> createPopOver(node)));
            currentIcon.setOnMouseExited(e -> popoverWrapper.hidePopOver());
        }
    }

    private PopOver createPopOver(Node node) {
        node.getStyleClass().add("default-text");

        PopOver popover = new PopOver(node);
        if (currentIcon.getScene() != null) {
            popover.setDetachable(false);
            popover.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
            popover.setArrowIndent(5);

            popover.show(currentIcon, -17);
        }
        return popover;
    }
}
