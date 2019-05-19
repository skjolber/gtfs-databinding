package com.github.skjolber.dc.gtfs;

import com.github.skjolber.dc.GtfsFeed;

public class GtfsIntermediateProcessor<T> extends IntermediateProcessor<T> {

	protected GtfsFeed feed;
	
	public GtfsIntermediateProcessor(int count, GtfsFeed feed) {
		super(count);
		
		this.feed = feed;
	}

	public GtfsFeed getFeed() {
		return feed;
	}

}
