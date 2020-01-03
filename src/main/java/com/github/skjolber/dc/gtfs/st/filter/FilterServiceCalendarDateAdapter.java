package com.github.skjolber.dc.gtfs.st.filter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.GtfsIntermediateProcessor;
import com.github.skjolber.dc.model.Service;
import com.github.skjolber.dc.model.ServiceCalendarDate;
import com.github.skjolber.stcsv.CsvReader;
import com.github.skjolber.stcsv.databinder.CsvMapper2;
import com.github.skjolber.unzip.FileEntryChunkStreamHandler;
import com.github.skjolber.unzip.FileEntryHandler;
import com.github.skjolber.unzip.FileEntryStreamHandler;
import com.github.skjolber.unzip.csv.AbstractSesselTjonnaCsvFileEntryStreamHandler;
import com.github.skjolber.unzip.csv.CsvLineHandler;
import com.github.skjolber.unzip.csv.CsvLineHandlerFactory;

/**
 * 
 * For single-threaded parsing. Assumes valid dates are already created in the feed.
 *
 */

@SuppressWarnings("unchecked")
public class FilterServiceCalendarDateAdapter extends GtfsIntermediateProcessor<ServiceCalendarDate> implements CsvLineHandlerFactory, CsvLineHandler<ServiceCalendarDate> {

	protected static CsvMapper2<ServiceCalendarDate, GtfsIntermediateProcessor<ServiceCalendarDate>> parser;

	static {
		// service_id,date,exception_type
		parser = CsvMapper2.builder(ServiceCalendarDate.class, (Class<GtfsIntermediateProcessor<ServiceCalendarDate>>)(Class<?>)GtfsIntermediateProcessor.class)
			.stringField("date")
				.consumer( (t, i, id) -> t.setDate(i.getFeed().getDate(id, false)))
				.quoted()
				.optional()
			.integerField("exception_type")
				.setter(ServiceCalendarDate::setExceptionType)
				.optional()
			.stringField("service_id")
				.consumer( (t, i, id) -> i.add(0, id, t) )
				.required()
			.build();
	}

	protected List<ServiceCalendarDate> serviceCalendarDates = new ArrayList<>();

	public FilterServiceCalendarDateAdapter(GtfsFeed feed) {
		super(1, feed);
	}

	@Override
	public void handleLine(ServiceCalendarDate value) {
		// discard dates which are outside the filter window
		if(value.getDate() != null) {
			serviceCalendarDates.add(value);
		}
	}

	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("calendar_dates.txt")) {
			throw new IllegalArgumentException();
		}
		return (CsvLineHandler<T>)this;
	}
	
	private class RouteCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<ServiceCalendarDate> {

		public RouteCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		@Override
		protected CsvReader<ServiceCalendarDate> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader, FilterServiceCalendarDateAdapter.this);
		}
	}

	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		return new RouteCsvFileEntryStreamHandler("calendar_dates.txt", this, size, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return null;
	}

	public List<ServiceCalendarDate> resolveServices() {
		// resolve services, creating them if necessary
		for (Entry<String, List<ServiceCalendarDate>> entry : getById(0).entrySet()) {
			Service service = feed.getService(entry.getKey());
			if(service == null) {
				service = new Service();
				service.setId(entry.getKey());
				feed.putServiceById(entry.getKey(), service);
			}
			for(ServiceCalendarDate s : entry.getValue()) {
				if(s.getDate() != null) {
					service.add(s);
					s.setService(service);
				}
			}
		}
		return serviceCalendarDates;
	}
	
}
