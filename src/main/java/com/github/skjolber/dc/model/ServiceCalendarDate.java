package com.github.skjolber.dc.model;

import java.time.LocalDate;


public class ServiceCalendarDate {

    public static final int EXCEPTION_TYPE_ADD = 1;

    public static final int EXCEPTION_TYPE_REMOVE = 2;

    private Service service;

    private LocalDate date;

    private int exceptionType;

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(int exceptionType) {
        this.exceptionType = exceptionType;
    }

	public boolean isActive(LocalDate date) {
		return this.date.isEqual(date);
	}

}
