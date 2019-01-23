package com.chughes.abqwtb.server;

import com.chughes.abqwtb.server.model.Vehicle;
import com.chughes.abqwtb.server.service.GtfsDataService;
import com.chughes.abqwtb.server.service.RealtimeDataService;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

  @Scheduled(fixedRate = 2 * 60 * 1000)
  public void reportCurrentTime() {
    List<Vehicle> vehicles = httpGetter.get();
    log.info("The time is now {} \n There are {} buses", dateFormat.format(new Date()), vehicles.size());
    for (Vehicle vehicle:vehicles){
      //Trip t = gtfsDataService.getStore().getTripForId(new AgencyAndId("1",vehicle.getTripId().trim()));
      //Stop s = gtfsDataService.getStore().getStopForId(new AgencyAndId("1",vehicle.getNextStopId()));

      Duration d = Duration.between(vehicle.getMsgTime(),vehicle.getNextStopSchedTime());

      realtimeDataService.putSecondsLate(Integer.parseInt(vehicle.getTripId()),
          (int) d.getSeconds());
      //System.out.println(vehicle.getTripId() + " " + d);
    }
  }
}
