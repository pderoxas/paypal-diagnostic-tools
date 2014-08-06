package com.paypal.merchant.retail.tools.control.dialogs;

import com.paypal.merchant.retail.tools.util.PropertyManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import org.controlsfx.dialog.Dialog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Paolo
 * Created on 3/21/14 12:20 PM
 */
public abstract class DialogBase extends Dialog {
    /**
     * Id will allow us to know which dialog is currently in scope. It is useful in some conditional situations
     */
    private static DialogBase currentDialog;
    private String sourceId;
    private Label messageLabel;
    private StackPane mainContentPane;
    private StackPane processingPane;
    private StackPane messagePane;

    protected static final int DIALOG_AUTO_HIDE_DELAY = PropertyManager.INSTANCE.getProperty("dialog.auto.hide.milliseconds", 5000);
    protected static final int DIALOG_AUTO_HIDE_NONE = -1;    //dialog will not auto hide
    public static DialogBase getCurrentDialog() {
        return currentDialog;
    }

    public DialogBase(String title, boolean lightweight, int autoHideDelay, double width, double height) {
        super(null, title, lightweight);
        try {
            this.setResizable(false);
            this.setClosable(true);
            this.mainContentPane = new StackPane();
            this.processingPane = new StackPane();
            this.messagePane = new StackPane();

            final ImageView loadingWheel = new ImageView(new Image("/images/LoadingWheel.gif"));
            loadingWheel.setPreserveRatio(true);
            loadingWheel.setFitWidth(100);

            StackPane.setAlignment(loadingWheel, Pos.CENTER);
            processingPane.setMaxWidth(350);
            processingPane.setMaxHeight(200);
            processingPane.getChildren().addAll(loadingWheel);
            processingPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");
            processingPane.setVisible(false);


            messageLabel = new Label();
            messageLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
            messageLabel.setMaxWidth(350);
            messageLabel.setMaxHeight(200);
            messageLabel.setWrapText(true);
            messageLabel.setAlignment(Pos.CENTER);
            messageLabel.setTextAlignment(TextAlignment.CENTER);

            StackPane.setAlignment(messageLabel, Pos.CENTER);
            messagePane.setMaxWidth(350);
            messagePane.setMaxHeight(200);
            messagePane.setPadding(new Insets(10));
            messagePane.getChildren().addAll(messageLabel);
            messagePane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");
            messagePane.setVisible(false);

            // This will listen on the message pane visible property.
            // When it changes to true, it will auto hide the dialog after the delay time as elapsed
            ChangeListener<Boolean> messagePaneListener = (observable, oldValue, newValue) -> {
                if (newValue) {
                    new Timer().schedule(new TimerTask() {
                        public void run() {
                            Platform.runLater(() -> {
                                showMainContent();
                                if (autoHideDelay > 0) {
                                    hide();
                                }
                            });
                        }
                    }, PropertyManager.INSTANCE.getProperty("dialog.auto.hide.milliseconds", 5000));
                }
            };

            // Add the listener to the "visible" property
            messagePane.visibleProperty().addListener(messagePaneListener);


            final StackPane dialogContent = new StackPane();
            dialogContent.setPadding(new Insets(0, 0, 0, 0));


            dialogContent.getChildren().add(0, mainContentPane);
            dialogContent.getChildren().add(1, processingPane);
            dialogContent.getChildren().add(2, messagePane);
            dialogContent.setSnapToPixel(true);
            dialogContent.snappedTopInset();
            dialogContent.snappedBottomInset();
            dialogContent.setAlignment(Pos.CENTER);

            if (height > 0) {
                dialogContent.setMinHeight(height);
                dialogContent.setPrefHeight(height);
            }

            if (width > 0) {
                dialogContent.setMinWidth(width);
                dialogContent.setPrefWidth(width);
            }

            this.setContent(dialogContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param event - The event that initiated the show of the dialog
     *              For example, the click of an image may have initiated the show of the dialog, we would
     *              pass the id of the image.  This is useful in determining what action to take within the dialog.
     * @return Action
     */
    public org.controlsfx.control.action.Action show(Event event) {
        System.out.println("HERE I AM!!!!!");
        currentDialog = this;
        try {
            if (event != null) {
                this.sourceId = ((Node) event.getSource()).getId();
            }
            this.showMainContent();
            if (event != null) {
                event.consume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.show();
    }

    @Override
    public void hide() {
        if(currentDialog != null && currentDialog.equals(this)){
            super.hide();
            currentDialog = null;
        }
    }

    public static void hideCurrentDialog() {
        if(currentDialog != null){
            currentDialog.hide();        }
    }



    /**
     * Set the main content of the Dialog
     *
     * @param content Node
     */
    public void setMainContentLayer(Node content) {
        content.addEventHandler(KeyEvent.KEY_PRESSED, this::escapeKeyEventHandler);

        mainContentPane.getChildren().removeAll();
        mainContentPane.getChildren().add(0, content);
        Platform.runLater(() -> mainContentPane.requestFocus());
    }

    /**
     * Set the main content of the Dialog at a specific index
     *
     * @param content Node
     */
    public void setMainContentLayer(int index, Node content) {
        content.addEventHandler(KeyEvent.KEY_PRESSED, this::escapeKeyEventHandler);

        if(mainContentPane.getChildren().size() > index){
            mainContentPane.getChildren().remove(index);
        }

        mainContentPane.getChildren().add(index, content);
        Platform.runLater(() -> mainContentPane.requestFocus());
    }

    /**
     * This will add the node to the main content stackpane at the given index
     *
     * @param content Node
     */
    public void addMainContentLayer(Node content) {
        content.addEventHandler(KeyEvent.KEY_PRESSED, this::escapeKeyEventHandler);
        mainContentPane.getChildren().add(content);
        Platform.runLater(() -> mainContentPane.requestFocus());
    }


    /**
     * This will show the processing pane
     */
    public void showProcessing() {
        mainContentPane.setOpacity(0.3);    // main content pane
        processingPane.setVisible(true);    // processing pane
        messagePane.setVisible(false);      // message pane
    }

    /**
     * This will show the main content pane
     */
    public void showMainContent() {
        System.out.println("Now I'm here!!");
        Platform.runLater(() -> {
            ((StackPane) this.getContent()).getChildren().get(0).requestFocus();
            mainContentPane.setOpacity(1.0);    // main content pane
            mainContentPane.setVisible(true);   // main content pane

            processingPane.setVisible(false);   // processing pane
            messagePane.setVisible(false);      // message pane
        });
    }

    /**
     * Show message pane and then close after the delay
     *
     * @param message String
     */
    public void showMessage(String message) {
        messageLabel.setText(message);

        mainContentPane.setOpacity(0.3);    // main content pane
        processingPane.setVisible(false);   // processing pane
        messagePane.setVisible(true);       // message pane
    }

    public String getSourceId() {
        return sourceId;
    }

    public abstract void executePrimaryAction();

    private final void escapeKeyEventHandler(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE:
                this.hide();
                break;
            case ENTER:
                this.executePrimaryAction();
                break;
        }
    }

    public StackPane getMainContentPane() {
        return this.mainContentPane;
    }

}
