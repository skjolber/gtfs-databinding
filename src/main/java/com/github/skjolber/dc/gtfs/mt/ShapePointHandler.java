package com.github.skjolber.dc.gtfs.mt;

import com.github.skjolber.dc.model.ShapePoint;
import com.github.skjolber.unzip.csv.CsvLineHandler;

import java.util.ArrayList;
import java.util.List;

public class ShapePointHandler implements CsvLineHandler<ShapePoint> {

	private List<ShapePoint> shapePoints = new ArrayList<>(1024);
	
	@Override
	public void handleLine(ShapePoint shapePoint) {
		shapePoints.add(shapePoint);
	}
	
	public List<ShapePoint> getShapePoints() {
		return shapePoints;
	}
}
