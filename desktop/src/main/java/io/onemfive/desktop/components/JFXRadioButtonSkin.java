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

import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.transitions.JFXAnimationTimer;
import com.jfoenix.transitions.JFXKeyFrame;
import com.jfoenix.transitions.JFXKeyValue;
import javafx.animation.Interpolator;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.skin.RadioButtonSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Code copied and adapted from com.jfoenix.skins.JFXRadioButtonSkin
 */

public class JFXRadioButtonSkin extends RadioButtonSkin {
    private final JFXRippler rippler;
    private double padding = 12;

    private Circle radio, dot;
    private final StackPane container;

    private JFXAnimationTimer timer;

    public JFXRadioButtonSkin(JFXRadioButton control) {
        super(control);

        final double radioRadius = 7;
        radio = new Circle(radioRadius);
        radio.getStyleClass().setAll("radio");
        radio.setStrokeWidth(1);
        radio.setStrokeType(StrokeType.INSIDE);
        radio.setFill(Color.TRANSPARENT);
        radio.setSmooth(true);

        dot = new Circle(radioRadius);
        dot.getStyleClass().setAll("dot");
        dot.fillProperty().bind(control.selectedColorProperty());
        dot.setScaleX(0);
        dot.setScaleY(0);
        dot.setSmooth(true);

        container = new StackPane(radio, dot);
        container.getStyleClass().add("radio-container");

        rippler = new JFXRippler(container, JFXRippler.RipplerMask.CIRCLE) {
            @Override
            protected double computeRippleRadius() {
                double width = ripplerPane.getWidth();
                double width2 = width * width;
                return Math.min(Math.sqrt(width2 + width2), RIPPLE_MAX_RADIUS) * 1.1 + 5;
            }

            @Override
            protected void setOverLayBounds(Rectangle overlay) {
                overlay.setWidth(ripplerPane.getWidth());
                overlay.setHeight(ripplerPane.getHeight());
            }

            protected void initControlListeners() {
                // if the control got resized the overlay rect must be rest
                control.layoutBoundsProperty().addListener(observable -> resetRippler());
                if (getChildren().contains(control)) {
                    control.boundsInParentProperty().addListener(observable -> resetRippler());
                }
                control.addEventHandler(MouseEvent.MOUSE_PRESSED,
                        (event) -> createRipple(event.getX() + padding, event.getY() + padding));
                // create fade out transition for the ripple
                control.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> releaseRipple());
            }

            @Override
            protected Node getMask() {
                double radius = ripplerPane.getWidth() / 2;
                return new Circle(radius, radius, radius);
            }

            @Override
            protected void positionControl(Node control) {

            }
        };

        updateChildren();

