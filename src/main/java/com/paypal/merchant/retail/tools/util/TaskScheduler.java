package com.paypal.merchant.retail.tools.util;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Paolo on 7/28/2014.
 */
public abstract class TaskScheduler {
    private Logger logger = Logger.getLogger(this.getClass());
    protected ScheduledExecutorService service;
    protected Map<String, Runnable> runnableMap;

    protected TaskScheduler(Map<String, Runnable> runnableMap, int threadPoolSize) {
        logger.info("Initializing TaskScheduler with runnableMap of size: " + runnableMap.size() +
                    " and a thread pool size of: " + threadPoolSize);
        this.service = Executors.newScheduledThreadPool(threadPoolSize);
        this.runnableMap = runnableMap;
    }

    /**
     * This will start the scheduler all the scheduledFutures
     */
    public abstract void start();

    /**
     * This will stop the scheduler all the scheduledFutures
     */
    public abstract void stop();

    /**
     * Returns the delay time for a given runnable
     * @param runnableKey - The key of the desired runnable from runnableMap
     * @return - Long the delay time for the desired runnable
     */
    public abstract long getDelayTime(String runnableKey);
}
