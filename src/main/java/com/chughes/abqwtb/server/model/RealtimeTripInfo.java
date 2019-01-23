package com.chughes.abqwtb.server.model;

public class RealtimeTripInfo implements Comparable<RealtimeTripInfo> {

  private int secondsLate;

  private int tripId;

  private int scheduledTime;

  private String displayTime;

  private String service;

  public int getSecondsLate() {
    return secondsLate;
  }

  public void setSecondsLate(int secondsLate) {
    this.secondsLate = secondsLate;
  }

  public int getTripId() {
    return tripId;
  }

  public void setTripId(int tripId) {
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
