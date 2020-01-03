package com.github.skjolber.dc.gtfs.mt;

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.GtfsIntermediateProcessor;
import com.github.skjolber.dc.model.FeedId;
import com.github.skjolber.dc.model.Route;
import com.github.skjolber.dc.model.Service;
import com.github.skjolber.dc.model.Trip;
import com.github.skjolber.stcsv.CsvReader;
import com.github.skjolber.stcsv.databinder.CsvMapper2;
import com.github.skjolber.stcsv.databinder.StaticCsvMapper;
import com.github.skjolber.unzip.FileChunkSplitter;
import com.github.skjolber.unzip.FileEntryChunkStreamHandler;
import com.github.skjolber.unzip.FileEntryHandler;
import com.github.skjolber.unzip.FileEntryStreamHandler;
import com.github.skjolber.unzip.NewlineChunkSplitter;
import com.github.skjolber.unzip.csv.AbstractSesselTjonnaCsvFileEntryChunkStreamHandler;
import com.github.skjolber.unzip.csv.AbstractSesselTjonnaCsvFileEntryStreamHandler;
import com.github.skjolber.unzip.csv.CsvLineHandler;
import com.github.skjolber.unzip.csv.CsvLineHandlerFactory;

/**
 * 
 * For chunked / multi-threaded parsing.
 *
 */

@SuppressWarnings("unchecked")
public class TripAdapter implements CsvLineHandlerFactory {

	protected static CsvMapper2<Trip, GtfsIntermediateProcessor<Trip>> parser;

	static {
		parser = CsvMapper2.builder(Trip.class, (Class<GtfsIntermediateProcessor<Trip>>)(Class<?>)GtfsIntermediateProcessor.class)
			.stringField("route_id")
				.consumer( (t, i, id) -> i.add(0, id,  t))
				.quoted()
				.optional()
			.stringField("service_id")
				.consumer( (t, i, id) -> i.add(1, id, t) )
				.required()
			.stringField("trip_id")
				.consumer( (t, id) -> t.setId(id) )
				.required()
			.stringField("trip_headsign")
				.setter(Trip::setHeadsign)
				.quoted()
				.optional()
			.stringField("direction_id")
				.setter(Trip::setDirectionId)
				.optional()
			.stringField("shape_id")
				.consumer( (t, i, id) -> t.setShapeId(new FeedId(i.getFeed().getAgencyId(), id) ) )
				.optional()
			.integerField("wheelchair_accessible")
				.setter(Trip::setWheelchairAccessible)
				.optional()
			.build();
	}
	
	protected Map<Thread, TripHandler> handlers = new ConcurrentHashMap<>(Runtime.getRuntime().availableProcessors() * 2);
	protected Map<Thread, GtfsIntermediateProcessor<Trip>> processors = new ConcurrentHashMap<>(Runtime.getRuntime().availableProcessors());

	protected int chunkLength;
	protected GtfsFeed feed;
		
	public TripAdapter(int chunkLength, GtfsFeed feed) {
		this.chunkLength = chunkLength;
		this.feed = feed;
	}

	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("trips.txt")) {
			throw new IllegalArgumentException();
		}
		TripHandler csvLineHandler = (TripHandler) handlers.get(Thread.currentThread());
		if(csvLineHandler == null) {
			csvLineHandler = new TripHandler();
			handlers.put(Thread.currentThread(), csvLineHandler);
		}
		return (CsvLineHandler<T>) csvLineHandler;
	}
	
	private class TripCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<Trip> {

		public TripCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}


		public TripCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		@Override
		protected CsvReader<Trip> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader, newIntermediateProcessor());
		}
	}

	private class TripCsvFileEntryChunkStreamHandler extends AbstractSesselTjonnaCsvFileEntryChunkStreamHandler<Trip> {
		
		public TripCsvFileEntryChunkStreamHandler(String name, Charset charset, FileChunkSplitter fileChunkSplitter, CsvLineHandlerFactory csvLineHandlerFactory) {
			super(name, charset, fileChunkSplitter, csvLineHandlerFactory);
		}

		@Override
		protected StaticCsvMapper<Trip> createStaticCsvMapper(String firstLine) throws Exception {
			return new StaticCsvMapperAdapter<Trip, GtfsIntermediateProcessor<Trip>>(parser.buildStaticCsvMapper(firstLine)) {

				@Override
				protected GtfsIntermediateProcessor<Trip> newIntermediateProcessor() {
					return TripAdapter.this.newIntermediateProcessor();
				}
			};
		}
	}
	
	protected GtfsIntermediateProcessor<Trip> newIntermediateProcessor() {
		GtfsIntermediateProcessor<Trip> processor = processors.get(Thread.currentThread());
		if(processor == null) {
			processor = new GtfsIntermediateProcessor<Trip>(2, feed);
			processors.put(Thread.currentThread(), processor);
		}
		return processor;
	}
	
	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		/*
		if(executor.getCorePoolSize() >= 4) {
			// prevent deadlocks by carefully choosing the parallel buffer size
			return new TripCsvFileEntryStreamHandler("trips.txt", this, Math.min(chunkLength, size), fileEntryHandler, executor);
		}
		*/
		return new TripCsvFileEntryStreamHandler("trips.txt", this, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return new TripCsvFileEntryChunkStreamHandler("trips.txt", StandardCharsets.UTF_8, new NewlineChunkSplitter(chunkLength), this);
	}
	
	public void resolveRoutes() {
		for (GtfsIntermediateProcessor<Trip> p : processors.values()) {
			Map<String, List<Trip>> tripByRouteId = p.getById(0);
			for (Entry<String, List<Trip>> entry : tripByRouteId.entrySet()) {
				Route route = feed.getRoute(entry.getKey());
				for(Trip trip : entry.getValue()) {
					trip.setRoute(route);
				}
			}
		}
	}
	
	public void resolveServices() {
		for (GtfsIntermediateProcessor<Trip> p : processors.values()) {
			Map<String, List<Trip>> tripByServiceId = p.getById(1);
			for (Entry<String, List<Trip>> entry : tripByServiceId.entrySet()) {
				Service s = feed.getService(entry.getKey());
				if(s == null) {
					throw new RuntimeException("Unknown service " + entry.getKey());
				}
				for(Trip trip : entry.getValue()) {
					trip.setService(s);
					s.add(trip);
				}
			}
		}
	}
	
	public List<Trip> resolveTrips() {
		List<Trip> trips = new ArrayList<>(4096);
		for (TripHandler tripHandler : handlers.values()) {
			trips.addAll(tripHandler.getTrips());
		}
		return trips;
	}

}
