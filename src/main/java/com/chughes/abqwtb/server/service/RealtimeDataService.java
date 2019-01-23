package com.chughes.abqwtb.server.service;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RealtimeDataService {

  private Map<Integer, Integer> secondsLateDataStore;

  @PostConstruct
  public void init(){
    secondsLateDataStore = new HashMap<>();
  }

  public void putSecondsLate(int tripId, int seconds){
    secondsLateDataStore.put(tripId, seconds);
  }

  public int getSecondsLate(int tripId){
    return secondsLateDataStore.get(tripId);
  }

  public Map<Integer, Integer> getSecondsLateDataStore() {
    return secondsLateDataStore;
  }
}
