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

    public static XYChart.Series getLocationData = new XYChart.Series();
    public static XYChart.Series openLocationData = new XYChart.Series();
    public static XYChart.Series authorizeData = new XYChart.Series();
    public static XYChart.Series voidData = new XYChart.Series();

    public static XYChart.Series getLocationDataSingle = new XYChart.Series();
    public static XYChart.Series openLocationDataSingle = new XYChart.Series();
    public static XYChart.Series authorizeDataSingle = new XYChart.Series();
    public static XYChart.Series voidDataSingle = new XYChart.Series();

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
            getLocationData.setName("Get Location (repeating)");
            openLocationData.setName("Open Location (repeating)");
            authorizeData.setName("Authorization (repeating)");
            voidData.setName("Void (repeating)");

            getLocationDataSingle.setName("Get Location (one-time)");
            openLocationDataSingle.setName("Open Location (one-time)");
            authorizeDataSingle.setName("Authorization (one-time)");
            voidDataSingle.setName("Void (one-time)");

            xAxis.setAnimated(true);
            yAxis.setAnimated(true);
            lineChart.setAnimated(true);

            lineChart.getData().addAll(authorizeData, voidData, getLocationData, openLocationData);
            lineChart.getData().addAll(authorizeDataSingle, voidDataSingle, getLocationDataSingle, openLocationDataSingle);
            lineChartPane.getChildren().add(lineChart);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void addAuthorize() {
        addData(authorizeData);
    }
    public static void removeAuthorize() {
        removeData(authorizeData);
    }
    public static void addVoid() {
        addData(voidData);
    }
    public static void removeVoid() {
        removeData(voidData);
    }
    public static void addGetLocation() {
        addData(getLocationData);
    }
    public static void removeGetLocation() {
        removeData(getLocationData);
    }
    public static void addSetLocationAvailability() {
        addData(openLocationData);
    }
    public static void removeSetLocationAvailability() {
        removeData(openLocationData);
    }
    public static void addAuthorizeSingle() {
        addData(authorizeDataSingle);
    }
    public static void removeAuthorizeSingle() {
        removeData(authorizeDataSingle);
    }
    public static void addVoidSingle() {
        addData(voidDataSingle);
    }
    public static void removeVoidSingle() {
        removeData(voidDataSingle);
    }
    public static void addGetLocationSingle() {
        addData(getLocationDataSingle);
    }
    public static void removeGetLocationSingle() {
        removeData(getLocationDataSingle);
    }
    public static void addOpenLocationSingle() {
        addData(openLocationDataSingle);
    }
    public static void removeOpenLocationSingle() {
        removeData(openLocationDataSingle);
    }

    public static void clearAll() {
        getLocationData.getData().clear();
        openLocationData.getData().clear();
        authorizeData.getData().clear();
        voidData.getData().clear();

        getLocationDataSingle.getData().clear();
        openLocationDataSingle.getData().clear();
        authorizeDataSingle.getData().clear();
        voidDataSingle.getData().clear();
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
