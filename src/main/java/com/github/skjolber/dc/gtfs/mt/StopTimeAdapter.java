package com.github.skjolber.dc.gtfs.mt;

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.IntermediateProcessor;
import com.github.skjolber.dc.model.Stop;
import com.github.skjolber.dc.model.StopTime;
import com.github.skjolber.dc.model.Trip;
import com.github.skjolber.stcsv.CsvReader;
import com.github.skjolber.stcsv.databinder.CsvMapper2;
import com.github.skjolber.stcsv.databinder.StaticCsvMapper;
import com.github.skjolber.unzip.FileEntryChunkStreamHandler;
import com.github.skjolber.unzip.FileEntryHandler;
import com.github.skjolber.unzip.FileEntryStreamHandler;
import com.github.skjolber.unzip.csv.AbstractSesselTjonnaCsvFileEntryChunkStreamHandler;
import com.github.skjolber.unzip.csv.AbstractSesselTjonnaCsvFileEntryStreamHandler;
import com.github.skjolber.unzip.csv.CsvLineHandler;
import com.github.skjolber.unzip.csv.CsvLineHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For chunked / multi-threaded parsing.
 *
 */

@SuppressWarnings("unchecked")
public class StopTimeAdapter implements CsvLineHandlerFactory {

	private static final Logger log = LoggerFactory.getLogger(StopTimeAdapter.class);
	protected static CsvMapper2<StopTime, IntermediateProcessor<StopTime>> parser;

	public static CsvMapper2<StopTime, IntermediateProcessor<StopTime>> getParser() {
		return parser;
	}
	
	private static int getStringAsSeconds(String string) { // 1114:30:00
		int length = string.length();
		if(length == 8) {
			return
					(string.charAt(6) - '0') * 10 + (string.charAt(7) - '0') // seconds
							+ (string.charAt(3) - '0') * 600 + (string.charAt(4) - '0') * 60 // minutes
							+ (string.charAt(0) - '0') * 36000 + (string.charAt(1) - '0') * 3600 // hours
					;
		}
		return 
				(string.charAt(length - 2) - '0') * 10 + (string.charAt(length - 1) - '0') // seconds
				+ (string.charAt(length - 5) - '0') * 600 + (string.charAt(length - 4) - '0') * 60 // minutes
				+ Integer.parseInt(string, 0, length - 6, 10) * 3600 // hours
				;
	}
	
	static {
		// trip_id,stop_id,stop_sequence,arrival_time,departure_time,pickup_type,drop_off_type,shape_dist_traveled,stop_headsign
		parser = CsvMapper2.builder(StopTime.class, (Class<IntermediateProcessor<StopTime>>)(Class<?>)IntermediateProcessor.class)
			.stringField("trip_id")
				.consumer( (t, i, id) -> i.add(0, id, t))
				.required()
			.stringField("stop_id")
				.consumer( (t, i, id) -> i.add(1, id, t)  )
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
			.build();
	}
	
	protected Map<Thread, StopTimeHandler> handlers = new ConcurrentHashMap<>(Runtime.getRuntime().availableProcessors());
	protected Map<Thread, IntermediateProcessor<StopTime>> processors = new ConcurrentHashMap<>(Runtime.getRuntime().availableProcessors());

	protected final int chunkLength;
	protected GtfsFeed feed;
	
