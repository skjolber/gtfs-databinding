package com.github.skjolber.dc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.skjolber.dc.model.Agency;
import com.github.skjolber.dc.model.Route;
import com.github.skjolber.dc.model.Service;
import com.github.skjolber.dc.model.ServiceCalendar;
import com.github.skjolber.dc.model.ServiceCalendarDate;
import com.github.skjolber.dc.model.Stop;
import com.github.skjolber.dc.model.StopTime;
import com.github.skjolber.dc.model.Transfer;
import com.github.skjolber.dc.model.Trip;


/**
 * GTFS feed content.
 *
 */

public class GtfsFeed {

    private static final Pattern PATTERN = Pattern.compile("^(\\d{4})(\\d{2})(\\d{2})$");

	private String agencyId;
	
	protected Map<String, Agency> agencyById = new HashMap<>(4096);
	protected Map<String, Route> routeById = new HashMap<>(4096);
	protected Map<String, Trip> tripById = new HashMap<>(4096);
	protected Map<String, Stop> stopById = new HashMap<>(4096);
	protected Map<String, Service> serviceById = new HashMap<>(4096);
	
	protected List<StopTime> stopTimes = new ArrayList<>(4096);
	protected List<Stop> stops = new ArrayList<>(4096);
	protected List<Trip> trips = new ArrayList<>(4096);
	protected List<Agency> agencies = new ArrayList<>(4096);
	protected List<Route> routes = new ArrayList<>(4096);
	protected List<Transfer> transfers = new ArrayList<>(4096);
	
	protected Map<String, LocalDate> dates = new ConcurrentHashMap<>(4096);
	
	protected List<ServiceCalendar> calendars = new ArrayList<>(4096);
	protected List<ServiceCalendarDate> calendarDates = new ArrayList<>(4096);
	
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
	
	public String getAgencyId() {
		return agencyId;
	}
	
	public Map<String, Route> getRouteById() {
		return routeById;
	}
	
	public void addTripById(Map<String, Trip> trips) {
		tripById.putAll(trips);
	}
	
	public void addAgenciesById(Map<String, Agency> trips) {
		agencyById.putAll(trips);
	}
	
	public Map<String, Trip> getTripById() {
		return tripById;
	}

	public void addStopById(Map<String, Stop> stops) {
		stopById.putAll(stops);
	}
	
	public Map<String, Stop> getStopById() {
		return stopById;
	}
	
	public void putRouteById(String id, Route route) {
		this.routeById.put(id, route);
	}

	public void putTripById(String id, Trip trip) {
		this.tripById.put(id, trip);
	}

	public void putServiceById(String id, Service trip) {
		this.serviceById.put(id, trip);
	}

	public void putStopById(String id, Stop stop) {
		this.stopById.put(id, stop);
	}
	
	public Stop getStop(String id) {
		return stopById.get(id);
	}

	public Route getRoute(String id) {
		return routeById.get(id);
	}

	public Trip getTrip(String id) {
		return tripById.get(id);
	}

	public void setStopById(Map<String, Stop> stopById) {
		this.stopById = stopById;
	}
	
	public Agency getAgency(String id) {
		return agencyById.get(id);
	}

	public void setAgencyById(Map<String, Agency> agencyById) {
		this.agencyById = agencyById;
	}
	
	public void setTripById(Map<String, Trip> tripById) {
		this.tripById = tripById;
	}
	
	public List<StopTime> getStopTimes() {
		return stopTimes;
	}
	
	public List<Stop> getStops() {
		return stops;
	}
	
	public List<Trip> getTrips() {
		return trips;
	}
	
	public List<Agency> getAgencies() {
		return agencies;
	}
	
	public List<Route> getRoutes() {
		return routes;
	}
	
	public LocalDate getDate(String value, boolean create) {
		LocalDate localDate = dates.get(value);
		if(localDate == null) {
			if(create) {
				localDate = parseString(value);
				dates.put(value, localDate);
			}
		}
		return localDate;
	}
	
    /**
     * Parse a service date from a string in "YYYYMMDD" format.
     *
     * @param value a string of the form "YYYYMMDD"
     * @return a new {@link LocalDate} object
     */
    public static LocalDate parseString(String value) {

        Matcher matcher = PATTERN.matcher(value);

        if (!matcher.matches())
            throw new IllegalArgumentException("Unexpected date '" + value + "'");

        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int day = Integer.parseInt(matcher.group(3));
        return LocalDate.of(year, month, day);
    }
    
    public List<ServiceCalendar> getCalendars() {
		return calendars;
	}
    
    public List<ServiceCalendarDate> getCalendarDates() {
		return calendarDates;
	}

	public void add(String key, Stop stop) {
		stopById.put(key, stop);
	}

	public void addStopTimes(List<StopTime> stopTimes) {
		this.stopTimes.addAll(stopTimes);
	}
	
	public void addTrip(Trip trip) {
		this.tripById.put(trip.getId(), trip);
	}

	public void addTrips(List<Trip> trips) {
		for(Trip trip : trips) {
			this.tripById.put(trip.getId(), trip);
		}
	}
	
	public void addStops(List<Stop> stops) {
		for(Stop stop: stops) {
			this.stopById.put(stop.getId(), stop);
		}
	}

	public void add(List<Agency> agencies) {
		for(Agency agency: agencies) {
			this.agencyById.put(agency.getId(), agency);
		}
	}
	
	public void addRoutes(List<Route> routes) {
		for(Route route : routes) {
			this.routeById.put(route.getId(), route);
		}
	}

	public Service getService(String key) {
		return serviceById.get(key);
	}

	public Map<String, Service> getServices() {
		return serviceById;
	}
	
	public List<Transfer> getTransfers() {
		return transfers;
	}
}
