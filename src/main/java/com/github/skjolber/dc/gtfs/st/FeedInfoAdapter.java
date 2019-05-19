package com.github.skjolber.dc.gtfs.st;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.model.FeedInfo;
import com.github.skjolber.stcsv.CsvReader;
import com.github.skjolber.stcsv.sa.StringArrayCsvReader;
import com.github.skjolber.unzip.FileEntryChunkStreamHandler;
import com.github.skjolber.unzip.FileEntryHandler;
import com.github.skjolber.unzip.FileEntryStreamHandler;
import com.github.skjolber.unzip.csv.AbstractSesselTjonnaCsvFileEntryStreamHandler;
import com.github.skjolber.unzip.csv.CsvLineHandler;
import com.github.skjolber.unzip.csv.CsvLineHandlerFactory;

/**
 * For single-threaded parsing. This uses the statically typed CSV parser.
 *
 */

public class FeedInfoAdapter implements CsvLineHandlerFactory, CsvLineHandler<String[]> {

	protected Map<String, FeedInfo> feedInfoById = new HashMap<>(1024);

	public Map<String, FeedInfo> getFeedInfoById() {
		return feedInfoById;
	}

	@Override
	public void handleLine(String[] value) {
		FeedInfo feedInfo = new FeedInfo();
		feedInfo.setId(value[0]);
		feedInfo.setPublisherName(value[1]);
		feedInfoById.put(value[0], feedInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("feed_info.txt")) {
			throw new IllegalArgumentException();
		}
		return (CsvLineHandler<T>)this;
	}
	
	private class RouteCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<String[]> {

		public RouteCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, fileEntryHandler, executor);
		}

		@Override
		protected CsvReader<String[]> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return StringArrayCsvReader.builder()
					.withColumnMapping("feed_id", 0)
					.withColumnMapping("feed_publisher_name", 1)
					.build(reader);
		}
	}

	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler fileEntryHandler, ThreadPoolExecutor executor, long size) throws Exception {
		return new RouteCsvFileEntryStreamHandler("feed_info.txt", this, size, fileEntryHandler, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return null;
	}
	
}
