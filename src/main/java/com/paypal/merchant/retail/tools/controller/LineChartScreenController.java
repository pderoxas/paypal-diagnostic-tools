package com.paypal.merchant.retail.tools.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Paolo on 7/21/2014.
 *
 */
public class LineChartScreenController implements Initializable, ManagedPane {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final static XYChart.Series REPEATING_GET_LOCATION_PLOTS = new XYChart.Series();
    public final static XYChart.Series REPEATING_OPEN_LOCATION_PLOTS = new XYChart.Series();
    public final static XYChart.Series REPEATING_AUTHORIZE_PLOTS = new XYChart.Series();
    public final static XYChart.Series REPEATING_VOID_PLOTS = new XYChart.Series();

    public final static XYChart.Series ONE_TIME_GET_LOCATION_PLOT = new XYChart.Series();
    public final static XYChart.Series ONE_TIME_OPEN_LOCATION_PLOT = new XYChart.Series();
    public final static XYChart.Series ONE_TIME_AUTHORIZE_PLOT = new XYChart.Series();
    public final static XYChart.Series ONE_TIME_VOID_PLOT = new XYChart.Series();

    private static LineChart<String,Number> lineChart;
    @FXML
    Pane lineChartPane;

    @Override
    public void setParent(PaneManager paneManager) {
        PaneManager paneManager1 = paneManager;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Time");
            lineChart = new LineChart<>(xAxis,yAxis);

            lineChart.setTitle("SDK Response Times");
            lineChart.setPrefWidth(900);
            lineChart.setPrefHeight(577);

            // Set Key Names
            REPEATING_GET_LOCATION_PLOTS.setName("Get Location (repeating)");
            REPEATING_OPEN_LOCATION_PLOTS.setName("Open Location (repeating)");
            REPEATING_AUTHORIZE_PLOTS.setName("Authorization (repeating)");
            REPEATING_VOID_PLOTS.setName("Void (repeating)");

            ONE_TIME_GET_LOCATION_PLOT.setName("Get Location (one-time)");
            ONE_TIME_OPEN_LOCATION_PLOT.setName("Open Location (one-time)");
            ONE_TIME_AUTHORIZE_PLOT.setName("Authorization (one-time)");
            ONE_TIME_VOID_PLOT.setName("Void (one-time)");

            xAxis.setAnimated(true);
            yAxis.setAnimated(true);
            lineChart.setAnimated(true);

            lineChart.getData().addAll(REPEATING_AUTHORIZE_PLOTS, REPEATING_VOID_PLOTS, REPEATING_GET_LOCATION_PLOTS, REPEATING_OPEN_LOCATION_PLOTS);
            lineChart.getData().addAll(ONE_TIME_AUTHORIZE_PLOT, ONE_TIME_VOID_PLOT, ONE_TIME_GET_LOCATION_PLOT, ONE_TIME_OPEN_LOCATION_PLOT);
            lineChartPane.getChildren().add(lineChart);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void showAuthorize() {
        addData(REPEATING_AUTHORIZE_PLOTS);
    }
    public static void hideAuthorize() {
        removeData(REPEATING_AUTHORIZE_PLOTS);
    }
    public static void showVoid() {
        addData(REPEATING_VOID_PLOTS);
    }
    public static void hideVoid() {
        removeData(REPEATING_VOID_PLOTS);
    }
    public static void showGetLocation() {
        addData(REPEATING_GET_LOCATION_PLOTS);
    }
    public static void hideGetLocation() {
        removeData(REPEATING_GET_LOCATION_PLOTS);
    }
    public static void showSetLocationAvailability() {
        addData(REPEATING_OPEN_LOCATION_PLOTS);
    }
    public static void hideSetLocationAvailability() {
        removeData(REPEATING_OPEN_LOCATION_PLOTS);
    }
    public static void showAuthorizeSingle() {
        addData(ONE_TIME_AUTHORIZE_PLOT);
    }
    public static void hideAuthorizeSingle() {
        removeData(ONE_TIME_AUTHORIZE_PLOT);
    }
    public static void showVoidSingle() {
        addData(ONE_TIME_VOID_PLOT);
    }
    public static void hideVoidSingle() {
        removeData(ONE_TIME_VOID_PLOT);
    }
    public static void showGetLocationSingle() {
        addData(ONE_TIME_GET_LOCATION_PLOT);
    }
    public static void hideGetLocationSingle() {
        removeData(ONE_TIME_GET_LOCATION_PLOT);
    }
    public static void showOpenLocationSingle() {
        addData(ONE_TIME_OPEN_LOCATION_PLOT);
    }
    public static void hideOpenLocationSingle() {
        removeData(ONE_TIME_OPEN_LOCATION_PLOT);
    }

    public static void clearAll() {
        REPEATING_GET_LOCATION_PLOTS.getData().clear();
        REPEATING_OPEN_LOCATION_PLOTS.getData().clear();
        REPEATING_AUTHORIZE_PLOTS.getData().clear();
        REPEATING_VOID_PLOTS.getData().clear();

        ONE_TIME_GET_LOCATION_PLOT.getData().clear();
        ONE_TIME_OPEN_LOCATION_PLOT.getData().clear();
        ONE_TIME_AUTHORIZE_PLOT.getData().clear();
        ONE_TIME_VOID_PLOT.getData().clear();
    }


    private static void addData(XYChart.Series data) {
        if(!lineChart.getData().contains(data)){
            lineChart.getData().add(data);
        }
    }
    private static void removeData(XYChart.Series data) {
        if(lineChart.getData().contains(data)){
            lineChart.getData().remove(data);
        }
    }



}
