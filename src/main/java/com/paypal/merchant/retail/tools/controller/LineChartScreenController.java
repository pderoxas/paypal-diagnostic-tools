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

    public static XYChart.Series getLocationStats = new XYChart.Series();
    public static XYChart.Series setLocationAvailabilityStats = new XYChart.Series();

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
            final LineChart<String,Number> lineChart = new LineChart<>(xAxis,yAxis);

            lineChart.setTitle("SDK Performance");
            lineChart.setPrefWidth(900);
            lineChart.setPrefHeight(550);
            getLocationStats.setName("Get Location");
            setLocationAvailabilityStats.setName("Set Location Availability");

            xAxis.setAnimated(true);
            yAxis.setAnimated(true);
            lineChart.setAnimated(true);

            lineChart.getData().addAll(getLocationStats, setLocationAvailabilityStats);
            lineChartPane.getChildren().add(lineChart);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
