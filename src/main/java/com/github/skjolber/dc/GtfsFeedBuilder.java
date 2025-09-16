package com.github.skjolber.dc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.gtfs.AbstractGtfsChunkedCsvFileEntryHandler;
import com.github.skjolber.dc.gtfs.FilterGtfsChunkedCsvFileEntryHandler;
import com.github.skjolber.dc.gtfs.GtfsChunkedCsvFileEntryHandler;
import com.github.skjolber.unzip.ChunkSplitterFileEntryHandler;
import com.github.skjolber.unzip.FileZipFileFactory;
import com.github.skjolber.unzip.UrlZipFileFactory;
import com.github.skjolber.unzip.ZipFileEngine;
import com.github.skjolber.unzip.ZipFileSource;


/**
 * GTFS feed content.
 *
 */

public class GtfsFeedBuilder {

	public static GtfsFeedBuilder newInstance() {
		return new GtfsFeedBuilder();
	}
	
	private LocalDate startDate;
	private LocalDate endDate;

	private int chunkLength = 32 * 1024 * 1024;

	private ThreadPoolExecutor executor;
	private boolean shutdownExecutor = false;
	
	private Double colocationLimit; // radians
	
	private ZipFileSource source;
	
	public GtfsFeedBuilder withStartDate(LocalDate startDate) {
		this.startDate = startDate;
		return this;
	}

	public GtfsFeedBuilder withExecutor(ThreadPoolExecutor executor) {
		this.executor = executor;
		return this;
	}

	public GtfsFeedBuilder withEndDate(LocalDate endDate) {
		this.endDate = endDate;
		return this;
	}
	
	public GtfsFeedBuilder withChunkLength(int length) {
		this.chunkLength = length;
		return this;
	}

	public GtfsFeedBuilder withThreads(int threads) {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
		shutdownExecutor = true;
		
		return this;
	}

	public GtfsFeedBuilder withColocationLimit(double radians) {
		this.colocationLimit = radians;
		return this;
	}

	public GtfsFeedBuilder withFile(String path) throws IOException {
		return withFile(new File(path));
	}

	public GtfsFeedBuilder withUrl(URL url, int segmentLength) throws IOException {
		this.source = new UrlZipFileFactory(url, segmentLength);
		return this;
	}

	public GtfsFeedBuilder withFile(File file) throws IOException {
		this.source = new FileZipFileFactory(file);
		return this;
	}
	
	public GtfsFeed build() throws IOException {
		if(source == null) {
			throw new IllegalArgumentException("No input file or URL");
		}
		if(executor == null) {
			withThreads(Runtime.getRuntime().availableProcessors());
		}

		AbstractGtfsChunkedCsvFileEntryHandler gtfsCsvFileEntryHandler;
		
		if(endDate != null || startDate != null) {
			gtfsCsvFileEntryHandler = new FilterGtfsChunkedCsvFileEntryHandler(chunkLength, colocationLimit, startDate, endDate);
		} else {
			gtfsCsvFileEntryHandler = new GtfsChunkedCsvFileEntryHandler(chunkLength, colocationLimit);
		}

		ChunkSplitterFileEntryHandler wrapper = new ChunkSplitterFileEntryHandler(chunkLength, gtfsCsvFileEntryHandler);
		
		ZipFileEngine engine = new ZipFileEngine(wrapper, executor);

		List<String> files = new ArrayList<>();
		files.add("agency.txt");
		files.add("routes.txt");
		files.add("stops.txt");
		files.add("trips.txt");
		files.add("calendar.txt");
		files.add("calendar_dates.txt");
		files.add("transfers.txt");

		if(endDate == null && startDate == null) {
			files.add("stop_times.txt");
		}

		files.add("shapes.txt");

		boolean success = false;
		if(engine.handle(source, Arrays.asList("feed_info.txt"))) {
			if(engine.handle(source, files)) {
				if(endDate != null || startDate != null) {
					if(engine.handle(source, Arrays.asList("stop_times.txt"))) {
						success = true;
					}
				} else {
					success = true;
				}
			}
		}
		
		if(shutdownExecutor) {
			engine.close();
		}
		
		if(success) {
			return gtfsCsvFileEntryHandler.getHandlerCache();
		}
		return null;
	}

}
