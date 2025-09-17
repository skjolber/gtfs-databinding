package com.github.skjolber.dc.gtfs;

import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import com.github.skjolber.dc.gtfs.mt.ShapePointAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.mt.TripAdapter;
import com.github.skjolber.dc.gtfs.st.AgencyAdapter;
import com.github.skjolber.dc.gtfs.st.FeedInfoAdapter;
import com.github.skjolber.dc.gtfs.st.RouteAdapter;
import com.github.skjolber.dc.gtfs.st.StopAdapter;
import com.github.skjolber.dc.gtfs.st.TransferAdapter;
import com.github.skjolber.dc.model.FeedInfo;
import com.github.skjolber.unzip.ChunkedFileEntryHandler;
import com.github.skjolber.unzip.FileEntryStreamHandler;

public abstract class AbstractGtfsChunkedCsvFileEntryHandler implements ChunkedFileEntryHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractGtfsChunkedCsvFileEntryHandler.class);
	
	protected static final int PARALLEL_BUFFER_SIZE = 16 * 1024 * 1024;
	protected static final int PARALLEL_READ_BUFFER_SIZE = 16 * 1024;
	
	public static int getParallelReadBufferSize() {
		return PARALLEL_READ_BUFFER_SIZE;
	}

	public static int getParallelBufferSize() {
		return PARALLEL_BUFFER_SIZE;
	}

	protected ReentrantLock lock = new ReentrantLock();

	protected GtfsFeed feed = new GtfsFeed();

	protected volatile boolean agency = false;
	protected volatile boolean routes = false;
	protected volatile boolean trips = false;
	protected volatile boolean stops = false;
	protected volatile boolean stopTimes = false;
	protected volatile boolean feedInfo = false;
	protected volatile boolean serviceCalendarDates = false;
	protected volatile boolean serviceCalendar = false;
	protected volatile boolean transfers = false;
	protected volatile boolean shapePoints = false;

	protected volatile boolean setAgencyOnRoutes = false;
	protected volatile boolean setRouteOnTrip = false;
	protected volatile boolean setServiceOnTrip = false;
	protected volatile boolean createCalendarServices = false;
	protected volatile boolean createCalendarDatesServices = false;
	protected volatile boolean setStopsAndTripsOnTransfers = false;

	protected volatile boolean endFileCollectionForFeedInfo = false;
	
	protected StopAdapter stopAdapter;
	protected FeedInfoAdapter feedInfoAdapter = new FeedInfoAdapter();
	protected RouteAdapter routeAdapter = new RouteAdapter(feed);
	protected AgencyAdapter agencyAdapter = new AgencyAdapter();
	protected TripAdapter tripAdapter;
	protected TransferAdapter transferAdapter = new TransferAdapter(feed);
	protected Double stopColocationLimit;
	protected ShapePointAdapter shapePointAdapter;

	public AbstractGtfsChunkedCsvFileEntryHandler(int chunkLength, Double colocationLimit) {
		this.stopAdapter = new StopAdapter(feed);
		this.stopColocationLimit = colocationLimit;

		this.shapePointAdapter = new ShapePointAdapter(chunkLength, feed);
	}

	public void setDefaultAgencyId(String defaultAgencyId) {
		feed.setAgencyId(defaultAgencyId);
	}
	
	@Override
	public void beginFileEntry(String name) {
		logger.info("beginFileEntry " + name);
	}

	@Override
	public void beginFileCollection(String name) {
		logger.info("beginFileCollection " + name);
	}

	public void validate() {
		if(!setAgencyOnRoutes) {
			throw new RuntimeException();
		}

		if(!setRouteOnTrip) {
			throw new RuntimeException();
		}
		
		if(!serviceCalendarDates) {
			throw new RuntimeException();
		}
		
		if(!serviceCalendar) {
			throw new RuntimeException();
		}
		
		if(!setServiceOnTrip) {
			throw new RuntimeException();
		}
		
	}

	public void endFileEntry(String name, ThreadPoolExecutor executor) {
		logger.info("End file entry processing " + name);

		switch(name) {
			case "agency.txt" : onAgencyEntryProcessed(executor); break;
			case "routes.txt": onRoutesEntryProcessed(executor); break;
			case "trips.txt": onTripsEntryProcessed(executor); break;
			case "stops.txt": onStopsEntryProcessed(executor); break;
			case "stop_times.txt": onStopTimesEntryProcessed(executor); break;
			case "feed_info.txt": onFeedInfoEntryProcessed(executor); break;
			case "calendar_dates.txt": onCalendarDatesEntryProcessed(executor); break;
			case "calendar.txt": onCalendarEntryProcessed(executor); break;			
			case "transfers.txt": onTransfersEntryProcessed(executor); break;
			case "shapes.txt": onShapePointEntryProcessed(executor); break;
		}
		
		// compete for lock to forward state
        if(lock.tryLock()) {
        	try {
        		while(onPostProcessing());
            } finally {
                lock.unlock();
            }
        } else {
			logger.info("Skipping post processing since it is already being performed by another thread");
		}
	}

	private void onTransfersEntryProcessed(ThreadPoolExecutor executor) {
		this.transfers = true;
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
		this.stopTimes = true;
	}

	protected void onStopsEntryProcessed(ThreadPoolExecutor executor) {
		// add to store and lookup cache
		feed.addStops(stopAdapter.getStops());

		stopAdapter.resolveParents();

		if(stopColocationLimit != null) {
			stopAdapter.resolveColocations(stopColocationLimit, stopColocationLimit);
			feed.addStops(stopAdapter.getParentStops());
		}
		
		this.stops = true;
	}

	protected void onTripsEntryProcessed(ThreadPoolExecutor executor) {
		// add to store and lookup cache
		this.trips = true;
	}

	protected void onShapePointEntryProcessed(ThreadPoolExecutor executor) {
		// add to store and lookup cache
		this.shapePoints = true;
	}

	protected void onRoutesEntryProcessed(ThreadPoolExecutor executor) {
		// add to store and lookup cache
		feed.addRoutes(routeAdapter.getRoutes());

		this.routes = true;
	}

	protected void onAgencyEntryProcessed(ThreadPoolExecutor executor) {
		feed.add(agencyAdapter.getAgencies());
		
		this.agency = true;
	}

	protected abstract boolean onPostProcessing();
	
	public String getAgencyId() {
		return feed.getAgencyId();
	}

	protected void onCalendarEntryProcessed(ThreadPoolExecutor executor) {
		this.serviceCalendar = true;
	}

	protected void onCalendarDatesEntryProcessed(ThreadPoolExecutor executor) {
		this.serviceCalendarDates = true;
	}
	
	public GtfsFeed getHandlerCache() {
		return feed;
	}

	@Override
	public FileEntryStreamHandler getFileEntryStreamHandler(String name, long size, ThreadPoolExecutor executor) throws Exception {
		switch(name) {
			case "agency.txt" : return agencyAdapter.getFileEntryStreamHandler(this, executor, size);
			case "routes.txt": return routeAdapter.getFileEntryStreamHandler(this, executor, size);
			case "stops.txt": return stopAdapter.getFileEntryStreamHandler(this, executor, size);
			case "feed_info.txt": return feedInfoAdapter.getFileEntryStreamHandler(this, executor, size);
			case "transfers.txt": return transferAdapter.getFileEntryStreamHandler(this, executor, size);
		}
		return null;
	}

}
