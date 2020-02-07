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
import de.jensd.fx.glyphs.GlyphIcons;
import io.onemfive.desktop.components.controlsfx.control.PopOver;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;

import static io.onemfive.desktop.util.FormBuilder.getIcon;

public class InfoAutoTooltipLabel extends AutoTooltipLabel {

    public static final int DEFAULT_WIDTH = 300;
    private Node textIcon;
    private PopOverWrapper popoverWrapper = new PopOverWrapper();
    private ContentDisplay contentDisplay;

    public InfoAutoTooltipLabel(String text, GlyphIcons icon, ContentDisplay contentDisplay, String info) {
        this(text, contentDisplay);

        setIcon(icon);
        positionAndActivateIcon(contentDisplay, info, DEFAULT_WIDTH);
    }

    public InfoAutoTooltipLabel(String text, AwesomeIcon icon, ContentDisplay contentDisplay, String info, double width) {
        super(text);

        setIcon(icon);
        positionAndActivateIcon(contentDisplay, info, width);
    }

    public InfoAutoTooltipLabel(String text, ContentDisplay contentDisplay) {
        super(text);
        this.contentDisplay = contentDisplay;
    }

    public void setIcon(GlyphIcons icon) {
        textIcon = getIcon(icon);
    }

    public void setIcon(GlyphIcons icon, String info) {
        setIcon(icon);
        positionAndActivateIcon(contentDisplay, info, DEFAULT_WIDTH);
    }

    public void setIcon(AwesomeIcon icon) {
        textIcon = getIcon(icon);
    }

    public void hideIcon() {
        textIcon = null;
        setGraphic(textIcon);
    }

    private void positionAndActivateIcon(ContentDisplay contentDisplay, String info, double width) {
        textIcon.setOpacity(0.4);
        textIcon.getStyleClass().add("tooltip-icon");
        textIcon.setOnMouseEntered(e -> popoverWrapper.showPopOver(() -> createInfoPopOver(info, width)));
        textIcon.setOnMouseExited(e -> popoverWrapper.hidePopOver());

        setGraphic(textIcon);
        setContentDisplay(contentDisplay);
    }

    private PopOver createInfoPopOver(String info, double width) {
        Label helpLabel = new Label(info);
        helpLabel.setMaxWidth(width);
        helpLabel.setWrapText(true);
        helpLabel.setPadding(new Insets(10));
        return createInfoPopOver(helpLabel);
    }

    private PopOver createInfoPopOver(Node node) {
        node.getStyleClass().add("default-text");

        PopOver infoPopover = new PopOver(node);
        if (textIcon.getScene() != null) {
            infoPopover.setDetachable(false);
            infoPopover.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);

            infoPopover.show(textIcon, -10);
        }
        return infoPopover;
    }
}
