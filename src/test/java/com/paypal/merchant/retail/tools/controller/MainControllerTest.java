package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.sdk.contract.entities.Address;
import com.paypal.merchant.retail.sdk.contract.entities.Location;
import com.paypal.merchant.retail.sdk.internal.entities.AddressImpl;
import com.paypal.merchant.retail.sdk.internal.entities.LocationImpl;
import com.paypal.merchant.retail.tools.JavaFXThreadingRule;
import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.util.PropertyManager;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MainControllerTest {

    @org.junit.Rule
    public JavaFXThreadingRule rule = new JavaFXThreadingRule();

    static Stage primaryStage;
    static PaneManager paneManager;
    static Pane unitUnderTest;
    static Location sdkLocation;

    private static final String PANE = "UUT";
    private static final String PANE_FXML = "/fxml/main.fxml";

    @Before
    public void setUp() throws Exception {

        primaryStage = new Stage();
        paneManager = new PaneManager();

        sdkLocation = new LocationImpl();
        sdkLocation.setId("TestLocationId");
        sdkLocation.setStoreId("TestStoreId");
        sdkLocation.setPhoneNumber("800-555-1234");

        Address address = new AddressImpl();
        address.setLine1("123 Main St");
        address.setLine2("Suite 200");
        address.setCity("Providence");
        address.setState("RI");
        address.setPostalCode("02903");
        sdkLocation.setAddress(address);

        Group root = new Group();
        root.getChildren().addAll(paneManager);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/main.css");

        Stage primaryStage = new Stage();
        primaryStage.setScene(scene);
        primaryStage.setTitle(PropertyManager.INSTANCE.getProperty("application.title"));
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }

    @After
    public void tearDown() throws Exception {
        primaryStage.close();
    }

    @Test
    public void testMainFxmlWithAddressLine2() {

        Main.setLocation(sdkLocation);

        paneManager.loadPane(PANE, PANE_FXML);
        paneManager.setPane(PANE);
        unitUnderTest = (Pane) paneManager.getPane(PANE);

        assertTrue(unitUnderTest.isVisible());

        Label storeId = (Label) unitUnderTest.lookup("#lbl_storeId");
        assertEquals("TestStoreId", storeId.getText());

        Label locationId = (Label) unitUnderTest.lookup("#lbl_locationId");
        assertEquals("TestLocationId", locationId.getText());

        Label addressLine1 = (Label) unitUnderTest.lookup("#lbl_addressLine1");
        assertEquals("123 Main St", addressLine1.getText());

        Label addressLine2 = (Label) unitUnderTest.lookup("#lbl_addressLine2");
        assertEquals("Suite 200", addressLine2.getText());

        Label addressLine3 = (Label) unitUnderTest.lookup("#lbl_addressLine3");
        assertEquals("Providence, RI", addressLine3.getText());

        Label addressLine4 = (Label) unitUnderTest.lookup("#lbl_addressLine4");
        assertEquals("02903", addressLine4.getText());

        Label phone = (Label) unitUnderTest.lookup("#lbl_phone");
        assertEquals("800-555-1234", phone.getText());

        Label manager = (Label) unitUnderTest.lookup("#lbl_manager");
        assertEquals("Joseph Smith", manager.getText());
    }

    @Test
    public void testMainFxmlWithoutAddressLine2() {
        sdkLocation.getAddress().setLine2(null);
        Main.setLocation(sdkLocation);

        paneManager.loadPane(PANE, PANE_FXML);
        paneManager.setPane(PANE);
        unitUnderTest = (Pane) paneManager.getPane(PANE);

        assertTrue(unitUnderTest.isVisible());

        Label storeId = (Label) unitUnderTest.lookup("#lbl_storeId");
        assertEquals("TestStoreId", storeId.getText());

        Label locationId = (Label) unitUnderTest.lookup("#lbl_locationId");
        assertEquals("TestLocationId", locationId.getText());

        Label addressLine1 = (Label) unitUnderTest.lookup("#lbl_addressLine1");
        assertEquals("123 Main St", addressLine1.getText());

        Label addressLine2 = (Label) unitUnderTest.lookup("#lbl_addressLine2");
        assertEquals("Providence, RI", addressLine2.getText());

        Label addressLine3 = (Label) unitUnderTest.lookup("#lbl_addressLine3");
        assertEquals("02903", addressLine3.getText());

        Label addressLine4 = (Label) unitUnderTest.lookup("#lbl_addressLine4");
        assertNull(addressLine4.getText());

        Label phone = (Label) unitUnderTest.lookup("#lbl_phone");
        assertEquals("800-555-1234", phone.getText());

        Label manager = (Label) unitUnderTest.lookup("#lbl_manager");
        assertEquals("Joseph Smith", manager.getText());
    }
}