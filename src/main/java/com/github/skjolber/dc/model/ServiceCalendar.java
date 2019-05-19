package com.github.skjolber.dc.model;

import java.time.LocalDate;

public class ServiceCalendar {

    private Service service;

    private int monday;

    private int tuesday;

    private int wednesday;

    private int thursday;

    private int friday;

    private int saturday;

    private int sunday;

    private LocalDate startDate;

    private LocalDate endDate;

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public int getMonday() {
        return monday;
    }

    public void setMonday(int monday) {
        this.monday = monday;
    }

    public int getTuesday() {
        return tuesday;
    }

    public void setTuesday(int tuesday) {
        this.tuesday = tuesday;
    }

    public int getWednesday() {
        return wednesday;
    }

    public void setWednesday(int wednesday) {
        this.wednesday = wednesday;
    }

    public int getThursday() {
        return thursday;
    }

    public void setThursday(int thursday) {
        this.thursday = thursday;
    }

    public int getFriday() {
        return friday;
    }

    public void setFriday(int friday) {
        this.friday = friday;
    }

    public int getSaturday() {
        return saturday;
    }

    public void setSaturday(int saturday) {
        this.saturday = saturday;
    }

    public int getSunday() {
        return sunday;
    }

    public void setSunday(int sunday) {
        this.sunday = sunday;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public boolean isActiveRange(LocalDate from, LocalDate to) {
    	return !(to.isBefore(startDate) || from.isAfter(endDate));
    }

    public boolean isActiveRange(LocalDate localDate) {
    	if(localDate.isBefore(startDate) || localDate.isAfter(endDate)) {
    		return false;
    	}
    	return true;
    }

    public boolean isActive(LocalDate localDate) {
    	if(localDate.isBefore(startDate) || localDate.isAfter(endDate)) {
    		return false;
    	}
    	
    	switch(localDate.getDayOfWeek().getValue()) {
	    	case 1: return monday != 0;
	    	case 2: return tuesday != 0;
	    	case 3: return wednesday != 0;
	    	case 4: return thursday != 0;
	    	case 5: return friday != 0;
	    	case 6: return saturday != 0;
	    	case 7: return sunday != 0;
    	}
    	
    	return false;
    }

}
