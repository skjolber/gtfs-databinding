package com.github.skjolber.dc.gtfs.mt;

import java.util.ArrayList;
import java.util.List;

import com.github.skjolber.dc.model.StopTime;
import com.github.skjolber.unzip.csv.CsvLineHandler;

public class StopTimeHandler implements CsvLineHandler<StopTime> {

	protected List<List<StopTime>> stopTimes = new ArrayList<>(1024);

	protected ArrayList<StopTime> tripStopTimes = new ArrayList<>(32);
	
	protected int previous = -1;
	
	public StopTimeHandler() {
		stopTimes.add(tripStopTimes);
	}

	public List<List<StopTime>> getStopTimes() {
		return stopTimes;
	}
	
	@Override
	public void handleLine(StopTime value) {
		if(value.getStopSequence() < previous) {
			tripStopTimes.trimToSize();
			
			tripStopTimes = new ArrayList<StopTime>(32);
			stopTimes.add(tripStopTimes);
		} else {
			if(!tripStopTimes.isEmpty()) {
				StopTime previous = tripStopTimes.get(tripStopTimes.size() - 1);
				value.setPrevious(previous);
				previous.setNext(value);
			}
		}
		
		tripStopTimes.add(value);

		previous = value.getStopSequence();
	}
	
}