	public StopTimeAdapter(int chunkLength, GtfsFeed feed) {
		this.chunkLength = chunkLength;
		this.feed = feed;
	}
	
	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("stop_times.txt")) {
			throw new IllegalArgumentException("Unexpected file name " + fileName);
		}
		StopTimeHandler csvLineHandler = (StopTimeHandler) handlers.get(Thread.currentThread());
		if(csvLineHandler == null) {
			csvLineHandler = new StopTimeHandler();
			handlers.put(Thread.currentThread(), csvLineHandler);
		}
		return (CsvLineHandler<T>) csvLineHandler;
	}

	private class StopTimeCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<StopTime> {

		public StopTimeCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler delegate, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, delegate, executor);
		}

		public StopTimeCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, FileEntryHandler delegate, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, delegate, executor);
		}

		@Override
		protected CsvReader<StopTime> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader, newIntermediateProcessor());
		}
	}

	private class StopTimeCsvFileEntryChunkStreamHandler extends AbstractSesselTjonnaCsvFileEntryChunkStreamHandler<StopTime> {

		public StopTimeCsvFileEntryChunkStreamHandler(String name, Charset charset, CsvLineHandlerFactory csvLineHandlerFactory) {
			super(name, charset, null, csvLineHandlerFactory);
		}

		@Override
		protected StaticCsvMapper<StopTime> createStaticCsvMapper(String firstLine) throws Exception {
			this.fileChunkSplitter = new StopTimeSequenceChunkerSplitter(firstLine, chunkLength);
			
			return new StaticCsvMapperAdapter<StopTime, IntermediateProcessor<StopTime>>(parser.buildStaticCsvMapper(firstLine)) {

				@Override
				protected IntermediateProcessor<StopTime> newIntermediateProcessor() {
					return StopTimeAdapter.this.newIntermediateProcessor();
				}
			};
		}
	}
	
	protected IntermediateProcessor<StopTime> newIntermediateProcessor() {
		IntermediateProcessor<StopTime> processor = processors.get(Thread.currentThread());
		if(processor == null) {
			processor = new IntermediateProcessor<StopTime>(2);
			processors.put(Thread.currentThread(), processor);
		}
		return processor;
	}
	
	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler delegate, ThreadPoolExecutor executor, long size) throws Exception {
		/*
		if(executor.getCorePoolSize() >= 2) {
			// prevent deadlocks by carefully choosing the parallel buffer size
			return new StopTimeCsvFileEntryStreamHandler("stop_times.txt", this, Math.min(chunkLength, size), delegate, executor);
		}
		*/
		return new StopTimeCsvFileEntryStreamHandler("stop_times.txt", this, delegate, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return new StopTimeCsvFileEntryChunkStreamHandler("stop_times.txt", StandardCharsets.UTF_8, this);
	}
	
	public void resolveReferences() {
		log.info("Resolve stop time references");
		for (IntermediateProcessor<StopTime> p : processors.values()) {
			for (Entry<String, List<StopTime>> entry : p.getById(1).entrySet()) {
				Stop stop = feed.getStop(entry.getKey());
				
				List<StopTime> list = entry.getValue();
				for(StopTime stopTime : list) {
					stopTime.setStop(stop);
					stop.add(stopTime);
				}
			}
			for (Entry<String, List<StopTime>> entry : p.getById(0).entrySet()) {
				Trip trip = feed.getTrip(entry.getKey());
				if(trip == null) {
					throw new RuntimeException();
				}
				for(StopTime stopTime : entry.getValue()) {
					stopTime.setTrip(trip);
				}
			}
		}
		
		for (StopTimeHandler stopTimeHandler : handlers.values()) {
			for (List<StopTime> list : stopTimeHandler.getStopTimes()) {
				//log.info("Process " + list.size() + " stop times");

				if(!list.isEmpty()) {
					// handle inconsistencies in sequence numbering here


					for(int i = 0; i < list.size() - 1; i++) {
						if(list.get(i).getTrip() != list.get(i + 1).getTrip()) {
							StopTime last = list.get(i);
							StopTime first = list.get(i + 1);
								
							last.setNext(null);
							first.setPrevious(null);
							
							last.getTrip().setStopTimes(list.subList(0, i + 1));
							
							list = list.subList(i + 1, list.size());
							i = 0;
							
							// TODO normalize sequence numbers too?
						}
					}


					if(!list.isEmpty()) {
						Trip trip = list.get(0).getTrip();
						if(trip.getStopTimes() != null) {
							throw new RuntimeException(trip.getId() + " already has " + trip.getStopTimes().size() + " stop times, wanted to set " + list.size());
						}
						trip.setStopTimes(list);
					}
				}
			}
		}
		
		for (Stop stop : feed.getStops()) {
			Collections.sort(stop.getStopTimes(), StopTime.departureTimeComparator);
		}		
	}
	
	public Collection<StopTimeHandler> getHandlers() {
		return handlers.values();
	}

}
