package com.github.skjolber.dc.gtfs.st;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.model.Agency;
import com.github.skjolber.stcsv.CsvReader;
import com.github.skjolber.stcsv.databinder.CsvMapper;
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

public class AgencyAdapter implements CsvLineHandlerFactory, CsvLineHandler<Agency> {

	protected static CsvMapper<Agency> parser;

	static {
		// agency_id,agency_name,agency_url,agency_timezone,agency_phone
		parser = CsvMapper.builder(Agency.class)
			.stringField("agency_id")
				.setter(Agency::setId)
				.optional()
			.stringField("agency_name")
				.setter(Agency::setName)
				.quoted()
				.required()
			.stringField("agency_url")
				.setter(Agency::setUrl)
				.required()
			.stringField("agency_timezone")
				.setter(Agency::setTimezone)
				.required()
			.stringField("agency_lang")
				.setter(Agency::setLang)
				.optional()				
			.stringField("agency_phone")
				.setter(Agency::setPhone)
				.optional()				
			.stringField("agency_fare_url")
				.setter(Agency::setFareUrl)
				.optional()				
			.stringField("agency_email")
				.setter(Agency::setEmail)
				.optional()				
			.build();
	}

	protected List<Agency> agencies = new ArrayList<>();

	public List<Agency> getAgencies() {
		return agencies;
	}

	@Override
	public void handleLine(Agency value) {
		agencies.add(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("agency.txt")) {
			throw new IllegalArgumentException();
		}
		return (CsvLineHandler<T>)this;
	}
	
	private class RouteCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<Agency> {

		public RouteCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		@Override
		protected CsvReader<Agency> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader);
		}
	}

	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		return new RouteCsvFileEntryStreamHandler("agency.txt", this, size, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return null;
	}
	
}
