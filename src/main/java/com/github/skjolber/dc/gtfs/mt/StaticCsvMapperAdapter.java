package com.github.skjolber.dc.gtfs.mt;

import java.io.Reader;

import com.github.skjolber.stcsv.CsvReader;
import com.github.skjolber.stcsv.StaticCsvMapper;
import com.github.skjolber.stcsv.StaticCsvMapper2;

public abstract class StaticCsvMapperAdapter<T, D> implements StaticCsvMapper<T> {

	protected StaticCsvMapper2<T, D> staticCsvMapper2;
	
	public StaticCsvMapperAdapter(StaticCsvMapper2<T, D> staticCsvMapper2) {
		this.staticCsvMapper2 = staticCsvMapper2;
	}

	@Override
	public CsvReader<T> newInstance(Reader reader) {
		return staticCsvMapper2.newInstance(reader, newIntermediateProcessor());
	}

	@Override
	public CsvReader<T> newInstance(Reader reader, char[] current, int offset, int length) {
		return staticCsvMapper2.newInstance(reader, current, offset, length, newIntermediateProcessor());
	}

	protected abstract D newIntermediateProcessor();
}