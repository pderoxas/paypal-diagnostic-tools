package com.paypal.merchant.retail.tools.model;

/**
 * Created by Paolo on 8/1/2014.
 */
public class Stat {
    private long startTime;
    private long elapsedTime;
    private boolean isSuccessful;

    public Stat() {
    }

    public Stat(long startTime, long elapsedTime, boolean isSuccessful) {
        this.startTime = startTime;
        this.elapsedTime = elapsedTime;
        this.isSuccessful = isSuccessful;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }
}
