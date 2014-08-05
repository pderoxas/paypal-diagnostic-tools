package com.paypal.merchant.retail.tools;

import com.paypal.merchant.retail.tools.client.SdkClient;
import com.paypal.merchant.retail.tools.controller.MainController;
import com.paypal.merchant.retail.tools.util.PropertyManager;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pderoxas on 2/26/14.
 * This is the main class of the Demo POS Application
 */
public final class Main extends Application {
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static FXMLLoader fxmlLoader;
    private static Parent root;

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private static final int SPLASH_WIDTH = 687;
    private static final int SPLASH_HEIGHT = 181;

    public static void main(String[] args) {
        logger.info("Launching Merchant SDK Tool");
        try{
            fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));
            Application.launch(args);
        } catch(Exception e) {
            logger.error("Error! ", e);
        }
    }


    public static MainController getController(){
        return fxmlLoader.getController();
    }

    @Override public void init() {
        ImageView splash = new ImageView(new Image("images/splash.png"));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH);
        progressText = new Label("Will get current status of store location . . .");
        splashLayout = new VBox();
        splashLayout.setStyle("-fx-background-color:white;");
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-padding: 5; -fx-background-color: white; -fx-border-width:5; -fx-border-color: linear-gradient(to bottom, #333333, derive(black, 75%));");
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(Stage initStage) throws Exception {
        final Task getLocationTask = new Task() {
            @Override protected Void call() throws InterruptedException {
                updateMessage("Getting Store Location information . . .");

                // TODO: implement some initialization
                SdkClient.INSTANCE.initialize();

//                BigDecimal amount = BigDecimal.valueOf(0.01);
//                String payCode = "6506000136356447";
//                try {
//                    String authorizationTransactionId = SdkClient.INSTANCE.getAuthorizationTransactionId(payCode, amount);
//                    logger.debug("Approval Code: " + authorizationTransactionId);
//
//                    String voidAuthCode = SdkClient.INSTANCE.getVoidAuthorizationId(authorizationTransactionId);
//                    logger.debug("Void Authorization Code: " + voidAuthCode);
//                } catch (ClientException e) {
//                    e.printStackTrace();
//                }

                updateMessage("Success");
                Platform.runLater(() -> showMainStage());
                return null;
            }
        };

        showSplash(initStage, getLocationTask);
        new Thread(getLocationTask).start();
    }

    private static void showMainStage() {
        try{

            //fxmlLoader.setController(controller);

            root = fxmlLoader.load();
            root.setId("rootGroup");

            Scene scene = new Scene(root);
            scene.getStylesheets().add("/styles/main.css");

            Stage mainStage = new Stage(StageStyle.DECORATED);
            mainStage.setScene(scene);
            mainStage.setTitle(PropertyManager.INSTANCE.getProperty("application.title"));
            mainStage.show();
            mainStage.setOnCloseRequest(t -> {
                Platform.exit();
                System.exit(0);
            });
        } catch(Exception e) {
            logger.error("Failed to show Main Stage", e);
        }
    }


    /**
     * Show the splash screen while we get Location information
     * @param initStage
     * @param task
     */
    private void showSplash(final Stage initStage, Task task) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(0.5), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();
            }
        });

        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.show();
    }
}
