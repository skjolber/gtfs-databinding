package com.github.skjolber.dc.gtfs.mt.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.GtfsIntermediateProcessor;
import com.github.skjolber.dc.gtfs.mt.TripAdapter;
import com.github.skjolber.dc.gtfs.mt.TripHandler;
import com.github.skjolber.dc.model.Service;
import com.github.skjolber.dc.model.Trip;

/**
 * 
 * For chunked / multi-threaded parsing.
 *
 */

public class FilterTripAdapter extends TripAdapter {

	public FilterTripAdapter(int chunkLength, GtfsFeed feed) {
		super(chunkLength, feed);
	}

	public void resolveServices() {
		for (GtfsIntermediateProcessor<Trip> p : processors.values()) {
			Map<String, List<Trip>> tripByRouteId = p.getById(1);
			for (Entry<String, List<Trip>> entry : tripByRouteId.entrySet()) {
				Service s = feed.getService(entry.getKey());
				if(s != null) {
					for(Trip trip : entry.getValue()) {
						trip.setService(s);
						s.add(trip);
					}
				}
			}
		}
	}
	
	public List<Trip> resolveTrips() {
		ArrayList<Trip> trips = new ArrayList<>(4096);
		for (TripHandler tripHandler : handlers.values()) {
			List<Trip> list = tripHandler.getTrips();
			trips.ensureCapacity(trips.size() + list.size());
			for(Trip t : list) {
				if(t.getService() != null) {
					trips.add(t);
				}
			}
		}
		return trips;
	}

}
