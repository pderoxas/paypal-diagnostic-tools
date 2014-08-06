package com.paypal.merchant.retail.tools.util;

import org.apache.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    private boolean isStarted = false;

    protected TaskScheduler(Map<String, Runnable> runnableMap, int threadPoolSize) {
        logger.info("Initializing TaskScheduler with runnableMap of size: " + runnableMap.size() +
                " and a thread pool size of: " + threadPoolSize);
        this.service = Executors.newScheduledThreadPool(threadPoolSize);
        this.runnableMap = runnableMap;
        this.propChangeSupport = new PropertyChangeSupport(this);
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * This will start the scheduler all the scheduledFutures
     */
    public void start() {
        propChangeSupport.firePropertyChange("isStarted", this.isStarted, true);
        isStarted = true;

    }

    /**
     * This will stop the scheduler all the scheduledFutures
     */
    public void stop() {
        propChangeSupport.firePropertyChange("isStarted", this.isStarted, false);
        isStarted = false;
    }

    /**
     * Returns the delay time for a given runnable
     *
     * @param runnableKey - The key of the desired runnable from runnableMap
     * @return - Long the delay time for the desired runnable
     */
    public abstract long getDelayTime(String runnableKey);
}
