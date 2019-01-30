package com.chughes.abqwtb.server.service;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import com.chughes.abqwtb.server.model.BusUpdateData;
import org.springframework.stereotype.Component;

@Component
public class RealtimeDataService {

  private Map<String, BusUpdateData> secondsLateTempDataStore;
  private Map<String, BusUpdateData> secondsLateDataStore;

  @PostConstruct
  public void init(){
    secondsLateDataStore = new HashMap<>();
    secondsLateTempDataStore = new HashMap<>();
  }

  public void putSecondsLate(String tripId, BusUpdateData update){
    secondsLateDataStore.put(tripId, update);
  }

  public BusUpdateData getSecondsLate(String tripId){
    return secondsLateDataStore.get(tripId);
  }

  public void putSecondsLateTemp(String tripId, BusUpdateData update){
    secondsLateTempDataStore.put(tripId, update);
  }

  public BusUpdateData getSecondsLateTemp(String tripId){
    return secondsLateTempDataStore.get(tripId);
  }

  public Map<String, BusUpdateData> getSecondsLateDataStore() {
    return secondsLateDataStore;
  }
}
