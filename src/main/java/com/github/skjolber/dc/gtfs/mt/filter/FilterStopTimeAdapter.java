package com.github.skjolber.dc.gtfs.mt.filter;

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.mt.StaticCsvMapperAdapter;
import com.github.skjolber.dc.gtfs.mt.StopTimeHandler;
import com.github.skjolber.dc.gtfs.mt.StopTimeSequenceChunkerSplitter;
import com.github.skjolber.dc.model.Stop;
import com.github.skjolber.dc.model.StopTime;
import com.github.skjolber.stcsv.CsvMapper2;
import com.github.skjolber.stcsv.CsvReader;
import com.github.skjolber.stcsv.StaticCsvMapper;
import com.github.skjolber.unzip.FileEntryChunkStreamHandler;
import com.github.skjolber.unzip.FileEntryHandler;
import com.github.skjolber.unzip.FileEntryStreamHandler;
import com.github.skjolber.unzip.csv.AbstractSesselTjonnaCsvFileEntryChunkStreamHandler;
import com.github.skjolber.unzip.csv.AbstractSesselTjonnaCsvFileEntryStreamHandler;
import com.github.skjolber.unzip.csv.CsvLineHandler;
import com.github.skjolber.unzip.csv.CsvLineHandlerFactory;

/**
 * 
 * For chunked / multi-threaded parsing. All stops and trips must already be parsed. 
 * Null trips would indicate that it has been filtered out, and so should its stop times.
 *
 */

@SuppressWarnings("unchecked")
public class FilterStopTimeAdapter implements CsvLineHandlerFactory {

	protected static CsvMapper2<StopTime, GtfsFeed> parser;

	public static CsvMapper2<StopTime, GtfsFeed> getParser() {
		return parser;
	}
	
	private static int getStringAsSeconds(String string) { // 14:30:00
		return 
				(string.charAt(6) - '0') * 10 + (string.charAt(7) - '0') // seconds
				+ (string.charAt(3) - '0') * 600 + (string.charAt(4) - '0') * 60 // minutes
				+ (string.charAt(0) - '0') * 36000 + (string.charAt(1) - '0') * 3600 // hours
				;
	}
	
	static {
		// trip_id,stop_id,stop_sequence,arrival_time,departure_time,pickup_type,drop_off_type,shape_dist_traveled,stop_headsign
		parser = CsvMapper2.builder(StopTime.class, (Class<GtfsFeed>)(Class<?>)GtfsFeed.class)
			.stringField("trip_id")
				.consumer( (t, i, id) -> t.setTrip(i.getTrip(id)) )
				.required()
			.stringField("stop_id")
				.consumer( (t, i, id) -> t.setStop(i.getStop(id)) )
				.required()
			.integerField("stop_sequence")
				.setter(StopTime::setStopSequence)
				.optional()
			.stringField("arrival_time")
				.consumer( (t, v) -> t.setArrivalTime(getStringAsSeconds(v)) )
				.optional()
			.stringField("departure_time")
				.consumer( (t, v) -> t.setDepartureTime(getStringAsSeconds(v)) )
				.optional()
			.integerField("timepoint")
				.setter(StopTime::setTimepoint)
				.optional()
			.integerField("pickup_type")
				.setter(StopTime::setPickupType)
				.optional()
			.integerField("drop_off_type")
				.setter(StopTime::setDropOffType)
				.optional()
			.doubleField("shape_dist_traveled")
				.setter(StopTime::setShapeDistTraveled)
				.optional()
			.stringField("stop_headsign")
				.setter(StopTime::setStopHeadsign)
				.quotedWithoutLinebreaks()
				.optional()
			.stringField("fare_period_id")
				.setter(StopTime::setFarePeriodId)
				.optional()
			.build();
	}
	
	protected Map<Thread, FilterStopTimeHandler> handlers = new ConcurrentHashMap<>(Runtime.getRuntime().availableProcessors() * 2);

	protected int chunkLength;
	protected GtfsFeed gtfsFeed;
	
	public FilterStopTimeAdapter(int chunkLength, GtfsFeed gtfsFeed) {
		this.chunkLength = chunkLength;
		this.gtfsFeed = gtfsFeed;
	}
	
	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("stop_times.txt")) {
			throw new IllegalArgumentException("Unexpected file name " + fileName);
		}
		FilterStopTimeHandler csvLineHandler = (FilterStopTimeHandler) handlers.get(Thread.currentThread());
		if(csvLineHandler == null) {
			csvLineHandler = new FilterStopTimeHandler();
			handlers.put(Thread.currentThread(), csvLineHandler);
		}
		return (CsvLineHandler<T>) csvLineHandler;
	}

	private class StopTimeCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<StopTime> {

		public StopTimeCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		@Override
		protected CsvReader<StopTime> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader, gtfsFeed);
		}
	}

	private class StopTimeCsvFileEntryChunkStreamHandler extends AbstractSesselTjonnaCsvFileEntryChunkStreamHandler<StopTime> {

		public StopTimeCsvFileEntryChunkStreamHandler(String name, Charset charset, CsvLineHandlerFactory csvLineHandlerFactory) {
			super(name, charset, null, csvLineHandlerFactory);
		}

		@Override
		protected StaticCsvMapper<StopTime> createStaticCsvMapper(String firstLine) throws Exception {
			this.fileChunkSplitter = new StopTimeSequenceChunkerSplitter(firstLine, chunkLength);
			
			return new StaticCsvMapperAdapter<StopTime, GtfsFeed>(parser.buildStaticCsvMapper(firstLine)) {

				@Override
				protected GtfsFeed newIntermediateProcessor() {
					return gtfsFeed;
				}
			};
		}		
	}
	
	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		return new StopTimeCsvFileEntryStreamHandler("stop_times.txt", this, size, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return new StopTimeCsvFileEntryChunkStreamHandler("stop_times.txt", StandardCharsets.UTF_8, this);
	}
	
	public void resolveReferences() {
		// add stop times on the stops
		for (FilterStopTimeHandler stopTimeHandler : handlers.values()) {
			for (List<StopTime> list : stopTimeHandler.getStopTimes()) {
				for(StopTime st : list) {
					st.getStop().add(st);
				}
			}
		}

		for (Stop stop : gtfsFeed.getStops()) {
			Collections.sort(stop.getStopTimes(), StopTime.departureTimeComparator);
		}
	}
	
	public Collection<? extends StopTimeHandler> getHandlers() {
		return handlers.values();
	}
	
}
