package com.paypal.merchant.retail.tools.control;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.text.TextAlignment;

/**
 * Created by Paolo
 * Created on 4/24/14 5:01 PM
 */
public class CustomButton extends Button {

    //private static final Border onClickBorder = new Border(new BorderStroke(Color.AQUA, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(3)));
    //private static final Border defaultBorder = new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(3)));

    public CustomButton() {
        this(null);
    }

    public CustomButton(String s) {
        super(s);
        this.getStylesheets().add("/styles/main.css");
        this.setAlignment(Pos.CENTER);
        this.setTextAlignment(TextAlignment.CENTER);
        this.setFocusTraversable(true);

        EventHandler<Event> onPressedHandler = event -> {
            this.setOpacity(0.5);
        };
        this.setOnMousePressed(onPressedHandler);
        this.setOnTouchPressed(onPressedHandler);

        EventHandler<Event> onReleasedHandler = event -> {
            this.setOpacity(1.0);
        };
        this.setOnMouseReleased(onReleasedHandler);
        this.setOnTouchReleased(onReleasedHandler);
    }
}
