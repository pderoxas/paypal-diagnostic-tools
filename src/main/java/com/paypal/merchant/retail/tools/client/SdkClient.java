package com.paypal.merchant.retail.tools.client;

import com.paypal.merchant.retail.log4jLogger.LogManager;
import com.paypal.merchant.retail.sdk.contract.PayPalMerchantRetailSDK;
import com.paypal.merchant.retail.sdk.contract.commands.*;
import com.paypal.merchant.retail.sdk.contract.entities.*;
import com.paypal.merchant.retail.sdk.internal.commands.PayPalMerchantRetailSDKImpl;
import com.paypal.merchant.retail.sdk.internal.deprecated.VoidCommand;
import com.paypal.merchant.retail.sdk.internal.deprecated.VoidRequest;
import com.paypal.merchant.retail.sdk.internal.deprecated.VoidResponse;
import com.paypal.merchant.retail.tools.exception.ClientException;
import com.paypal.merchant.retail.tools.model.Stat;
import com.paypal.merchant.retail.tools.util.CallableWithTimeout;
import com.paypal.merchant.retail.tools.util.PropertyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paolo on 7/23/2014.
 *
 */
public enum SdkClient {
    INSTANCE;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private CommandBuilder commandBuilder;
    private String locationId;
    private final String STORE_ID = PropertyManager.INSTANCE.getProperty("sdk.store.id");
    //private static final long TIMEOUT_INTERVAL = PropertyManager.INSTANCE.getProperty("sdk.service.timeout.seconds", 30);
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
    private static final Random RANDOM = new Random();

    // Response Time Lists
    private Stat getLocationStat = new Stat();
    private Stat openLocationStat = new Stat();
    private Stat authorizationStat = new Stat();
    private Stat voidStat = new Stat();

    private List<Stat> getLocationStats = new ArrayList<>();
    private List<Stat> openLocationStats = new ArrayList<>();
    private List<Stat> authorizationStats = new ArrayList<>();
    private List<Stat> voidStats = new ArrayList<>();

    private SdkClient() {
        try {
            logger.debug("Loading Config.xml for PayPal Merchant SDK");
            Source sdkConfig = new StreamSource(this.getClass().getClassLoader().getResourceAsStream("Config.xml"));

            logger.info("Creating new instance of PayPalMerchantRetailSDKImpl | StoreId: " + STORE_ID);
            PayPalMerchantRetailSDKImpl sdkImpl =(PayPalMerchantRetailSDKImpl) PayPalMerchantRetailSDK.newInstance(sdkConfig);

            sdkImpl.registerLogManager(LogManager.newInstance());
            logger.info("Successfully created a new instance of PayPalMerchantRetailSDKImpl | StoreId: " + STORE_ID);

            locationId = sdkImpl.getSdkConfig().getStoreConfig(STORE_ID).getLocationId();

            CommandBuilderContext builderContext = CommandBuilderContext.newInstance();
            builderContext.setStoreId(STORE_ID);
            commandBuilder = sdkImpl.newCommandBuilder(builderContext);

            logger.debug("Finished initializing SdkClient");
        } catch (Exception e) {
            logger.error("Failed to initialize the SdkClient singleton");
        }
    }

    public void initialize() {
        logger.info("Initializing . . .");
    }

    public Stat getGetLocationStat() {
        return getLocationStat;
    }

    public Stat getOpenLocationStat() {
        return openLocationStat;
    }

    public Stat getAuthorizationStat() {
        return authorizationStat;
    }

    public Stat getVoidStat() {
        return voidStat;
    }

    public List<Stat> getGetLocationStats() {
        return getLocationStats;
    }

    public List<Stat> getOpenLocationStats() {
        return openLocationStats;
    }

    public List<Stat> getAuthorizationStats() {
        return authorizationStats;
    }

    public List<Stat> getVoidStats() {
        return voidStats;
    }

