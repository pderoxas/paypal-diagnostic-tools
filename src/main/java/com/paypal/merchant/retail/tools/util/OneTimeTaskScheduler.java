package com.paypal.merchant.retail.tools.util;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paolo
 * Created on 5/14/14 1:22 PM
 */
public class OneTimeTaskScheduler extends TaskScheduler {
    Logger logger = Logger.getLogger(this.getClass());

    private Map<String, ScheduledFuture<?>> scheduledFutureMap;
    private long initialWait;
    private TimeUnit timeUnit;

    public OneTimeTaskScheduler(final Map<String, Runnable> runnableMap, long initialWait, int threadPoolSize, TimeUnit timeUnit) {
        super(runnableMap, threadPoolSize);
        this.initialWait = initialWait;
        this.timeUnit = timeUnit;
    }

    @Override
    public void start() {
        try {
            logger.debug("Setting the schedule of tasks.");
            scheduledFutureMap = new HashMap<>();
            for (String runnableKey : super.runnableMap.keySet()) {
                scheduledFutureMap.put(runnableKey,
                        super.service.schedule(super.runnableMap.get(runnableKey), initialWait, timeUnit));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void stop() {
        if(scheduledFutureMap != null) {
            logger.info("Stop the task scheduler. No more future tasks will be executed.");
            for(ScheduledFuture<?> scheduledFuture : scheduledFutureMap.values()) {
                scheduledFuture.cancel(true);
            }
        }
    }

    @Override
    public long getDelayTime(String runnableKey) {
        return 0;
    }
}
