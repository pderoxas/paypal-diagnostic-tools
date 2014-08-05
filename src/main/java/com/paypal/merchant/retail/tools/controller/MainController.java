package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.client.SdkClient;
import com.paypal.merchant.retail.tools.exception.ClientException;
import com.paypal.merchant.retail.tools.model.Stat;
import com.paypal.merchant.retail.tools.util.OneTimeTaskScheduler;
import com.paypal.merchant.retail.tools.util.RepeatingTaskScheduler;
import com.paypal.merchant.retail.tools.util.TaskScheduler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paolo on 7/21/2014.
 */
public class MainController implements Initializable, ManagedPane {
    private static Logger logger = LoggerFactory.getLogger(MainController.class);
    private PaneManager paneManager;

    public static final String LINE_CHART_PANE = "lineChart";
    public static final String LINE_CHART_PANE_FXML = "/fxml/lineChartScreen.fxml";

    public static final String BAR_CHART_PANE = "processRefund";
    public static final String BAR_CHART_PANE_FXML = "/fxml/barChartScreen.fxml";

    private DateFormat formatter = new SimpleDateFormat("HH:mm");

    private TaskScheduler oneTimeTaskScheduler;
    private Map<String, Runnable> oneTimeRunnableMap = new HashMap<>();

    private TaskScheduler repeatingTaskScheduler;
    private Map<String, Runnable> repeatingRunnableMap = new HashMap<>();

    @FXML
    private HBox mainHBox;

    @FXML
    private Pane processingPane;

    @FXML
    private ImageView img_processing;

    @FXML
    private Label lbl_getLocationAvg, lbl_setLocationAvailAvg, lbl_authorizeAvg, lbl_voidAvg;

    @FXML
    private Label lbl_getLocationSingle, lbl_setLocationAvailSingle, lbl_authorizeSingle, lbl_voidSingle;

    @FXML
    private CheckBox chk_getLocation, chk_openLocation, chk_authorize, chk_void;

    @FXML
    private CheckBox chk_getLocationSingle, chk_openLocationSingle, chk_authorizeSingle, chk_voidSingle;

    @FXML
    private ChoiceBox<Integer> cb_interval, cb_duration, cb_timeoutValue, cb_sampleSize;

    @FXML
    private MenuBar mb_menuBar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logger.debug("initializing...");

            oneTimeRunnableMap.put("GetLocation", getLocationOneTimeRunnable);
            oneTimeRunnableMap.put("SetLocationAvailability", openLocationOneTimeRunnable);
            oneTimeRunnableMap.put("AuthorizeAndVoid", authorizeAndVoidOneTimeRunnable);

            repeatingRunnableMap.put("GetLocation", getLocationRepeatingRunnable);
            repeatingRunnableMap.put("SetLocationAvailability", openLocationRepeatingRunnable);
            repeatingRunnableMap.put("AuthorizeAndVoid", authorizeAndVoidRepeatingRunnable);

            oneTimeTaskScheduler = new OneTimeTaskScheduler(oneTimeRunnableMap, 0,
                    oneTimeRunnableMap.size(),  TimeUnit.MINUTES);

            paneManager = new PaneManager();
            paneManager.setId("rootPaneManager");
            paneManager.loadPane(LINE_CHART_PANE, LINE_CHART_PANE_FXML);
            paneManager.loadPane(BAR_CHART_PANE, BAR_CHART_PANE_FXML);
            paneManager.setPane(LINE_CHART_PANE);
            mainHBox.getChildren().add(paneManager);

            final Image processing = new Image(Main.class.getResourceAsStream("/images/LoadingWheel.gif"));
            img_processing.setImage(processing);

