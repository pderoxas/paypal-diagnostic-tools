package com.paypal.merchant.retail.tools.control.dialogs;

import com.paypal.merchant.retail.tools.control.CustomButton;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.action.Action;

/**
 * Created by Paolo
 * Created on 5/12/14 2:34 PM
 */
public class AboutDialog extends DialogBase {
    private Label messageLabel = new Label();

    public AboutDialog() {
        super("Notification", true, DIALOG_AUTO_HIDE_DELAY, 0, 0);

        messageLabel.setVisible(true);
        messageLabel.getStyleClass().add("font-medium");
        messageLabel.setWrapText(true);
        messageLabel.setPrefWidth(1200);
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.setAlignment(Pos.CENTER);

        Button actionButton = new CustomButton("Close");
        actionButton.getStyleClass().addAll("button-large","button-blue");
        actionButton.setOnAction(ae -> {
            this.hide();
            ae.consume();
        });

        HBox hbButtons = new HBox();
        hbButtons.setPrefWidth(500);
        hbButtons.setPrefHeight(200);
        hbButtons.setAlignment(Pos.CENTER);
        hbButtons.setSpacing(10.0);
        hbButtons.setPrefHeight(100);
        hbButtons.getChildren().addAll(actionButton);

        GridPane mainContent = new GridPane();
        mainContent.getStylesheets().add("/styles/main.css");
        mainContent.setAlignment(Pos.CENTER);
        mainContent.add(messageLabel, 0, 0);
        mainContent.add(hbButtons, 0, 1);

        this.setMainContentLayer(mainContent);
    }

    public Action show(Event event, String title, String messageText) {
        this.setTitle(title);
        messageLabel.setText(messageText);
        return super.show(event);
    }

    @Override
    public void executePrimaryAction() {
        this.hide();
    }
}
