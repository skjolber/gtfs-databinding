package com.github.skjolber.dc.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Service {

	private String id;
	
	private List<ServiceCalendar> calendars = new ArrayList<>();
	
	private List<ServiceCalendarDate> dates = new ArrayList<>();

	private List<Trip> trips = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ServiceCalendar> getCalendars() {
		return calendars;
	}

	public void setCalendars(List<ServiceCalendar> calendars) {
		this.calendars = calendars;
	}

	public List<ServiceCalendarDate> getDates() {
		return dates;
	}

	public void setDates(List<ServiceCalendarDate> dates) {
		this.dates = dates;
	}

	public boolean add(ServiceCalendar e) {
		return calendars.add(e);
	}

	public boolean add(ServiceCalendarDate e) {
		return dates.add(e);
	}

	public boolean isActive(LocalDate from, LocalDate to) {
		boolean add = false;
		for(ServiceCalendar serviceCalendar : calendars) {
			if(serviceCalendar.isActiveRange(from, to)) {
				add = true;
				
				break;
			}
		}
		return add;
	}

	public boolean isActive(LocalDate date) {
		boolean add = false;
		for(ServiceCalendar serviceCalendar : calendars) {
			if(serviceCalendar.isActive(date)) {
				add = true;
				
				break;
			}
		}
		
		boolean remove = false;
		for (ServiceCalendarDate serviceCalendarDate : dates) {
			if(serviceCalendarDate.isActive(date)) {
				switch(serviceCalendarDate.getExceptionType()) {
					case ServiceCalendarDate.EXCEPTION_TYPE_ADD: {
						add = true;
						break;
					}
					case ServiceCalendarDate.EXCEPTION_TYPE_REMOVE: {
						remove = true;
						break;
					}
				}
			}
		}
		
		return add && !remove;
	}

	public void add(Trip s) {
		this.trips.add(s);
	}
}

