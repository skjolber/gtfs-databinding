package com.github.skjolber.dc.gtfs.mt;

import java.util.ArrayList;
import java.util.List;

import com.github.skjolber.dc.model.Trip;
import com.github.skjolber.unzip.csv.CsvLineHandler;

public class TripHandler implements CsvLineHandler<Trip> {

	private List<Trip> trips = new ArrayList<>(1024);
	
	@Override
	public void handleLine(Trip trip) {
		trips.add(trip);
	}
	
	public List<Trip> getTrips() {
		return trips;
	}
}
