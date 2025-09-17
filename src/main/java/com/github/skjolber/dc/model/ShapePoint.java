package com.github.skjolber.dc.model;

public class ShapePoint implements Comparable<ShapePoint> {

    private String shapeId;

    private int sequence;

    private double lat;

    private double lon;

    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }


    @Override
    public int compareTo(ShapePoint o) {
        return this.getSequence() - o.getSequence();
    }
}
