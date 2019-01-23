package com.chughes.abqwtb.server.service;

import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.THURSDAY;
import static java.util.Calendar.TUESDAY;
import static java.util.Calendar.WEDNESDAY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GtfsDataService {

  private GtfsDaoImpl store;

  private ArrayList<AgencyAndId> serviceIds = null;

  private Map<AgencyAndId, List<StopTime>> timesByStop;

  @PostConstruct
  public void init() {
    GtfsReader reader = new GtfsReader();
    try {
      reader.setInputLocation(new File("src/main/resources/gtfs.zip"));
      store = new GtfsDaoImpl();
      reader.setEntityStore(store);
      reader.run();
    } catch (IOException e) {
      throw new RuntimeException("Can't read gtfs file");
    }

  }

  public GtfsDaoImpl getStore() {
    return store;
  }

  public ArrayList<AgencyAndId> getServiceIds(){
    if (serviceIds == null){
      serviceIds = getCurrentServiceIds();
    }
    return serviceIds;
  }

  private ArrayList<AgencyAndId> getCurrentServiceIds(){
    ArrayList<AgencyAndId> service = new ArrayList<>();
    ServiceDate serviceDate = getServiceDate();
    for (ServiceCalendar calendar:store.getAllCalendars()){
      if (calendar.getStartDate().compareTo(serviceDate) > 0){
        continue;
      }
      if (calendar.getEndDate().compareTo(serviceDate) < 0){
        continue;
      }
      switch (getDayOfWeek()){
        case SUNDAY:
          if (calendar.getSunday() == 0) continue;
          break;
        case MONDAY:
          if (calendar.getMonday() == 0) continue;
          break;
        case TUESDAY:
          if (calendar.getTuesday() == 0) continue;
          break;
        case WEDNESDAY:
          if (calendar.getWednesday() == 0) continue;
          break;
        case THURSDAY:
          if (calendar.getThursday() == 0) continue;
          break;
        case FRIDAY:
          if (calendar.getFriday() == 0) continue;
          break;
        case SATURDAY:
          if (calendar.getSaturday() == 0) continue;
          break;
      }
      service.add(calendar.getServiceId());
    }
    for (ServiceCalendarDate serviceCalendarDate:store.getAllCalendarDates()){
      if (serviceCalendarDate.getDate().equals(serviceDate)){
        if (serviceCalendarDate.getExceptionType() == 1) {
          service.add(serviceCalendarDate.getServiceId());
        }else if (serviceCalendarDate.getExceptionType() == 2){
          service.remove(serviceCalendarDate.getServiceId());
        }
      }
    }
    return service;
  }

  private int getDayOfWeek(){
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    //Before 3am, it's still yesterday
    if (calendar.get(Calendar.HOUR_OF_DAY) < 3){
      dayOfWeek--;
    }
    return dayOfWeek;
  }

  private ServiceDate getServiceDate(){
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.HOUR,-3);
    return new ServiceDate(calendar);
  }

  //Invalidate serviceIds at 3:05am
  @Scheduled(cron="0 3 5 * * ?")
  private void invalidateServiceIds(){
    serviceIds = null;
  }
}
