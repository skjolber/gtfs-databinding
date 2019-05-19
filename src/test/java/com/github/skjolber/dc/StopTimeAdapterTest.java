package com.github.skjolber.dc;

import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import com.github.skjolber.dc.gtfs.GtfsIntermediateProcessor;
import com.github.skjolber.dc.gtfs.IntermediateProcessor;
import com.github.skjolber.dc.gtfs.mt.StopTimeAdapter;
import com.github.skjolber.dc.model.StopTime;
import com.github.skjolber.stcsv.CsvMapper2;
import com.github.skjolber.stcsv.CsvReader;

public class StopTimeAdapterTest {

	@Test
	public void testParse() throws Exception {
		GtfsFeed feed = new GtfsFeed();
		CsvMapper2<StopTime, IntermediateProcessor<StopTime>> parser = StopTimeAdapter.getParser();
		InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/stop_times1.txt"));
		CsvReader<StopTime> r = parser.create(reader, new GtfsIntermediateProcessor<StopTime>(2, feed));
		
		int count = 2;
		StopTime previous = r.next();
		do {
			StopTime next = r.next();
			if(next == null) {
				break;
			}
			
			if(next.getStopSequence() < previous.getStopSequence()) {
				throw new RuntimeException(next.getStopSequence() + " < + " + previous.getStopSequence() + " at line " + count);
			}
			
			count++;
		} while(true);
	}
	
}
