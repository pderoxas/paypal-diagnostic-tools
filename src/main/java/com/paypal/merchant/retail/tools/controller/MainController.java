package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.client.SdkClient;
import com.paypal.merchant.retail.tools.control.dialogs.AboutDialog;
import com.paypal.merchant.retail.tools.exception.ClientException;
import com.paypal.merchant.retail.tools.exception.SerializationException;
import com.paypal.merchant.retail.tools.model.Stat;
import com.paypal.merchant.retail.tools.util.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

    //Message Dialog
    private final AboutDialog aboutDialog = new AboutDialog();

    @FXML
    private HBox mainHBox;

    @FXML
    private Pane processingPane;

    @FXML
    private ImageView img_processing;

    @FXML
    private Label lbl_getLocationAvg, lbl_openLocationAvg, lbl_authorizeAvg, lbl_voidAvg;

    @FXML
    private Label lbl_getLocationSingle, lbl_setLocationAvailSingle, lbl_authorizeSingle, lbl_voidSingle;

    @FXML
    private CheckBox chk_getLocation, chk_openLocation, chk_authorize, chk_void;

    @FXML
    private CheckBox chk_getLocationSingle, chk_openLocationSingle, chk_authorizeSingle, chk_voidSingle;

    @FXML
    private ChoiceBox<Integer> cb_interval, cb_duration, cb_timeoutValue, cb_sampleSize;

    @FXML
    private Button btn_start, btn_stop, btn_send;

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
            oneTimeTaskScheduler.addPropertyChangeListener(oneTimeTaskSchedulerChangeListener);


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
            initializeButtons();

        } catch (Exception e) {
            logger.error("Failed to initialize SDK Tool! ", e);
        }
    }

    private void initializeCheckboxes() {
        chk_getLocation.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.showGetLocation();
            } else {
                LineChartScreenController.hideGetLocation();
            }
        });

        chk_openLocation.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.showSetLocationAvailability();
            } else {
                LineChartScreenController.hideSetLocationAvailability();
            }
        });

        chk_authorize.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.showAuthorize();
            } else {
                LineChartScreenController.hideAuthorize();
            }
        });

        chk_void.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.showVoid();
            } else {
                LineChartScreenController.hideVoid();
            }
        });

        chk_getLocationSingle.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.showGetLocationSingle();
            } else {
                LineChartScreenController.hideGetLocationSingle();
            }
        });

        chk_openLocationSingle.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.showOpenLocationSingle();
            } else {
                LineChartScreenController.hideOpenLocationSingle();
            }
        });

        chk_authorizeSingle.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.showAuthorizeSingle();
            } else {
                LineChartScreenController.hideAuthorizeSingle();
            }
        });

        chk_voidSingle.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(new_val) {
                LineChartScreenController.showVoidSingle();
            } else {
                LineChartScreenController.hideVoidSingle();
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
        MenuItem newFileMenuItem = new MenuItem("Clear");
        newFileMenuItem.setOnAction(clearLineChart());

        MenuItem saveFileMenuItem = new MenuItem("Save");
        saveFileMenuItem.setOnAction(saveLineChartData());

        MenuItem loadFileMenuItem = new MenuItem("Load");
        loadFileMenuItem.setDisable(true);

        //File
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(newFileMenuItem, saveFileMenuItem, loadFileMenuItem);

        MenuItem aboutHelpMenuItem = new MenuItem("About");
        aboutHelpMenuItem.setOnAction(showAboutDialog());

        //Help
        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().addAll(aboutHelpMenuItem);

        mb_menuBar.getMenus().addAll(fileMenu, helpMenu);
    }

    private void initializeButtons() {

        EventHandler<Event> onPressedHandler = event -> btn_send.setOpacity(0.5);
        btn_send.setOnMousePressed(onPressedHandler);
        btn_send.setOnTouchPressed(onPressedHandler);

        EventHandler<Event> onReleasedHandler = event -> btn_send.setOpacity(1.0);
        btn_send.setOnMouseReleased(onReleasedHandler);
        btn_send.setOnTouchReleased(onReleasedHandler);
    }

    private EventHandler<ActionEvent> clearLineChart() {
        return event -> LineChartScreenController.clearAll();
    }

    private EventHandler<ActionEvent> saveLineChartData() {
        return event -> {
            try {
                SerializationUtils.serializeToXML(SdkClient.INSTANCE.getLineChartData(), "data/" + System.currentTimeMillis() + "-line.xml");
            } catch (SerializationException e) {
                logger.error("Failed to save LineChart LocationData");
            }
        };
    }

    private EventHandler<ActionEvent> showAboutDialog() {
        return event -> {
            //aboutDialog.show(null, "About", "Version " + PropertyManager.INSTANCE.getProperty("application.version"));
            Dialogs.create()
                    .owner( paneManager)
                    .title("About")
                    .masthead(null)
                    .message( "Version " + PropertyManager.INSTANCE.getProperty("application.version"))
                    .showInformation();
        };
    }



