package com.github.skjolber.dc.gtfs.mt;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.GtfsIntermediateProcessor;
import com.github.skjolber.dc.gtfs.GtfsOneToManyProcessor;
import com.github.skjolber.dc.gtfs.IntermediateProcessor;
import com.github.skjolber.dc.model.ShapePoint;
import com.github.skjolber.dc.model.StopTime;
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

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 
 * For chunked / multi-threaded parsing.
 *
 */

@SuppressWarnings("unchecked")
public class ShapePointAdapter implements CsvLineHandlerFactory {

	protected static CsvMapper2<ShapePoint, GtfsIntermediateProcessor<ShapePoint>> parser;

	static {
		// stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station,wheelchair_boarding,stop_timezone,platform_code,vehicle_type
		parser = CsvMapper2.builder(ShapePoint.class, (Class<GtfsIntermediateProcessor<ShapePoint>>)(Class<?>)GtfsIntermediateProcessor.class)
				.stringField("shape_id")
				.setter(ShapePoint::setShapeId)
				.required()
				.integerField("shape_pt_sequence")
				.setter(ShapePoint::setSequence)
				.optional()
				.doubleField("shape_pt_lat")
				.setter(ShapePoint::setLat)
				.required()
				.doubleField("shape_pt_lon")
				.setter(ShapePoint::setLon)
				.required()
				.build();
	}

	protected Map<Thread, ShapePointHandler> handlers = new ConcurrentHashMap<>(Runtime.getRuntime().availableProcessors() * 2);
	protected Map<Thread, GtfsIntermediateProcessor<ShapePoint>> processors = new ConcurrentHashMap<>(Runtime.getRuntime().availableProcessors());

	protected int chunkLength;
	protected GtfsFeed feed;

	public ShapePointAdapter(int chunkLength, GtfsFeed feed) {
		this.chunkLength = chunkLength;
		this.feed = feed;
	}

	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("shapes.txt")) {
			throw new IllegalArgumentException();
		}
		ShapePointHandler csvLineHandler = handlers.get(Thread.currentThread());
		if(csvLineHandler == null) {
			csvLineHandler = new ShapePointHandler();
			handlers.put(Thread.currentThread(), csvLineHandler);
		}
		return (CsvLineHandler<T>) csvLineHandler;
	}
	
	private class ShapePointCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<ShapePoint> {

		public ShapePointCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		public ShapePointCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		@Override
		protected CsvReader<ShapePoint> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader, newIntermediateProcessor());
		}
	}

	private class ShapePointCsvFileEntryChunkStreamHandler extends AbstractSesselTjonnaCsvFileEntryChunkStreamHandler<ShapePoint> {
		
		public ShapePointCsvFileEntryChunkStreamHandler(String name, Charset charset, FileChunkSplitter fileChunkSplitter, CsvLineHandlerFactory csvLineHandlerFactory) {
			super(name, charset, fileChunkSplitter, csvLineHandlerFactory);
		}

		@Override
		protected StaticCsvMapper<ShapePoint> createStaticCsvMapper(String firstLine) throws Exception {
			return new StaticCsvMapperAdapter<ShapePoint, GtfsIntermediateProcessor<ShapePoint>>(parser.buildStaticCsvMapper(firstLine)) {

				@Override
				protected GtfsIntermediateProcessor<ShapePoint> newIntermediateProcessor() {
					return ShapePointAdapter.this.newIntermediateProcessor();
				}
			};
		}
	}
	
	protected GtfsIntermediateProcessor<ShapePoint> newIntermediateProcessor() {
		GtfsIntermediateProcessor<ShapePoint> processor = processors.get(Thread.currentThread());
		if(processor == null) {
			processor = new GtfsIntermediateProcessor<ShapePoint>(2, feed);
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
		return new ShapePointCsvFileEntryStreamHandler("shapes.txt", this, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return new ShapePointCsvFileEntryChunkStreamHandler("shapes.txt", StandardCharsets.UTF_8, new NewlineChunkSplitter(chunkLength), this);
	}

	public List<ShapePoint> resolveShapePoints() {
		List<ShapePoint> trips = new ArrayList<>(4096);
		for (ShapePointHandler tripHandler : handlers.values()) {
			trips.addAll(tripHandler.getShapePoints());
		}
		return trips;
	}

}
