package com.github.skjolber.dc.gtfs;

import java.util.List;
import java.util.Map;

/**
 * Helper-class for storing multiple reference keys.
 *
 * @param <T> entity type
 */

public class IntermediateProcessor<T> {

	protected OneToManyProcessor<T>[] mappings;
	
	@SuppressWarnings("unchecked")
	public IntermediateProcessor(int count) {
		mappings = new OneToManyProcessor[count];
		for(int i = 0; i < count; i++) {
			mappings[i] = new OneToManyProcessor<>();
		}
	}
	
	public void add(int index, String key, T object) {
		mappings[index].add(key, object);
	}
	
	public Map<String, List<T>> getById(int index) {
		return mappings[index].getById();
	}
	
}
