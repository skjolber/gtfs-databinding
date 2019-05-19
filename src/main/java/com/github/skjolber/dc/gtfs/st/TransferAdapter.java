package com.github.skjolber.dc.gtfs.st;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.GtfsIntermediateProcessor;
import com.github.skjolber.dc.model.Stop;
import com.github.skjolber.dc.model.Transfer;
import com.github.skjolber.stcsv.CsvMapper2;
import com.github.skjolber.stcsv.CsvReader;
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
public class TransferAdapter extends GtfsIntermediateProcessor<Transfer> implements CsvLineHandlerFactory, CsvLineHandler<Transfer> {

	protected static CsvMapper2<Transfer, TransferAdapter> parser;

	static {
		// service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date
		parser = CsvMapper2.builder(Transfer.class, TransferAdapter.class)
			.stringField("from_stop_id")
				.consumer( (t, i, id) -> i.add(0, id, t))
				.required()
			.stringField("to_stop_id")
				.consumer( (t, i, id) -> i.add(1, id, t))
				.required()
			.integerField("transfer_type")
				.setter(Transfer::setType)
				.required()
			.integerField("min_transfer_time")
				.setter(Transfer::setMinTime)
				.optional()
			.build();
	}
	
	protected List<Transfer> transfers = new ArrayList<>();

	public TransferAdapter(GtfsFeed gtfsFeed) {
		super(2, gtfsFeed);
	}
	
	public List<Transfer> getServiceCalendars() {
		return transfers;
	}

	@Override
	public void handleLine(Transfer value) {
		transfers.add(value);
	}

	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("transfers.txt")) {
			throw new IllegalArgumentException();
		}
		return (CsvLineHandler<T>)this;
	}
	
	private class ServiceCalendarCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<Transfer> {

		public ServiceCalendarCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		@Override
		protected CsvReader<Transfer> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader, TransferAdapter.this);
		}
	}

	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		return new ServiceCalendarCsvFileEntryStreamHandler("transfers.txt", this, size, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return null;
	}

	public List<Transfer> resolveStops() {
		// resolve dates, creating them if necessary
		for (Entry<String, List<Transfer>> entry : getById(0).entrySet()) {
			Stop stop = feed.getStop(entry.getKey());
			
			for(Transfer transfer : entry.getValue()) {
				transfer.setFrom(stop);
			}
		}

		for (Entry<String, List<Transfer>> entry : getById(1).entrySet()) {
			Stop stop = feed.getStop(entry.getKey());
			
			for(Transfer transfer : entry.getValue()) {
				transfer.setTo(stop);
			}
		}

		feed.getTransfers().addAll(transfers);
		
		return transfers;
	}
	
}
