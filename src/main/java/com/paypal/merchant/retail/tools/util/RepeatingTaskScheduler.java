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
public class RepeatingTaskScheduler extends TaskScheduler {
    Logger logger = Logger.getLogger(this.getClass());

    private Map<String, ScheduledFuture<?>> scheduledFutureMap;
    private long initialWait;
    private long interval;
    private TimeUnit timeUnit;
    private long startTime;
    private long duration;

    public RepeatingTaskScheduler(final Map<String, Runnable> runnableMap, long initialWait, long interval, int threadPoolSize, TimeUnit timeUnit, long duration) {
        super(runnableMap, duration > 0 ? threadPoolSize + 1 : threadPoolSize);
        this.initialWait = initialWait;
        this.interval = interval;
        this.timeUnit = timeUnit;
        this.duration = duration;
        if(duration > 0) {
            runnableMap.put("DurationTimer", durationTimer);
        }
        this.startTime = System.currentTimeMillis() + initialWait;
    }

    @Override
    public void start() {
        try{
            logger.debug("Setting the schedule of tasks.");
            scheduledFutureMap = new HashMap<>();
            for(String runnableKey : super.runnableMap.keySet()) {
                scheduledFutureMap.put(runnableKey,
                        super.service.scheduleAtFixedRate(super.runnableMap.get(runnableKey), initialWait, interval, timeUnit));
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
                scheduledFuture.cancel(false);
            }
        }
    }

    @Override
    public long getDelayTime(String runnableKey) {
        if(scheduledFutureMap != null) {
            return scheduledFutureMap.get(runnableKey).getDelay(timeUnit);
        }
        return 0;
    }

    /**
     * This set the location availability via the SDK
     */
    public final Runnable durationTimer = () -> {
        try {
            if(System.currentTimeMillis() - startTime > duration) {
                stop();
            }
        } catch (Exception e) {
            logger.error("Failed to stop the scheduler after duration period has elapsed.", e);
        }
    };
}
