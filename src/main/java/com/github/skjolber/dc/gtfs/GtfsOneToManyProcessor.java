package com.github.skjolber.dc.gtfs;

import com.github.skjolber.dc.GtfsFeed;

public class GtfsOneToManyProcessor<T> extends OneToManyProcessor<T> {

	protected GtfsFeed feed;
	
	public GtfsOneToManyProcessor(GtfsFeed feed) {
		this.feed = feed;
	}

	public GtfsFeed getFeed() {
		return feed;
	}
	
}
