package com.github.skjolber.dc.gtfs.st;

import java.io.Reader;
import java.time.LocalDate;
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
 * For single-threaded parsing.
 *
 */

@SuppressWarnings("unchecked")
public class ServiceCalendarDateAdapter extends GtfsIntermediateProcessor<ServiceCalendarDate> implements CsvLineHandlerFactory, CsvLineHandler<ServiceCalendarDate> {

	protected static CsvMapper2<ServiceCalendarDate, GtfsIntermediateProcessor<ServiceCalendarDate>> parser;

	static {
		// service_id,date,exception_type
		parser = CsvMapper2.builder(ServiceCalendarDate.class, (Class<GtfsIntermediateProcessor<ServiceCalendarDate>>)(Class<?>)GtfsIntermediateProcessor.class)
			.stringField("date")
				.consumer( (t, i, id) -> i.add(0, id,  t))
				.quoted()
				.optional()
			.integerField("exception_type")
				.setter(ServiceCalendarDate::setExceptionType)
				.optional()
			.stringField("service_id")
				.consumer( (t, i, id) -> i.add(1, id, t) )
				.required()
			.build();
	}

	protected List<ServiceCalendarDate> serviceCalendarDates = new ArrayList<>();
	
	public ServiceCalendarDateAdapter(GtfsFeed feed) {
		super(2, feed);
	}

	public List<ServiceCalendarDate> getServiceCalendarDates() {
		return serviceCalendarDates;
	}
	
	@Override
	public void handleLine(ServiceCalendarDate value) {
		serviceCalendarDates.add(value);
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
			return parser.create(reader, ServiceCalendarDateAdapter.this);
		}
	}

	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		return new RouteCsvFileEntryStreamHandler("calendar_dates.txt", this, size, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return null;
	}
	
	public List<ServiceCalendarDate> resolveServices() {
		// resolve dates, creating them if necessary
		for (Entry<String, List<ServiceCalendarDate>> entry : getById(0).entrySet()) {
			LocalDate date = feed.getDate(entry.getKey(), true);
			for(ServiceCalendarDate serviceCalendar : entry.getValue()) {
				serviceCalendar.setDate(date);
			}
		}

		// resolve services, creating them if necessary
		for (Entry<String, List<ServiceCalendarDate>> entry : getById(1).entrySet()) {
			Service service = feed.getService(entry.getKey());
			if(service == null) {
				service = new Service();
				service.setId(entry.getKey());
				feed.putServiceById(service.getId(), service);
			}
			for(ServiceCalendarDate s : entry.getValue()) {
				service.add(s);
				s.setService(service);
			}
		}
		return serviceCalendarDates;
	}	
	
}