//    private EventHandler<ActionEvent> loadLineChartData(File selectedFile) {
//        return event -> {
//            try {
//                LineChartScreenController.clearAll();
//                Map<String, List<Stat>> lineChartData = SerializationUtils.deserializeFromXML(selectedFile.toPath());
//                for(String key : lineChartData.keySet()) {
//
//                    switch(key) {
//                        case "GetLocation":
//                            plotStats(LineChartScreenController.REPEATING_GET_LOCATION_PLOTS, lbl_getLocationAvg, lineChartData.get(key));
//                            break;
//                        case "OpenLocation":
//                            plotStats(LineChartScreenController.REPEATING_OPEN_LOCATION_PLOTS, lbl_openLocationAvg, lineChartData.get(key));
//                            break;
//                        case "Authorization":
//                            plotStats(LineChartScreenController.REPEATING_AUTHORIZE_PLOTS, lbl_authorizeAvg, lineChartData.get(key));
//                            break;
//                        case "Void":
//                            plotStats(LineChartScreenController.REPEATING_VOID_PLOTS, lbl_voidAvg, lineChartData.get(key));
//                            break;
//                        default:
//                            logger.error("Unknown stat list: " + key);
//                            break;
//                    }
//                }
//            } catch (SerializationException e) {
//                logger.error("Failed to save LineChart LocationData");
//            }
//        };
//    }





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

        repeatingTaskScheduler.addPropertyChangeListener(repeatingTaskSchedulerChangeListener);

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
            plotSingleStat(LineChartScreenController.ONE_TIME_GET_LOCATION_PLOT, lbl_getLocationSingle, SdkClient.INSTANCE.getGetLocationStat());
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
            plotSingleStat(LineChartScreenController.ONE_TIME_OPEN_LOCATION_PLOT, lbl_setLocationAvailSingle, SdkClient.INSTANCE.getOpenLocationStat());
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
            plotSingleStat(LineChartScreenController.ONE_TIME_AUTHORIZE_PLOT, lbl_authorizeSingle, SdkClient.INSTANCE.getAuthorizationStat());
            plotSingleStat(LineChartScreenController.ONE_TIME_VOID_PLOT, lbl_voidSingle, SdkClient.INSTANCE.getVoidStat());
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
            plotLastStat(LineChartScreenController.REPEATING_GET_LOCATION_PLOTS, lbl_getLocationAvg, SdkClient.INSTANCE.getGetLocationStats());
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
            plotLastStat(LineChartScreenController.REPEATING_OPEN_LOCATION_PLOTS, lbl_openLocationAvg, SdkClient.INSTANCE.getOpenLocationStats());
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
            plotLastStat(LineChartScreenController.REPEATING_AUTHORIZE_PLOTS, lbl_authorizeAvg, SdkClient.INSTANCE.getAuthorizationStats());
            plotLastStat(LineChartScreenController.REPEATING_VOID_PLOTS, lbl_voidAvg, SdkClient.INSTANCE.getVoidStats());
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

    private void plotStats(XYChart.Series series, Label avgElapsedTimeLabel, List<Stat> stats) {
        if (stats.size() == 0) {
            return;
        }

        long total = 0;
        for(Stat stat : stats) {
            Date date = new Date(stat.getStartTime());
            String xValue = formatter.format(date);
            long yValue = stat.getElapsedTime() / 1000000;  // get milliseconds
            total = total + stat.getElapsedTime();
            Platform.runLater(() -> {
                series.getData().add(new XYChart.Data(xValue, yValue));
            });
        }

        long avgElapsedTime = (total / stats.size()) / 1000000;  // get milliseconds
        Platform.runLater(() -> {
            avgElapsedTimeLabel.setText(String.valueOf(avgElapsedTime));
        });
    }


    private PropertyChangeListener repeatingTaskSchedulerChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (propertyChangeEvent.getPropertyName().equals("isStarted")) {
                Platform.runLater(() -> {
                    btn_start.setDisable((Boolean) propertyChangeEvent.getNewValue());
                    btn_stop.setDisable(! (Boolean) propertyChangeEvent.getNewValue());
                });
            }
        }
    };

    private PropertyChangeListener oneTimeTaskSchedulerChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (propertyChangeEvent.getPropertyName().equals("isStarted")) {
                Platform.runLater(() -> {
                    btn_send.setDisable((Boolean) propertyChangeEvent.getNewValue());
                });
            }
        }
    };





    public void showProcessing() {
        processingPane.setVisible(true);
    }

    public void hideProcessing() {
        processingPane.setVisible(false);
    }

}