    /**
     * Returns SDK Merchant Location object
     *
     * @return Location Object
     * @throws ClientException
     */
    public Location getSdkLocation(long timeout, boolean isRepeating) throws ClientException {
        try {
            Callable<Location> callable = () -> {
                logger.info("Calling out to the PayPal Merchant SDK: GetLocationRequest");
                GetLocationRequest request = GetLocationRequest.newInstance();
                request.setLookUpType(GetLocationRequest.IdLookUpType.LocationId);
                request.setId(locationId);
                GetLocationCommand command = commandBuilder.build(request);
                executeCommand(command, getLocationStat, getLocationStats, isRepeating);
                GetLocationResponse response = command.getResponse();
                return response.getLocation();
            };

            CallableWithTimeout<Location> callableWithTimeout = new CallableWithTimeout<>(callable,
                    timeout,
                    TIMEOUT_UNIT);
            return callableWithTimeout.call();
        } catch (Exception e) {
            logger.error("Unknown exception: ", e);
            throw new ClientException("Failed PayPal Merchant SDK: GetLocationRequest: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the Location to the desired state (Open or Closed)
     *
     * @param isOpen - The desired state (true=Open, false=Closed)
     * @throws ClientException
     */
    public Location setLocationAvailability(boolean isOpen, long timeout, boolean isRepeating) throws ClientException {
        try {
            Callable<Location> callable = () -> {
                logger.info("Calling out to the PayPal Merchant SDK: SetLocationAvailabilityRequest");
                logger.info("Setting isOpen to: " + isOpen);
                SetLocationAvailabilityRequest request = SetLocationAvailabilityRequest.newInstance();
                request.setOpen(isOpen);
                request.setId(locationId);
                SetLocationAvailabilityCommand command = commandBuilder.build(request);
                executeCommand(command, openLocationStat, openLocationStats, isRepeating);
                logger.info("Successfully set location availability to " + (isOpen ? "OPEN" : "CLOSED") + " for " + locationId);
                SetLocationAvailabilityResponse response = command.getResponse();
                return response.getLocation();
            };

            CallableWithTimeout<Location> callableWithTimeout = new CallableWithTimeout<>(callable,
                    timeout,
                    TIMEOUT_UNIT);
            return callableWithTimeout.call();

        } catch (Exception e) {
            logger.error("Failed PayPal Merchant SDK: SetLocationAvailabilityRequest: ", e);
            throw new ClientException("Failed PayPal Merchant SDK: SetLocationAvailabilityRequest: " + e.getMessage(), e);
        }
    }


    public String getAuthorizationTransactionId(String payCode, BigDecimal amount, long timeout, boolean isRepeating) throws ClientException {
        try {
            Callable<String> callable = () -> {
                logger.info("Calling out to the PayPal Merchant SDK: AuthorizeRequest");
                AuthorizeRequest request = AuthorizeRequest.newInstance();

                request.setIdempotentId(generateIdempotentId());
                request.setTransaction(com.paypal.merchant.retail.sdk.contract.entities.Transaction.newInstance());

                request.getTransaction().setAmount(Amount.newInstance(Amount.CurrencyCodeType.USD,
                        amount.setScale(2, RoundingMode.HALF_UP)));

                CustomerCredentialPayCode payCodeCredential = (CustomerCredentialPayCode) CustomerCredential.newInstance(CustomerCredential.Type.PayCode);
                payCodeCredential.setPayCode(payCode);

                request.setCustomerCredential(payCodeCredential);
                request.setTerminal(Terminal.newInstance());
                request.setReceipt(Receipt.newInstance());


                AuthorizeCommand command = commandBuilder.build(request);
                executeCommand(command, authorizationStat, authorizationStats, isRepeating);
                AuthorizeResponse response = command.getResponse();
                return response.getTransactionId();
            };

            CallableWithTimeout<String> callableWithTimeout = new CallableWithTimeout<>(callable,
                    timeout,
                    TIMEOUT_UNIT);
            return callableWithTimeout.call();

        } catch (Exception e) {
            logger.error("Failed PayPal Merchant SDK: AuthorizeRequest: ", e);
            throw new ClientException("Failed PayPal Merchant SDK: AuthorizeRequest: " + e.getMessage(), e);
        }
    }

    public String getVoidAuthorizationId(String authorizationTransactionId, long timeout, boolean isRepeating) throws ClientException {
        try {
            Callable<String> callable = () -> {
                logger.info("Calling out to the PayPal Merchant SDK: VoidRequest");
                VoidRequest request = VoidRequest.newInstance();

                request.setIdempotentId(generateIdempotentId());
                request.setAuthorizationId(authorizationTransactionId);
                request.setNote("Test");


                VoidCommand command = commandBuilder.build(request);
                executeCommand(command, voidStat, voidStats, isRepeating);
                VoidResponse response = command.getResponse();
                return response.getAuthorizationId();
            };

            CallableWithTimeout<String> callableWithTimeout = new CallableWithTimeout<>(callable,
                    timeout,
                    TIMEOUT_UNIT);
            return callableWithTimeout.call();

        } catch (Exception e) {
            logger.error("Failed PayPal Merchant SDK: AuthorizeRequest: ", e);
            throw new ClientException("Failed PayPal Merchant SDK: AuthorizeRequest: " + e.getMessage(), e);
        }
    }



    /**
     * Method that generically executes a command and throws an exception if error info is present
     *
     * @param command The command to execute
     * @throws ClientException
     */
    private void executeCommand(Command command, final Stat oneTimeCommandStat, final List<Stat> repeatingCommandStats, boolean isRepeating) throws ClientException {
        long startTimestamp = System.currentTimeMillis();
        boolean isSuccessful = false;
        long elapsedTime;
        long startTime = System.nanoTime();
        try {
            CommandResult result = command.execute();
            ErrorInfo errorInfo = command.getErrorInfo();
            logger.info(command.getClass().getSimpleName() + " Result: " + result.name());
            if (result != CommandResult.Success && errorInfo != null && errorInfo.getData().size() > 0) {
                logger.error(command.getClass().getSimpleName() + ": Error Code: " + errorInfo.getCode().name());
                logger.error(command.getClass().getSimpleName() + ": Error ID: " + errorInfo.getData().get(0).getErrorId());
                logger.error(command.getClass().getSimpleName() + ": Error Msg: " + errorInfo.getData().get(0).getMessage());
                throw new ClientException(errorInfo.getData().get(0).getMessage());
            }
            isSuccessful = true;
        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
        } finally {
            elapsedTime = System.nanoTime() - startTime;
            logger.debug("adding new Stat to getLocationData");
            oneTimeCommandStat.setStartTime(startTimestamp);
            oneTimeCommandStat.setElapsedTime(elapsedTime);
            oneTimeCommandStat.setSuccessful(isSuccessful);

            if(isRepeating) {
                repeatingCommandStats.add(oneTimeCommandStat);
            }

        }
    }

    private static String generateIdempotentId() {
        return Integer.toString(RANDOM.nextInt(Integer.MAX_VALUE));
    }

}
