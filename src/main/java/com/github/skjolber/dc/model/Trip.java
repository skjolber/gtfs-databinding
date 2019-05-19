package com.github.skjolber.dc.model;

import java.util.List;

public class Trip {

	private Route route;
	
	private String id;
	
    private Service service;
	
	private List<StopTime> stopTimes;
	
    private String shortName;

    private String headsign;

    private String directionId;

    private FeedId shapeId;

    private int wheelchairAccessible = 0;

    /**
     * 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes NOT allowed
     */
    private int bikesAllowed = 0;
	
	public long getDuration() {
		StopTime first = stopTimes.get(0);
		StopTime last = stopTimes.get(stopTimes.size() - 1);
		
		return last.getArrivalTime() - first.getDepartureTime();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<StopTime> getStopTimes() {
		return stopTimes;
	}

	public void setStopTimes(List<StopTime> stopTimes) {
		this.stopTimes = stopTimes;
	}
	
	public long getStartTime() {
		return stopTimes.get(0).getDepartureTime();
	}
	
	public long getEndTime() {
		return stopTimes.get(stopTimes.size() - 1).getArrivalTime();
	}
	
	public void setRoute(Route route) {
		this.route = route;
	}
	
	public Route getRoute() {
		return route;
	}

	public void add(StopTime entity) {
		if(!stopTimes.isEmpty()) {
			if(entity.getStopSequence() - 1 != stopTimes.get(stopTimes.size() - 1).getStopSequence()) {
				throw new RuntimeException("Attempting to add " + entity.getStopSequence()  + " when expecting " + stopTimes.get(stopTimes.size() - 1).getStopSequence());
			}
		}
		
		this.stopTimes.add(entity);
	}

	public void setService(Service service) {
		this.service = service;
	}
	
	public Service getService() {
		return service;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getHeadsign() {
		return headsign;
	}

	public void setHeadsign(String headsign) {
		this.headsign = headsign;
	}

	public String getDirectionId() {
		return directionId;
	}

	public void setDirectionId(String directionId) {
		this.directionId = directionId;
	}

	public FeedId getShapeId() {
		return shapeId;
	}

	public void setShapeId(FeedId shapeId) {
		this.shapeId = shapeId;
	}

	public int getWheelchairAccessible() {
		return wheelchairAccessible;
	}

	public void setWheelchairAccessible(int wheelchairAccessible) {
		this.wheelchairAccessible = wheelchairAccessible;
	}

	public int getBikesAllowed() {
		return bikesAllowed;
	}

	public void setBikesAllowed(int bikesAllowed) {
		this.bikesAllowed = bikesAllowed;
	}

	
}
