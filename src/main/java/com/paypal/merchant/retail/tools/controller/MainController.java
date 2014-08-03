package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.client.SdkClient;
import com.paypal.merchant.retail.tools.exception.ClientException;
import com.paypal.merchant.retail.tools.util.RepeatingTaskScheduler;
import com.paypal.merchant.retail.tools.util.TaskScheduler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private TaskScheduler taskScheduler;

    private List<Long> getLocationElapsedTimes = new ArrayList<>();
    private List<Long> setLocationAvailabilityElapsedTimes = new ArrayList<>();
    @FXML
    private HBox mainHBox;

    @FXML
    private Pane processingPane;

    @FXML
    private ImageView img_processing;

    @FXML
    private Label lbl_getLocationAvg, lbl_setLocationAvailAvg;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logger.debug("initializing...");

            Map<String, Runnable> runnableMap = new HashMap<>(1);
            runnableMap.put("GetLocation", getLocationRunnable);
            runnableMap.put("SetLocationAvailability", setLocationAvailabilityRunnable);
            taskScheduler = new RepeatingTaskScheduler(runnableMap, 0, 1, 2, TimeUnit.MINUTES);

            paneManager = new PaneManager();
            paneManager.setId("rootPaneManager");
            paneManager.loadPane(LINE_CHART_PANE, LINE_CHART_PANE_FXML);
            paneManager.loadPane(BAR_CHART_PANE, BAR_CHART_PANE_FXML);
            paneManager.setPane(LINE_CHART_PANE);
            mainHBox.getChildren().add(paneManager);

            final Image processing = new Image(Main.class.getResourceAsStream("/images/LoadingWheel.gif"));
            img_processing.setImage(processing);

        } catch (Exception e) {
            logger.error("Failed to initialize SDK Tool! ", e);
        }
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
    protected void handleStart(ActionEvent event) {
        logger.debug("Start");
        taskScheduler.start();
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
        taskScheduler.stop();
        event.consume();
    }

    /**
     * This set the location availability via the SDK
     */
    public final Runnable getLocationRunnable = () -> {
        long startTimestamp = System.currentTimeMillis();
        long startTime = System.nanoTime();
        long elapsedTime = 0;
        try {
            SdkClient.INSTANCE.getSdkLocation();
        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
        } finally {
            elapsedTime = System.nanoTime() - startTime;
            logger.debug("adding new Stat to getLocationStats");
            try {
                Date date = new Date(startTimestamp);
                String xValue = formatter.format(date);
                long yValue = elapsedTime / 1000000;

                getLocationElapsedTimes.add(yValue);
                Platform.runLater(() -> {
                    LineChartScreenController.getLocationStats.getData().add(new XYChart.Data(xValue, yValue));
                    lbl_getLocationAvg.setText(getAverage(getLocationElapsedTimes) + " ms");
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    };

    /**
     * This set the location availability via the SDK
     */
    public final Runnable setLocationAvailabilityRunnable = () -> {
        long startTimestamp = System.currentTimeMillis();
        long startTime = System.nanoTime();
        long elapsedTime = 0;
        try {
            SdkClient.INSTANCE.setLocationAvailability(Main.getLocation(), true);
        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
        } finally {
            elapsedTime = System.nanoTime() - startTime;
            logger.debug("adding new Stat to getLocationStats");
            try {
                Date date = new Date(startTimestamp);
                String xValue = formatter.format(date);
                long yValue = elapsedTime / 1000000;
                setLocationAvailabilityElapsedTimes.add(yValue);
                Platform.runLater(() -> {
                    LineChartScreenController.setLocationAvailabilityStats.getData().add(new XYChart.Data(xValue, yValue));
                    lbl_setLocationAvailAvg.setText(getAverage(setLocationAvailabilityElapsedTimes) + " ms");
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    };

    private long getAverage(List<Long> values) {
        if(values.size() == 0) {
            return 0;
        }
        long total = 0;
        for(Long value : values) {
            total = total + value;
        }
        return total/values.size();
    }

    public void showProcessing() {
        processingPane.setVisible(true);
    }

    public void hideProcessing() {
        processingPane.setVisible(false);
    }

}
