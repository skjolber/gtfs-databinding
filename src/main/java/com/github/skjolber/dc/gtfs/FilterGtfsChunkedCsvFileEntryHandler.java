package com.github.skjolber.dc.gtfs;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.gtfs.mt.ShapePointAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.mt.StopTimeHandler;
import com.github.skjolber.dc.gtfs.mt.filter.FilterStopTimeAdapter;
import com.github.skjolber.dc.gtfs.mt.filter.FilterTripAdapter;
import com.github.skjolber.dc.gtfs.st.filter.FilterServiceCalendarAdapter;
import com.github.skjolber.dc.gtfs.st.filter.FilterServiceCalendarDateAdapter;
import com.github.skjolber.dc.model.StopTime;
import com.github.skjolber.dc.model.Trip;
import com.github.skjolber.unzip.FileEntryChunkStreamHandler;
import com.github.skjolber.unzip.FileEntryStreamHandler;

/**
 * Filtering on start and end time
 * 
 */

public class FilterGtfsChunkedCsvFileEntryHandler extends AbstractGtfsChunkedCsvFileEntryHandler {

	private static Logger logger = LoggerFactory.getLogger(FilterGtfsChunkedCsvFileEntryHandler.class);
	
	private FilterStopTimeAdapter stopTimeAdapter;
	private FilterServiceCalendarAdapter serviceCalendarAdapter;
	private FilterServiceCalendarDateAdapter serviceCalendarDateAdapter;
	
	protected volatile boolean setStopTimesOnStop = false;

	public FilterGtfsChunkedCsvFileEntryHandler(int chunkLength, Double colocationLimit, LocalDate startDate, LocalDate endDate) {
		super(chunkLength, colocationLimit);
		
		this.tripAdapter = new FilterTripAdapter(chunkLength, feed);
		this.stopTimeAdapter = new FilterStopTimeAdapter(chunkLength, feed);

		this.serviceCalendarAdapter = new FilterServiceCalendarAdapter(feed, startDate, endDate);
		this.serviceCalendarDateAdapter = new FilterServiceCalendarDateAdapter(feed);
	}

	@Override
	public void endFileCollection(String name, ThreadPoolExecutor executor) {
		
		// first parse everything except stop times
		// create services
		// filter out trips that are outside the date range
		
		logger.info("endFileCollection " + name);

		if(endFileCollectionForFeedInfo) {
			while(onPostProcessing());
			
		} else if(feedInfo) {
			endFileCollectionForFeedInfo = true;
		}
	}
	
	@Override
	public void validate() {
		super.validate();
		
		if(!setStopTimesOnStop) {
			throw new RuntimeException();
		}
		
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
	
	protected boolean onPostProcessing() {
		if(!createCalendarServices && serviceCalendar) {
			logger.info("Post process service calendar");

			createCalendarServices = true;
			
			serviceCalendarAdapter.resolveServices();
			
			return true;
		}
		if(!createCalendarDatesServices && serviceCalendarDates) {
			logger.info("Post process service calendar dates");

			createCalendarDatesServices = true;
			
			serviceCalendarDateAdapter.resolveServices();
			
			return true;
		}
		
		if(!setRouteOnTrip && routes && trips) {
			logger.info("Post process trips routes");

			setRouteOnTrip = true;
			
			tripAdapter.resolveRoutes();

			return true;
		}
		
		if(!setServiceOnTrip && trips && createCalendarServices && createCalendarDatesServices) {
			logger.info("Post process trips services");

			setServiceOnTrip = true;
			
			tripAdapter.resolveServices();
			
			feed.addTrips(tripAdapter.resolveTrips());
			
			return true;
		}
		
		if(!setAgencyOnRoutes && agency && routes) {
			logger.info("Post process stop times");
			routeAdapter.resolveReferences(feed);

			setAgencyOnRoutes = true;

			return true;
		}

		if(!setStopTimesOnStop && stopTimes && stops) {
			logger.info("Post process stop times");

			stopTimeAdapter.resolveReferences();
			
			setStopTimesOnStop = true;
			
			return true;
		}
		
		if(!setServiceOnTrip && trips && createCalendarServices) {
			logger.info("Post process trips services");

			List<Trip> resolveTrips = tripAdapter.resolveTrips();
			for (Trip trip : resolveTrips) {
				feed.addTrip(trip);
			}

			tripAdapter.resolveServices();
			
			setServiceOnTrip = true;
			
			return true;
		}
		
		if(!setStopsAndTripsOnTransfers && transfers && stops && trips) {
			logger.info("Post process transfers");

			setStopsAndTripsOnTransfers = true;
			
			transferAdapter.resolveTranfers();
			
			return true;
		}
		
		return false;
	}

	protected void onCalendarEntryProcessed(ThreadPoolExecutor executor) {
		serviceCalendarAdapter.resolveServices();
		
		feed.getCalendars().addAll(serviceCalendarAdapter.getServiceCalendars());
		
		this.serviceCalendar = true;
	}

	protected void onCalendarDatesEntryProcessed(ThreadPoolExecutor executor) {
		serviceCalendarDateAdapter.resolveServices();
		
		// add to store
		feed.getCalendarDates().addAll(serviceCalendarDateAdapter.resolveServices());
		
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
			case "shapes.txt": return shapePointAdapter.getFileEntryStreamHandler(this, executor, size);
		}

		return super.getFileEntryStreamHandler(name, size, executor);
	}

	@Override
	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler(String name, long size, ThreadPoolExecutor executor) throws Exception {
		switch(name) {
			case "trips.txt": return tripAdapter.getFileEntryChunkedStreamHandler();
			case "stop_times.txt": return stopTimeAdapter.getFileEntryChunkedStreamHandler();
			case "shapes.txt": return shapePointAdapter.getFileEntryChunkedStreamHandler();
		}
		return super.getFileEntryChunkedStreamHandler(name, size, executor);
	}

}
