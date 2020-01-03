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
import com.github.skjolber.dc.model.ServiceCalendar;
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
public class ServiceCalendarAdapter extends GtfsIntermediateProcessor<ServiceCalendar> implements CsvLineHandlerFactory, CsvLineHandler<ServiceCalendar> {

	protected static CsvMapper2<ServiceCalendar, ServiceCalendarAdapter> parser;

	static {
		// service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date
		parser = CsvMapper2.builder(ServiceCalendar.class, ServiceCalendarAdapter.class)
			.stringField("service_id")
				.consumer( (t, i, id) -> i.add(2, id, t))
				.required()
			.stringField("start_date")
				.consumer( (t, i, v) -> i.add(0, v, t) )
				.required()
			.stringField("end_date")
				.consumer( (t, i, v) -> i.add(1, v, t) )
				.required()
			.integerField("monday")
				.setter(ServiceCalendar::setMonday)
				.optional()
			.integerField("tuesday")
				.setter(ServiceCalendar::setTuesday)
				.optional()
			.integerField("wednesday")
				.setter(ServiceCalendar::setWednesday)
				.optional()
			.integerField("thursday")
				.setter(ServiceCalendar::setThursday)
				.optional()
			.integerField("friday")
				.setter(ServiceCalendar::setFriday)
				.optional()
			.integerField("saturday")
				.setter(ServiceCalendar::setSaturday)
				.optional()
			.integerField("sunday")
				.setter(ServiceCalendar::setSunday)
				.optional()
			.build();
	}
	
	protected List<ServiceCalendar> serviceCalendars = new ArrayList<>();

	public ServiceCalendarAdapter(GtfsFeed gtfsFeed) {
		super(3, gtfsFeed);
	}
	
	public List<ServiceCalendar> getServiceCalendars() {
		return serviceCalendars;
	}

	@Override
	public void handleLine(ServiceCalendar value) {
		serviceCalendars.add(value);
	}

	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("calendar.txt")) {
			throw new IllegalArgumentException();
		}
		return (CsvLineHandler<T>)this;
	}
	
	private class ServiceCalendarCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<ServiceCalendar> {

		public ServiceCalendarCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		@Override
		protected CsvReader<ServiceCalendar> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader, ServiceCalendarAdapter.this);
		}
	}

	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		return new ServiceCalendarCsvFileEntryStreamHandler("calendar.txt", this, size, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return null;
	}

	public List<ServiceCalendar> resolveServices() {
		// resolve dates, creating them if necessary
		for (Entry<String, List<ServiceCalendar>> entry : getById(0).entrySet()) {
			LocalDate date = feed.getDate(entry.getKey(), true);
			
			for(ServiceCalendar serviceCalendar : entry.getValue()) {
				serviceCalendar.setStartDate(date);
			}
		}
		for (Entry<String, List<ServiceCalendar>> entry : getById(1).entrySet()) {
			LocalDate date = feed.getDate(entry.getKey(), true);
			for(ServiceCalendar serviceCalendar : entry.getValue()) {
				serviceCalendar.setEndDate(date);
			}
		}
		
		// resolve services, creating them if necessary
		for (Entry<String, List<ServiceCalendar>> entry : getById(2).entrySet()) {
			Service service = feed.getService(entry.getKey());
			if(service == null) {
				service = new Service();
				service.setId(entry.getKey());
				feed.putServiceById(entry.getKey(), service);
			}
			for(ServiceCalendar s : entry.getValue()) {
				service.add(s);
				s.setService(service);
			}
		}
		
		return serviceCalendars;
	}
	
	
	
}
