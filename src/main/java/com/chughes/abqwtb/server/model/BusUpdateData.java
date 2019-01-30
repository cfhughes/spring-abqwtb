package com.chughes.abqwtb.server.model;

import java.time.LocalTime;
import java.util.Date;

public class BusUpdateData {

    private LocalTime updateTime;

    private long secondsLate;

    private String nextStop;

    public BusUpdateData(LocalTime updateTime, long secondsLate, String nextStop) {
        this.updateTime = updateTime;
        this.secondsLate = secondsLate;
        this.nextStop = nextStop;
    }

    public LocalTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalTime updateTime) {
        this.updateTime = updateTime;
    }

    public long getSecondsLate() {
        return secondsLate;
    }

    public void setSecondsLate(long secondsLate) {
        this.secondsLate = secondsLate;
    }

    public String getNextStop() {
        return nextStop;
    }

    public void setNextStop(String nextStop) {
        this.nextStop = nextStop;
    }
}
