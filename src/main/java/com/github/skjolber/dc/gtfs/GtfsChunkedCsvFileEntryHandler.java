package com.github.skjolber.dc.gtfs;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.mt.StopTimeAdapter;
import com.github.skjolber.dc.gtfs.mt.StopTimeHandler;
import com.github.skjolber.dc.gtfs.mt.TripAdapter;
import com.github.skjolber.dc.gtfs.st.ServiceCalendarAdapter;
import com.github.skjolber.dc.gtfs.st.ServiceCalendarDateAdapter;
import com.github.skjolber.dc.model.FeedInfo;
import com.github.skjolber.dc.model.StopTime;
import com.github.skjolber.dc.model.Trip;
import com.github.skjolber.unzip.FileEntryChunkStreamHandler;
import com.github.skjolber.unzip.FileEntryStreamHandler;

public class GtfsChunkedCsvFileEntryHandler extends AbstractGtfsChunkedCsvFileEntryHandler {

	private static Logger logger = LoggerFactory.getLogger(GtfsChunkedCsvFileEntryHandler.class);

	private StopTimeAdapter stopTimeAdapter;
	private ServiceCalendarAdapter serviceCalendarAdapter;
	private ServiceCalendarDateAdapter serviceCalendarDateAdapter;
	
	protected volatile boolean setTripAndStopOnStopTime = false;

	public GtfsChunkedCsvFileEntryHandler(int chunkLength, Double colocationLimit) {
		super(chunkLength, colocationLimit);
		
		this.tripAdapter = new TripAdapter(chunkLength, feed);
		this.stopTimeAdapter = new StopTimeAdapter(chunkLength, feed);
		
		this.serviceCalendarAdapter = new ServiceCalendarAdapter(feed);
		this.serviceCalendarDateAdapter = new ServiceCalendarDateAdapter(feed);
	}

	@Override
	public void endFileCollection(String name, ThreadPoolExecutor executor) {
		logger.info("endFileCollection " + name);

		if(feedInfo) {
			while(onPostProcessing());
		}
	}

	protected void onFeedInfoEntryProcessed(ThreadPoolExecutor executor) {
		// add to store and lookup cache
		for (Entry<String, FeedInfo> entry : feedInfoAdapter.getFeedInfoById().entrySet()) {
			setDefaultAgencyId(entry.getKey());
		}
		
		this.feedInfo = true;
	}

	protected void onStopTimesEntryProcessed(ThreadPoolExecutor executor) {
		// add to store
		
		for (StopTimeHandler stopTimeHandler : stopTimeAdapter.getHandlers()) {
			for (List<StopTime> list : stopTimeHandler.getStopTimes()) {
				feed.addStopTimes(list);
			}
		}
		
		this.stopTimes = true;
	}

	protected void onTripsEntryProcessed(ThreadPoolExecutor executor) {
		// add to store and lookup cache
		List<Trip> resolveTrips = tripAdapter.resolveTrips();
		for (Trip trip : resolveTrips) {
			feed.addTrip(trip);
		}

		this.trips = true;
	}

	protected boolean onPostProcessing() {
		if(!createCalendarServices && serviceCalendar) {
			createCalendarServices = true;
			
			serviceCalendarAdapter.resolveServices();
			
			return true;
		}
		if(!createCalendarDatesServices && serviceCalendarDates) {
			createCalendarDatesServices = true;
			
			serviceCalendarDateAdapter.resolveServices();
			
			return true;
		}
		
		if(!setAgencyOnRoutes && agency && routes) {
			routeAdapter.resolveReferences(feed);

			setAgencyOnRoutes = true;

			return true;
		}

		if(!setRouteOnTrip && routes && trips && setServiceOnTrip) {
			
			tripAdapter.resolveRoutes();

			setRouteOnTrip = true;

			return true;
		}
		
		if(!setTripAndStopOnStopTime && setRouteOnTrip && stopTimes && trips && stops) {
			setTripAndStopOnStopTime = true;
			
			stopTimeAdapter.resolveReferences();
			
			return true;
		}
		
		if(!setServiceOnTrip && trips && createCalendarServices && createCalendarDatesServices) {
			setServiceOnTrip = true;
			
			tripAdapter.resolveServices();
			
			return true;
		}
		
		if(!setStopsOnTransfers && transfers && stops) {
			setStopsOnTransfers = true;
			
			transferAdapter.resolveStops();
			
			return true;
		}

		return false;
	}

	public String getAgencyId() {
		return feed.getAgencyId();
	}

	protected void onCalendarEntryProcessed(ThreadPoolExecutor executor) {
		feed.getCalendars().addAll(serviceCalendarAdapter.getServiceCalendars());
		
		this.serviceCalendar = true;
	}

	protected void onCalendarDatesEntryProcessed(ThreadPoolExecutor executor) {
		// add to store
		feed.getCalendarDates().addAll(serviceCalendarDateAdapter.getServiceCalendarDates());
		
		this.serviceCalendarDates = true;
	}
	
	public GtfsFeed getHandlerCache() {
		return feed;
	}

	@Override
	public FileEntryStreamHandler getFileEntryStreamHandler(String name, long size, ThreadPoolExecutor executor) throws Exception {
		switch(name) {
			case "trips.txt": return tripAdapter.getFileEntryStreamHandler(this, executor, size);
			case "stop_times.txt": return stopTimeAdapter.getFileEntryStreamHandler(this, executor, size);
			case "calendar.txt": return serviceCalendarAdapter.getFileEntryStreamHandler(this, executor, size);	
			case "calendar_dates.txt": return serviceCalendarDateAdapter.getFileEntryStreamHandler(this, executor, size);
		}

		return super.getFileEntryStreamHandler(name, size, executor);
	}

	@Override
	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler(String name, long size, ThreadPoolExecutor executor) throws Exception {
		switch(name) {
			case "trips.txt": return tripAdapter.getFileEntryChunkedStreamHandler();
			case "stop_times.txt": return stopTimeAdapter.getFileEntryChunkedStreamHandler();
		}
		return super.getFileEntryChunkedStreamHandler(name, size, executor);
	}
}
