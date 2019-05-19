package com.github.skjolber.dc.gtfs.st;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.skjolber.dc.GtfsFeed;
import com.github.skjolber.dc.gtfs.GtfsOneToManyProcessor;
import com.github.skjolber.dc.model.Stop;
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
public class StopAdapter extends GtfsOneToManyProcessor<Stop> implements CsvLineHandlerFactory, CsvLineHandler<Stop> {

	protected static CsvMapper2<Stop, GtfsOneToManyProcessor<Stop>> parser;

	static {
		// stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station,wheelchair_boarding,stop_timezone,platform_code,vehicle_type
		parser = CsvMapper2.builder(Stop.class, (Class<GtfsOneToManyProcessor<Stop>>)(Class<?>)GtfsOneToManyProcessor.class)
			.stringField("stop_id")
				.setter(Stop::setId)
				.required()
			.stringField("stop_code")
				.setter(Stop::setCode)
				.optional()
			.stringField("stop_name")
				.setter(Stop::setName)
				.quoted()
				.required()
			.stringField("stop_desc")
				.setter(Stop::setDesc)
				.quoted()
				.optional()
			.doubleField("stop_lat")
			    .consumer( (s, lat) -> s.setLat(Math.toRadians(lat)))
				.required()
			.doubleField("stop_lon")
				.consumer( (s, lon) -> s.setLon(Math.toRadians(lon)))
				.required()
			.stringField("zone_id")
				.setter(Stop::setZoneId)
				.optional()
			.stringField("stop_url")
				.setter(Stop::setUrl)
				.optional()
			.integerField("location_type")
				.setter(Stop::setLocationType)
				.optional()
			.stringField("parent_station")
				.consumer( (t, i, id) -> i.add(id,  t))
				.optional()
			.integerField("wheelchair_boarding")
				.setter(Stop::setWheelchairBoarding)
				.optional()
			.stringField("stop_timezone")
				.setter(Stop::setTimezone)
				.optional()
			.stringField("direction")
				.setter(Stop::setDirection)
				.optional()
			.stringField("platform_code")
				.setter(Stop::setPlatformCode)
				.optional()
			.integerField("vehicle_type")
				.setter(Stop::setVehicleType)
				.optional()
			.build();
	}

	protected List<Stop> stops = new ArrayList<>(4 * 1024);
	protected List<Stop> parentStops = new ArrayList<>(4 * 1024);
	
	public StopAdapter(GtfsFeed feed) {
		super(feed);
	}

	@Override
	public void handleLine(Stop value) {
		stops.add(value);
	}

	@Override
	public <T> CsvLineHandler<T> getHandler(String fileName, ThreadPoolExecutor executor) {
		if(!fileName.equals("stops.txt")) {
			throw new IllegalArgumentException();
		}
		return (CsvLineHandler<T>)this;
	}
	
	private class RouteCsvFileEntryStreamHandler extends AbstractSesselTjonnaCsvFileEntryStreamHandler<Stop> {

		public RouteCsvFileEntryStreamHandler(String name, CsvLineHandlerFactory csvLineHandlerFactory, long size, FileEntryHandler delegate, ThreadPoolExecutor executor) {
			super(name, csvLineHandlerFactory, delegate, executor);
		}

		@Override
		protected CsvReader<Stop> createCsvReader(Reader reader, ThreadPoolExecutor executorService) throws Exception {
			return parser.create(reader, StopAdapter.this);
		}
	}

	public FileEntryStreamHandler getFileEntryStreamHandler(FileEntryHandler delegate, ThreadPoolExecutor executor, long size) throws Exception {
		return new RouteCsvFileEntryStreamHandler("stops.txt", this, size, delegate, executor);
	}

	public FileEntryChunkStreamHandler getFileEntryChunkedStreamHandler() throws Exception {
		return null;
	}
	
	public void resolveParents() {
		for (Entry<String, List<Stop>> entry : entityById.entrySet()) {
			Stop parent = feed.getStop(entry.getKey());
			if(parent == null) {
				throw new IllegalArgumentException("Unknown parent station " + entry.getKey());
			}
			for(Stop stop : entry.getValue()) {
				stop.setParentStation(parent);
				
				parent.addChild(stop);
				
				stop.setRootParentStation(parent);
			}
		}
	}
	
	public void resolveColocations(double coLat, double coLon) {
		
		List<Stop> parents = new ArrayList<Stop>(stops.size());
		Map<String, Stop> parentMap = new HashMap<>(stops.size());
		for(Stop stop : stops) {
			if(stop.hasParentStation()) {
				Stop parentStation = stop.getParentStation();
				parentMap.put(parentStation.getId(), parentStation);
			}
		}
		
		Map<String, List<Stop>> map = new HashMap<>(parents.size() * 2);
		List<Stop> matches = new ArrayList<Stop>();
		for (int i = 0; i < parents.size(); i++) {
			Stop s = parents.get(i);

			for (int k = 0; k < parents.size(); k++) {
				if(i == k) {
					continue;
				}
				
				Stop candidate = parents.get(k);
				if(isColocated(s.getLat(), s.getLon(), candidate.getLat(), candidate.getLon(), coLat, coLon)) {
					matches.add(candidate);
				}
			}
			
			if(!matches.isEmpty()) {
				map.put(s.getId(), matches);
				
				matches = new ArrayList<Stop>();
			}
		}
		
		Set<String> set = new HashSet<>(map.keySet());
		for (String string : set) {
			if(map.containsKey(string)) {
				List<Stop> cluster = new ArrayList<>();
				cluster.add(feed.getStop(string));
				
				for(int i = 0; i < cluster.size(); i++) {
					List<Stop> list = map.remove(cluster.get(i).getId());
					if(list != null) {
						for(Stop stop : list) {
							if(!cluster.contains(stop)) {
								cluster.add(stop);
							}
						}
					}
				}

				Stop clusterParent = new Stop();

				Set<String> names = new HashSet<>();
				double lat = 0;
				double lon = 0;
				for(Stop stop : cluster) {
					names.add(stop.getName());
					
					lat += stop.getLat();
					lon += stop.getLon();
					
					clusterParent.addChild(stop);
					stop.setParentStation(clusterParent);
				}
				
				String name;
				if(names.size() == 1) {
					name = names.iterator().next();
				} else {
					name = names.toString();
				}
				
				clusterParent.setName(name);
				clusterParent.setId("cluster-" + parentStops.size());
				
				// average position
				clusterParent.setLat(lat / names.size());
				clusterParent.setLon(lon / names.size());
				
				clusterParent.setRootParentStation(clusterParent);
				
				parentStops.add(clusterParent);
			}
		}
		
		for(Stop stop : stops) {
			Stop s = stop; 
			while(s.hasParentStation()) {
				s = s.getParentStation();
			}
			stop.setRootParentStation(s);
		}
	}	
	
	public List<Stop> getStops() {
		return stops;
	}

	public List<Stop> getParentStops() {
		return parentStops;
	}

	public boolean isColocated(double lat1, double lon1, double lat2, double lon2, double coLat, double coLon) {
		return Math.abs(lat1 - lat2) < coLat && Math.abs(lon1 - lon2) < coLon;
	}
}
