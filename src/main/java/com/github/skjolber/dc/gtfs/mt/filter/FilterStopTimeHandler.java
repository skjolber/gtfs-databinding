package com.github.skjolber.dc.gtfs.mt.filter;

import java.util.ArrayList;

import com.github.skjolber.dc.gtfs.mt.StopTimeHandler;
import com.github.skjolber.dc.model.StopTime;
import com.github.skjolber.dc.model.Trip;

/**
 * It is not necessary to synchronized on trips because it is assumed that all the stop-times for a single trip are
 * in the same segment and thus parsed by the same thrad..
 * 
 */

public class FilterStopTimeHandler extends StopTimeHandler {

	protected StopTime previous = null;
	
	@Override
	public void handleLine(StopTime value) {
		Trip trip = value.getTrip();
		if(trip == null) {
			return;
		}
		
		if(previous != null) {
			if(previous.getTrip() == trip) {
				value.setPrevious(previous);
				previous.setNext(value);
			} else {
				trip.setStopTimes(new ArrayList<>());
			}
		} else {
			trip.setStopTimes(new ArrayList<>());
		}
		
		trip.add(value);
	}
	
}
