package com.github.skjolber.dc.gtfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper-class for storing a single reference key. Not thread safe.
 *
 * @param <T> entity type
 */

public class OneToManyProcessor<T> {

	protected Map<String, List<T>> entityById = new HashMap<>(4 * 4096);
	
	public OneToManyProcessor() {
	}

	public Map<String, List<T>> getById() {
		return entityById;
	}
	
	public void add(String id, T trip) {
		List<T> byId = entityById.get(id);
		if(byId == null) {
			byId = new ArrayList<>(8);
			entityById.put(id, byId);
		}
		byId.add(trip);
	}

}
