package com.github.skjolber.dc.gtfs.st;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.GtfsOneToManyProcessor;
import com.github.skjolber.dc.model.Agency;
import com.github.skjolber.dc.model.Route;
import com.github.skjolber.stcsv.CsvReader;
import com.github.skjolber.stcsv.databinder.CsvMapper2;
import com.github.skjolber.unzip.FileEntryChunkStreamHandler;
import com.github.skjolber.unzip.FileEntryHandler;
import com.github.skjolber.unzip.FileEntryStreamHandler;
import com.github.skjolber.unzip.csv.AbstractSesselTjonnaCsvFileEntryStreamHandler;
import com.github.skjolber.unzip.csv.CsvLineHandler;
import com.github.skjolber.unzip.csv.CsvLineHandlerFactory;

/**
 * For single-threaded parsing.
 *
 */

@SuppressWarnings("unchecked")
public class RouteAdapter extends GtfsOneToManyProcessor<Route> implements CsvLineHandlerFactory, CsvLineHandler<Route> {

	protected static CsvMapper2<Route, GtfsOneToManyProcessor<Route>> parser;

	static {
		// route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color
		parser = CsvMapper2.builder(Route.class, (Class<GtfsOneToManyProcessor<Route>>)(Class<?>)GtfsOneToManyProcessor.class)
			.stringField("agency_id")
				.consumer( (t, i, id) -> i.add(id,  t))
				.quoted()
				.optional()
			.stringField("route_id")
				.setter(Route::setId)
				.required()
			.stringField("route_short_name")
				.setter(Route::setShortName)
				.quoted()
				.optional()
			.stringField("route_long_name")
				.setter(Route::setLongName)
				.quoted()
				.optional()
			.stringField("route_desc")
				.setter(Route::setDesc)
				.quoted()
				.optional()
			.integerField("route_type")
				.setter(Route::setType)
				.required()
			.stringField("route_url")
				.setter(Route::setUrl)
				.optional()
			.stringField("route_color")
				.consumer((r, v) -> r.setColor(parseColor(v)))
				.optional()
			.stringField("route_text_color")
				.consumer((r, v) -> r.setTextColor(parseColor(v)))
				.optional()
			.integerField("route_sort_order")
				.setter(Route::setSortOrder)
				.optional()
			.build();
	}

	protected List<Route> routes = new ArrayList<>();

	public RouteAdapter(GtfsFeed feed) {
		super(feed);
	}
	
	public List<Route> getRoutes() {
		return routes;
	}

	private static int parseColor(String rawValue) {
	    try {
	        return Integer.parseInt( rawValue, 16);
	    } catch ( NumberFormatException nfe ) {
	        throw new RuntimeException( rawValue + " is not an integer." );
	    }
	}

	@Override
	public void handleLine(Route value) {
		routes.add(value);
	}

	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("routes.txt")) {
			throw new IllegalArgumentException();
		}
		return (CsvLineHandler<T>)this;
	}
	
	private class RouteCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<Route> {

		public RouteCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		@Override
		protected CsvReader<Route> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader, RouteAdapter.this);
		}
	}

	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		return new RouteCsvFileEntryStreamHandler("routes.txt", this, size, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return null;
	}
	
	public void resolveReferences(GtfsFeed gtfsFeed) {
		for (Entry<String, List<Route>> entry : entityById.entrySet()) {
			Agency route = gtfsFeed.getAgency(entry.getKey());
			for(Route trip : entry.getValue()) {
				trip.setAgency(route);
			}
		}
	}
	
}
