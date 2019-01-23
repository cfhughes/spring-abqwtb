package com.chughes.abqwtb.server;

import com.chughes.abqwtb.server.model.RealtimeTripInfo;
import com.chughes.abqwtb.server.model.VehicleOnTrip;
import com.chughes.abqwtb.server.service.GtfsDataService;
import com.chughes.abqwtb.server.service.RealtimeDataService;
import java.time.Duration;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

  private GtfsDataService gtfsDataService;
  private RealtimeDataService realtimeDataService;

  public MainController(GtfsDataService gtfsDataService,
      RealtimeDataService realtimeDataService) {
    this.gtfsDataService = gtfsDataService;
    this.realtimeDataService = realtimeDataService;
  }

  @GetMapping("/trips")
  public List<VehicleOnTrip> getTrips(){
    List<VehicleOnTrip> trips = new ArrayList<>();
    VehicleOnTrip trip = new VehicleOnTrip();
    trip.setVehicleId(1234);
    trips.add(trip);
    return trips;
  }

  @GetMapping("/stops")
  public List<RealtimeTripInfo> getTrips(@RequestParam int stopId){
    ArrayList<AgencyAndId> currentServiceIds = gtfsDataService
        .getServiceIds();
    GtfsDaoImpl store = gtfsDataService.getStore();
    Stop stop = store.getStopForId(new AgencyAndId("1", Integer.toString(stopId)));
    List<RealtimeTripInfo> stopTimes = new ArrayList<>();
    for (StopTime stopTime:store.getAllStopTimes()){
      if (stopTime.getStop().getId().equals(stop.getId())) {
        int tripId = Integer.parseInt(stopTime.getTrip().getId().getId());
        Trip trip = store.getTripForId(new AgencyAndId("1",tripId+""));
        if (!currentServiceIds.contains(trip.getServiceId())){
          continue;
        }
        //System.out.println(tripId);
        int secondsLate = -1;
        if (realtimeDataService.getSecondsLateDataStore().containsKey(tripId)) {
          secondsLate = realtimeDataService.getSecondsLateDataStore().get(tripId);
        }
        RealtimeTripInfo realtimeTripInfo = new RealtimeTripInfo();
        realtimeTripInfo.setSecondsLate(secondsLate);
        realtimeTripInfo.setScheduledTime(stopTime.getArrivalTime());
        realtimeTripInfo.setTripId(tripId);
        realtimeTripInfo.setService(trip.getServiceId().getId());
        LocalTime arrivalTime = LocalTime.of(
            stopTime.getArrivalTime() / (60 * 60) % 24,
            stopTime.getArrivalTime() / 60 % 60,
            stopTime.getArrivalTime() % 60);
        realtimeTripInfo.setDisplayTime(arrivalTime.toString());
        Duration duration = Duration.between(LocalTime.now(), arrivalTime);
        if (duration.getSeconds() < 3600 && duration.getSeconds() > -1800){
          stopTimes.add(realtimeTripInfo);
        }
      }
    }
    Collections.sort(stopTimes);
    return stopTimes;
  }

}
