package com.github.skjolber.dc.gtfs.st.filter;

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
 * For single-threaded parsing. Assumes valid dates are already created in the feed.
 *
 */

@SuppressWarnings("unchecked")
public class FilterServiceCalendarAdapter extends GtfsIntermediateProcessor<ServiceCalendar> implements CsvLineHandlerFactory, CsvLineHandler<ServiceCalendar> {

	protected static CsvMapper2<ServiceCalendar, FilterServiceCalendarAdapter> parser;

	static {
		// service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date
		parser = CsvMapper2.builder(ServiceCalendar.class, FilterServiceCalendarAdapter.class)
			.stringField("service_id")
				.consumer( (t, i, id) -> i.add(0, id, t))
				.required()
			.stringField("start_date")
				.consumer( (t, i, v) -> t.setStartDate(i.getFeed().getDate(v, false)) )
				.required()
			.stringField("end_date")
				.consumer( (t, i, v) -> t.setEndDate(i.getFeed().getDate(v, false)) )
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
	protected LocalDate startDate;
	protected LocalDate endDate;

	public FilterServiceCalendarAdapter(GtfsFeed feed, LocalDate startDate, LocalDate endDate) {
		super(1, feed);
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public List<ServiceCalendar> getServiceCalendars() {
		return serviceCalendars;
	}

	@Override
	public void handleLine(ServiceCalendar serviceCalendar) {
		// add if either start and end date is present
		// set the the upper/lower bound if only one of the dates exists
		if(serviceCalendar.getStartDate() != null && serviceCalendar.getEndDate() != null) {
			serviceCalendars.add(serviceCalendar);
		} else if(serviceCalendar.getStartDate() != null) {
			serviceCalendar.setEndDate(endDate);
			serviceCalendars.add(serviceCalendar);
		} else if(serviceCalendar.getEndDate() != null) {
			serviceCalendar.setStartDate(startDate);
			serviceCalendars.add(serviceCalendar);
		}
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
			return parser.create(reader, FilterServiceCalendarAdapter.this);
		}
	}

	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		return new ServiceCalendarCsvFileEntryStreamHandler("calendar.txt", this, size, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return null;
	}

	public List<ServiceCalendar> resolveServices() {
		// resolve services, creating them if necessary
		for (Entry<String, List<ServiceCalendar>> entry : getById(0).entrySet()) {
			Service service = feed.getService(entry.getKey());
			if(service == null) {
				service = new Service();
				service.setId(entry.getKey());
				feed.putServiceById(entry.getKey(), service);
			}
			for(ServiceCalendar s : entry.getValue()) {
				if(s.getStartDate() != null && s.getEndDate() != null) {
					service.add(s);
					s.setService(service);
				}
			}
		}
		
		return serviceCalendars;
	}
		
	
}
