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

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.jensd.fx.glyphs.GlyphIcons;
import io.onemfive.desktop.util.FormBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class HyperlinkWithIcon extends Hyperlink {

    public HyperlinkWithIcon(String text) {
        this(text, AwesomeIcon.INFO_SIGN);
    }

    public HyperlinkWithIcon(String text, AwesomeIcon awesomeIcon) {
        super(text);

        Label icon = new Label();
        AwesomeDude.setIcon(icon, awesomeIcon);
        icon.setMinWidth(20);
        icon.setOpacity(0.7);
        icon.getStyleClass().addAll("hyperlink", "no-underline");
        setPadding(new Insets(0));
        icon.setPadding(new Insets(0));

        setIcon(icon);
    }

    public HyperlinkWithIcon(String text, GlyphIcons icon) {
        this(text, icon, null);
    }

    public HyperlinkWithIcon(String text, GlyphIcons icon, String style) {
        super(text);

        Text textIcon = FormBuilder.getIcon(icon);
        textIcon.setOpacity(0.7);
        textIcon.getStyleClass().addAll("hyperlink", "no-underline");

        if (style != null) {
            textIcon.getStyleClass().add(style);
            getStyleClass().add(style);
        }

        setPadding(new Insets(0));

        setIcon(textIcon);
    }

    public void hideIcon() {
        setGraphic(null);
    }

    private void setIcon(Node icon) {
        setGraphic(icon);

        setContentDisplay(ContentDisplay.RIGHT);
        setGraphicTextGap(7.0);
    }

    public void clear() {
        setText("");
        setGraphic(null);
    }
}
