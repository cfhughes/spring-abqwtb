package com.chughes.abqwtb.server;

import com.chughes.abqwtb.server.model.BusUpdateData;
import com.chughes.abqwtb.server.model.Vehicle;
import com.chughes.abqwtb.server.service.GtfsDataService;
import com.chughes.abqwtb.server.service.RealtimeDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class ScheduledQuery {

  private static final Logger log = LoggerFactory.getLogger(ScheduledQuery.class);

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

  private HTTPGetter httpGetter;
  private RealtimeDataService realtimeDataService;
  private GtfsDataService gtfsDataService;

  public ScheduledQuery(HTTPGetter httpGetter,
      RealtimeDataService realtimeDataService,
      GtfsDataService gtfsDataService){
    this.httpGetter = httpGetter;
    this.realtimeDataService = realtimeDataService;
    this.gtfsDataService = gtfsDataService;
  }

  @Scheduled(fixedRate = 15 * 1000)
  public void reportCurrentTime() {
    int seconds = LocalTime.now(ZoneId.of("America/Denver")).toSecondOfDay();
    if (!gtfsDataService.isServiceActive(seconds)){
      return;
    }
    List<Vehicle> vehicles = httpGetter.get();
    log.info("The time is now {} and There are {} buses", dateFormat.format(new Date()), vehicles.size());
    for (Vehicle vehicle:vehicles){
      //Trip t = gtfsDataService.getStore().getTripForId(new AgencyAndId("1",vehicle.getTripId().trim()));
      //Stop s = gtfsDataService.getStore().getStopForId(new AgencyAndId("1",vehicle.getNextStopId()));

      Duration d = Duration.between(vehicle.getMsgTime(),vehicle.getNextStopSchedTime());
      //TODO Is this really necessary? Fixes past midnight trips
      if (d.compareTo(Duration.ofHours(20)) > 0){
        d = d.minus(Duration.ofHours(24));
      }
      if (d.compareTo(Duration.ofHours(-20)) < 0){
        d = d.plus(Duration.ofHours(24));
      }
      //log.info("Trip "+vehicle.getTripId() + " Duration "+d);

      BusUpdateData updateDataCurrent = new BusUpdateData(vehicle.getMsgTime(), d.getSeconds(), vehicle.getNextStopId());
      BusUpdateData updateDataPrevious = realtimeDataService.getSecondsLateTemp(vehicle.getTripId());

      if (updateDataPrevious != null) {
        //If next stop id is different, accept time update
        if (!updateDataPrevious.getNextStop().equals(vehicle.getNextStopId())) {
          realtimeDataService.putSecondsLate(vehicle.getTripId(), updateDataCurrent);
          if (d.getSeconds() > 5 * 60){
            log.info("Trip id {} is more than 5 minutes late on route {}",vehicle.getTripId(), vehicle.getRouteShortName());
          }
        }

        //If already later to next stop, use current lateness
        if (d.getSeconds() > updateDataPrevious.getSecondsLate()) {
          realtimeDataService.putSecondsLate(vehicle.getTripId(), updateDataCurrent);
        }
      }

      //Put current update for next time
      realtimeDataService.putSecondsLateTemp(vehicle.getTripId(), updateDataCurrent);

      //System.out.println(vehicle.getTripId() + " " + d);
    }
  }
}
