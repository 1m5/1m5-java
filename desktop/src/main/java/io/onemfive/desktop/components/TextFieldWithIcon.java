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

import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextAlignment;

import java.util.logging.Logger;

public class TextFieldWithIcon extends AnchorPane {

    public static final Logger LOG = Logger.getLogger(TextFieldWithIcon.class.getName());

    private final Label iconLabel;
    private final TextField textField;
    private final Label dummyTextField;

    public TextFieldWithIcon() {
        textField = new JFXTextField();
        textField.setEditable(false);
        textField.setMouseTransparent(true);
        textField.setFocusTraversable(false);
        setLeftAnchor(textField, 0d);
        setRightAnchor(textField, 0d);

        dummyTextField = new Label();
        dummyTextField.setWrapText(true);
        dummyTextField.setAlignment(Pos.CENTER_LEFT);
        dummyTextField.setTextAlignment(TextAlignment.LEFT);
        dummyTextField.setMouseTransparent(true);
        dummyTextField.setFocusTraversable(false);
        setLeftAnchor(dummyTextField, 0d);
        dummyTextField.setVisible(false);

        iconLabel = new Label();
        iconLabel.setLayoutX(0);
        iconLabel.setLayoutY(3);

        dummyTextField.widthProperty().addListener((observable, oldValue, newValue) -> {
            iconLabel.setLayoutX(dummyTextField.widthProperty().get() + 20);
        });

        getChildren().addAll(textField, dummyTextField, iconLabel);
    }



    public void setIcon(AwesomeIcon iconLabel) {
        AwesomeDude.setIcon(this.iconLabel, iconLabel);
    }

    public void setText(String text) {
        textField.setText(text);
        dummyTextField.setText(text);
    }
}