        // show focused state
        control.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!control.disableVisualFocusProperty().get()) {
                if (newVal) {
                    if (!getSkinnable().isPressed()) {
                        rippler.setOverlayVisible(true);
                    }
                } else {
                    rippler.setOverlayVisible(false);
                }
            }
        });

        control.pressedProperty().addListener((o, oldVal, newVal) -> rippler.setOverlayVisible(false));

        timer = new JFXAnimationTimer(
                new JFXKeyFrame(Duration.millis(200),
                        JFXKeyValue.builder()
                                .setTarget(dot.scaleXProperty())
                                .setEndValueSupplier(() -> getSkinnable().isSelected() ? 0.55 : 0)
                                .setInterpolator(Interpolator.EASE_BOTH)
                                .build(),
                        JFXKeyValue.builder()
                                .setTarget(dot.scaleYProperty())
                                .setEndValueSupplier(() -> getSkinnable().isSelected() ? 0.55 : 0)
                                .setInterpolator(Interpolator.EASE_BOTH)
                                .build(),
                        JFXKeyValue.builder()
                                .setTarget(radio.strokeProperty())
                                .setEndValueSupplier(() -> getSkinnable().isSelected() ? ((JFXRadioButton) getSkinnable()).getSelectedColor() : ((JFXRadioButton) getSkinnable()).getUnSelectedColor())
                                .setInterpolator(Interpolator.EASE_BOTH)
                                .build()
                ));


        registerChangeListener(control.selectedColorProperty(), obs -> updateColors());
        registerChangeListener(control.unSelectedColorProperty(), obs -> updateColors());
        registerChangeListener(control.selectedProperty(), obs -> {
            boolean isSelected = getSkinnable().isSelected();
            Color unSelectedColor = ((JFXRadioButton) getSkinnable()).getUnSelectedColor();
            Color selectedColor = ((JFXRadioButton) getSkinnable()).getSelectedColor();
            rippler.setRipplerFill(isSelected ? selectedColor : unSelectedColor);
            if (((JFXRadioButton) getSkinnable()).isDisableAnimation()) {
                // apply end values
                timer.applyEndValues();
            } else {
                // play selection animation
                timer.reverseAndContinue();
            }
        });

        updateColors();
        timer.applyEndValues();
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        if (radio != null) {
            removeRadio();
            getChildren().addAll(container, rippler);
        }
    }

    @Override
    protected void layoutChildren(final double x, final double y, final double w, final double h) {
        final RadioButton radioButton = getSkinnable();
        final double contWidth = snapSizeX(container.prefWidth(-1));
        final double contHeight = snapSizeY(container.prefHeight(-1));
        final double computeWidth = Math.max(radioButton.prefWidth(-1), radioButton.minWidth(-1));
        final double width = snapSizeX(contWidth);
        final double height = snapSizeY(contHeight);

        final double labelWidth = Math.min(computeWidth - contWidth, w - width);
        final double labelHeight = Math.min(radioButton.prefHeight(labelWidth), h);
        final double maxHeight = Math.max(contHeight, labelHeight);
        final double xOffset = computeXOffset(w, labelWidth + contWidth, radioButton.getAlignment().getHpos()) + x;

        final double yOffset = computeYOffset(h, maxHeight, radioButton.getAlignment().getVpos()) + x + 5;

        layoutLabelInArea(xOffset + contWidth + padding / 3, yOffset, labelWidth, maxHeight, radioButton.getAlignment());
        ((Text) getChildren().get((getChildren().get(0) instanceof Text) ? 0 : 1)).
                textProperty().set(getSkinnable().textProperty().get());

        container.resize(width, height);
        positionInArea(container,
                xOffset,
                yOffset,
                contWidth,
                maxHeight,
                0,
                radioButton.getAlignment().getHpos(),
                radioButton.getAlignment().getVpos());

        final double ripplerWidth = width + 2 * padding;
        final double ripplerHeight = height + 2 * padding;
        rippler.resizeRelocate((width / 2 + xOffset) - ripplerWidth / 2,
                (height / 2 + yOffset) + 2 - ripplerHeight / 2,
                ripplerWidth, ripplerHeight);
    }

    private void removeRadio() {
        // TODO: replace with removeIf
        for (int i = 0; i < getChildren().size(); i++) {
            if ("radio".equals(getChildren().get(i).getStyleClass().get(0))) {
                getChildren().remove(i);
            }
        }
    }

    private void updateColors() {
        boolean isSelected = getSkinnable().isSelected();
        Color unSelectedColor = ((JFXRadioButton) getSkinnable()).getUnSelectedColor();
        Color selectedColor = ((JFXRadioButton) getSkinnable()).getSelectedColor();
        rippler.setRipplerFill(isSelected ? selectedColor : unSelectedColor);
        radio.setStroke(isSelected ? selectedColor : unSelectedColor);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computeMinWidth(height,
                topInset,
                rightInset,
                bottomInset,
                leftInset) + snapSize(radio.minWidth(-1)) + padding / 3;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computePrefWidth(height,
                topInset,
                rightInset,
                bottomInset,
                leftInset) + snapSizeX(radio.prefWidth(-1)) + padding / 3;
    }

    private static double computeXOffset(double width, double contentWidth, HPos hpos) {
        switch (hpos) {
            case LEFT:
                return 0;
            case CENTER:
                return (width - contentWidth) / 2;
            case RIGHT:
                return width - contentWidth;
        }
        return 0;
    }

    private static double computeYOffset(double height, double contentHeight, VPos vpos) {
        switch (vpos) {
            case TOP:
                return 0;
            case CENTER:
                return (height - contentHeight) / 2;
            case BOTTOM:
                return height - contentHeight;
            default:
                return 0;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        timer.dispose();
        timer = null;
    }
}