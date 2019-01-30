package com.chughes.abqwtb.server.model;

public class RealtimeTripInfo implements Comparable<RealtimeTripInfo> {

  private long secondsLate;

  private String tripId;

  private int scheduledTime;

  private String displayTime;

  private String service;

  public long getSecondsLate() {
    return secondsLate;
  }

  public void setSecondsLate(long secondsLate) {
    this.secondsLate = secondsLate;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public int getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(int scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public String getDisplayTime() {
    return displayTime;
  }

  public void setDisplayTime(String displayTime) {
    this.displayTime = displayTime;
  }

  @Override
  public int compareTo(RealtimeTripInfo o) {
    return scheduledTime - o.getScheduledTime();
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }
}