            initializeMenuBar();
            initializeCheckboxes();
            initializeChoiceBoxes();

        } catch (Exception e) {
            logger.error("Failed to initialize SDK Tool! ", e);
        }
    }

    private void initializeCheckboxes() {
        chk_getLocation.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.addGetLocation();
            } else {
                LineChartScreenController.removeGetLocation();
            }
        });

        chk_openLocation.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.addSetLocationAvailability();
            } else {
                LineChartScreenController.removeSetLocationAvailability();
            }
        });

        chk_authorize.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.addAuthorize();
            } else {
                LineChartScreenController.removeAuthorize();
            }
        });

        chk_void.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.addVoid();
            } else {
                LineChartScreenController.removeVoid();
            }
        });




        chk_getLocationSingle.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.addGetLocationSingle();
            } else {
                LineChartScreenController.removeGetLocationSingle();
            }
        });

        chk_openLocationSingle.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.addOpenLocationSingle();
            } else {
                LineChartScreenController.removeOpenLocationSingle();
            }
        });

        chk_authorizeSingle.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.addAuthorizeSingle();
            } else {
                LineChartScreenController.removeAuthorizeSingle();
            }
        });

        chk_voidSingle.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.addVoidSingle();
            } else {
                LineChartScreenController.removeVoidSingle();
            }
        });
    }

    private void initializeChoiceBoxes() {
        // Interval in minutes
        cb_interval.getItems().clear();
        cb_interval.getItems().addAll(1, 5, 15, 30);
        cb_interval.getSelectionModel().select(0);

        // Duration in minutes
        cb_duration.getItems().clear();
        cb_duration.getItems().addAll(5, 30, 60, 90, 120);
        cb_duration.getSelectionModel().select(0);

        // Timeout in seconds
        cb_timeoutValue.getItems().clear();
        cb_timeoutValue.getItems().addAll(15, 30, 60);
        cb_timeoutValue.getSelectionModel().select(0);

        // Sample size - whole numbers
        cb_sampleSize.getItems().clear();
        cb_sampleSize.getItems().addAll(30, 60, 90);
        cb_sampleSize.getSelectionModel().select(0);
    }

    private void initializeMenuBar() {
        Menu fileMenu = new Menu("File");
        MenuItem newFileMenuItem = new MenuItem("Clear");
        newFileMenuItem.setOnAction(clearLineChart());

        MenuItem saveFileMenuItem = new MenuItem("Save");
        saveFileMenuItem.setDisable(true);

        MenuItem openFileMenuItem = new MenuItem("Open");
        openFileMenuItem.setDisable(true);
        fileMenu.getItems().addAll(newFileMenuItem, saveFileMenuItem, openFileMenuItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutHelpeMenuItem = new MenuItem("About");
        helpMenu.getItems().addAll(aboutHelpeMenuItem);

        mb_menuBar.getMenus().addAll(fileMenu, helpMenu);

    }

    private EventHandler<ActionEvent> clearLineChart() {
        return event -> LineChartScreenController.clearAll();
    }

    @Override
    public void setParent(PaneManager paneManager) {
        this.paneManager = paneManager;
    }

    /**
     * Handles the manage store event
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleSend(ActionEvent event) {
        logger.debug("Start");
        oneTimeTaskScheduler.start();
        //oneTimeTaskScheduler.stop();
        event.consume();
    }

    /**
     * Handles the manage store event
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleStart(ActionEvent event) {
        logger.debug("Start");
        repeatingTaskScheduler = new RepeatingTaskScheduler(repeatingRunnableMap, 0,
                cb_interval.getSelectionModel().getSelectedItem(),
                repeatingRunnableMap.size(), TimeUnit.MINUTES,
                cb_duration.getSelectionModel().getSelectedItem() * 60000);
        repeatingTaskScheduler.start();
        event.consume();
    }

    /**
     * Handles the manage store event
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleStop(ActionEvent event) {
        logger.debug("Stop");
        repeatingTaskScheduler.stop();
        event.consume();
    }

    /**
     * This set the location availability via the SDK
     */
    public final Runnable getLocationOneTimeRunnable = () -> {
        try {
            SdkClient.INSTANCE.getSdkLocation(cb_timeoutValue.getSelectionModel().getSelectedItem(), false);
        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
        } finally {
            plotSingleStat(LineChartScreenController.getLocationDataSingle, lbl_getLocationSingle, SdkClient.INSTANCE.getGetLocationStat());
        }
    };

    /**
     * This sets the location availability via the SDK
     */
    public final Runnable openLocationOneTimeRunnable = () -> {
        try {
            SdkClient.INSTANCE.setLocationAvailability(true, cb_timeoutValue.getSelectionModel().getSelectedItem(), false);
        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
        } finally {
            plotSingleStat(LineChartScreenController.openLocationDataSingle, lbl_setLocationAvailSingle, SdkClient.INSTANCE.getOpenLocationStat());
        }
    };

    /**
     * This gets an authorization then voids it
     */
    public final Runnable authorizeAndVoidOneTimeRunnable = () -> {
        try {
            BigDecimal amount = BigDecimal.valueOf(0.01);
            String payCode = "6506000136356447";

            String authorizationTransactionId = SdkClient.INSTANCE.getAuthorizationTransactionId(payCode, amount, cb_timeoutValue.getSelectionModel().getSelectedItem(), false);
            logger.debug("Approval Code: " + authorizationTransactionId);

            String voidAuthCode = SdkClient.INSTANCE.getVoidAuthorizationId(authorizationTransactionId, cb_timeoutValue.getSelectionModel().getSelectedItem(), false);
            logger.debug("Void Authorization Code: " + voidAuthCode);

        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
        } finally {
            plotSingleStat(LineChartScreenController.authorizeDataSingle, lbl_authorizeSingle, SdkClient.INSTANCE.getAuthorizationStat());
            plotSingleStat(LineChartScreenController.voidDataSingle, lbl_voidSingle, SdkClient.INSTANCE.getVoidStat());
        }
    };

    /**
     * This set the location availability via the SDK
     */
    public final Runnable getLocationRepeatingRunnable = () -> {
        try {
            SdkClient.INSTANCE.getSdkLocation(cb_timeoutValue.getSelectionModel().getSelectedItem(), true);
        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
        } finally {
            plotLastStat(LineChartScreenController.getLocationData, lbl_getLocationAvg, SdkClient.INSTANCE.getGetLocationStats());
        }
    };

    /**
     * This sets the location availability via the SDK
     */
    public final Runnable openLocationRepeatingRunnable = () -> {
        try {
            SdkClient.INSTANCE.setLocationAvailability(true, cb_timeoutValue.getSelectionModel().getSelectedItem(), true);
        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
        } finally {
            plotLastStat(LineChartScreenController.openLocationData, lbl_setLocationAvailAvg, SdkClient.INSTANCE.getOpenLocationStats());
        }
    };

    /**
     * This gets an authorization then voids it
     */
    public final Runnable authorizeAndVoidRepeatingRunnable = () -> {
        try {
            BigDecimal amount = BigDecimal.valueOf(0.01);
            String payCode = "6506000136356447";

            String authorizationTransactionId = SdkClient.INSTANCE.getAuthorizationTransactionId(payCode, amount, cb_timeoutValue.getSelectionModel().getSelectedItem(), true);
            logger.debug("Approval Code: " + authorizationTransactionId);

            String voidAuthCode = SdkClient.INSTANCE.getVoidAuthorizationId(authorizationTransactionId, cb_timeoutValue.getSelectionModel().getSelectedItem(), true);
            logger.debug("Void Authorization Code: " + voidAuthCode);

        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
        } finally {
            plotLastStat(LineChartScreenController.authorizeData, lbl_authorizeAvg, SdkClient.INSTANCE.getAuthorizationStats());
            plotLastStat(LineChartScreenController.voidData, lbl_voidAvg, SdkClient.INSTANCE.getVoidStats());
        }
    };

    private void plotSingleStat(XYChart.Series series, Label elapsedTimeLabel, Stat stat) {
        Date date = new Date(stat.getStartTime());
        String xValue = formatter.format(date);
        long yValue = stat.getElapsedTime() / 1000000;  // get milliseconds
        Platform.runLater(() -> {
            if(series.getData().size() > 0) {
                series.getData().remove(0);
            }

            series.getData().add(new XYChart.Data(xValue, yValue));
            elapsedTimeLabel.setText(String.valueOf(yValue));
        });
    }

    private void plotLastStat(XYChart.Series series, Label avgElapsedTimeLabel, List<Stat> stats) {

        if(stats.size() > cb_sampleSize.getSelectionModel().getSelectedItem()) {
            stats.remove(0);
        }

        if (stats.size() == 0) {
            return;
        }

        Stat lastStat = stats.get(stats.size() - 1);
        Date date = new Date(lastStat.getStartTime());
        String xValue = formatter.format(date);
        long yValue = lastStat.getElapsedTime() / 1000000;  // get milliseconds

        long total = 0;
        for (Stat stat : stats) {
            total = total + stat.getElapsedTime();
        }
        long avgElapsedTime = (total / stats.size()) / 1000000;  // get milliseconds

        Platform.runLater(() -> {
            series.getData().add(new XYChart.Data(xValue, yValue));
            avgElapsedTimeLabel.setText(String.valueOf(avgElapsedTime));

            if(series.getData().size() > cb_sampleSize.getSelectionModel().getSelectedItem()) {
                series.getData().remove(0);
            }
        });
    }



    public void showProcessing() {
        processingPane.setVisible(true);
    }

    public void hideProcessing() {
        processingPane.setVisible(false);
    }

}
