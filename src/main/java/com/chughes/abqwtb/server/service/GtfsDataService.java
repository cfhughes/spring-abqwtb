package com.chughes.abqwtb.server.service;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Calendar.*;

@Service
public class GtfsDataService {

    public static final int HOURS_AFTER_MIDNIGHT = 3;
    private GtfsDaoImpl store;

    private ArrayList<AgencyAndId> serviceIds = null;

    private Map<AgencyAndId, List<StopTime>> timesByStop;

    private Map<AgencyAndId, StopTime> lastStopByTrip;

    private int latestTime = 0;
    private int earliestTime = Integer.MAX_VALUE;

    @PostConstruct
    public void init() {
        GtfsReader reader = new GtfsReader();
        try {
            reader.setInputLocation(new File("src/main/resources/gtfs.zip"));
            store = new GtfsDaoImpl();
            reader.setEntityStore(store);
            reader.run();
            timesByStop = new HashMap<>();
            for (StopTime stopTime:store.getAllStopTimes()){
                if (!timesByStop.containsKey(stopTime.getStop().getId())){
                    timesByStop.put(stopTime.getStop().getId(),new ArrayList<>());
                }
                timesByStop.get(stopTime.getStop().getId()).add(stopTime);
                if (stopTime.getArrivalTime() < earliestTime){
                    earliestTime = stopTime.getArrivalTime();
                }
                if (stopTime.getArrivalTime() > latestTime){
                    latestTime = stopTime.getArrivalTime();
                }
            }
            System.out.println("Latest: "+latestTime+" Earliest: "+earliestTime);
        } catch (IOException e) {
            throw new RuntimeException("Can't read gtfs file");
        }

        getServiceIds();

    }

    public List<StopTime> timesForStop(AgencyAndId stop){
        return timesByStop.get(stop);
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

    public boolean isLastStop(StopTime stopTime){
        if (lastStopByTrip == null) {
            lastStopByTrip = new HashMap<>();
            for (StopTime stopT : store.getAllStopTimes()) {
                if (!getCurrentServiceIds().contains(stopT.getTrip().getServiceId())){
                    //No service today
                    continue;
                }
                StopTime currentLastStop = lastStopByTrip.get(stopT.getTrip().getId());
                if (currentLastStop == null || currentLastStop.getStopSequence() < stopT.getStopSequence()) {
                    lastStopByTrip.put(stopT.getTrip().getId(), stopT);
                }
            }
        }
        return stopTime.getId().equals(lastStopByTrip.get(stopTime.getTrip().getId()).getId());

    }

    //TODO Make sure this works for all cases
    public boolean isServiceActive(int seconds){
        int latestUpdateTime = latestTime + 60 * 60;
        if (seconds > earliestTime && seconds < latestUpdateTime){
            return true;
        }
        if (latestUpdateTime > 24 * 60 * 60 && seconds < latestUpdateTime % 24 * 60 * 60){
            return true;
        }
        return false;
    }

    private int getDayOfWeek(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        //Before 3am, it's still yesterday
        if (calendar.get(Calendar.HOUR_OF_DAY) < HOURS_AFTER_MIDNIGHT){
            dayOfWeek--;
        }
        return dayOfWeek;
    }

    private ServiceDate getServiceDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR,-HOURS_AFTER_MIDNIGHT);
        return new ServiceDate(calendar);
    }

    //Invalidate serviceIds at 3:05am
    @Scheduled(cron="0 "+HOURS_AFTER_MIDNIGHT+" 5 * * ?")
    private void invalidateServiceIds(){
        serviceIds = null;
        getServiceIds();
    }

    public int getLatestTime() {
        return latestTime;
    }

    public int getEarliestTime() {
        return earliestTime;
    }
}
