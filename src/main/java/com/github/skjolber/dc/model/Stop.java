package com.github.skjolber.dc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Stop {

    private static final int MISSING_VALUE = -999;

	private String id;
	
	private String name;
	
    private double lat;

    private double lon;

    private String code;

    private String desc;

    private String zoneId;

    private String url;

    private int locationType = 0;
    
    private int wheelchairBoarding = 0;

    private String direction;

    private String timezone;

    private int vehicleType = MISSING_VALUE;

    private String platformCode;
		
	private Stop parentStation;

	private Stop rootParentStation;

	private List<StopTime> stopTimes = new ArrayList<>();
	
	private Set<Stop> children;
	
	public Set<Stop> getChildren() {
		return children;
	}
	
	public boolean isChildren() {
		return children != null && !children.isEmpty();
	}
	
	public void setChildren(Set<Stop> children) {
		this.children = children;
	}
	
	public void addChild(Stop stop) {
		if(children == null) {
			children = new HashSet<>();
		}
		children.add(stop);
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Stop getParentStation() {
		return parentStation;
	}
	
	public void setParentStation(Stop parentStation) {
		this.parentStation = parentStation;
	}

	public boolean isParentStation() {
		return parentStation != null;
	}

	@Override
	public String toString() {
		return "Stop [id=" + id + ", name=" + name + ", parentStation=" + parentStation + "]";
	}
	
	public Stop top() {
		Stop toStop = this;
		while(toStop.isParentStation()) {
			toStop = toStop.getParentStation();
		}
		return toStop;
	}


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLocationType() {
        return locationType;
    }

    public void setLocationType(int locationType) {
        this.locationType = locationType;
    }

    public void setWheelchairBoarding(int wheelchairBoarding) {
        this.wheelchairBoarding = wheelchairBoarding;
    }

    public int getWheelchairBoarding() {
        return wheelchairBoarding;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean isVehicleTypeSet() {
        return vehicleType != MISSING_VALUE;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(int vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void clearVehicleType() {
        vehicleType = MISSING_VALUE;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }
    
    public List<StopTime> getStopTimes() {
		return stopTimes;
	}
    
    public int index(StopTime stopTime) {
		return Collections.binarySearch(stopTimes, stopTime, StopTime.arrivalTimeComparator);
    }
    
    public void setStopTimes(List<StopTime> stopTimes) {
		this.stopTimes = stopTimes;
	}
    
    public void add(StopTime stopTime) {
    	this.stopTimes.add(stopTime);
    }
    
    public List<StopTime> getStopTimes(int start, int end) {
    	List<StopTime> result = new ArrayList<>();
	
    	for (StopTime stopTime : stopTimes) {
			if(start <= stopTime.getArrivalTime() && stopTime.getArrivalTime() <= end) {
				result.add(stopTime);
			} else if(end <= stopTime.getDepartureTime() && stopTime.getDepartureTime() <= end) {
				result.add(stopTime);
			}
		}
    	
    	return result;
    }

	public boolean hasStopTimes() {
		return !stopTimes.isEmpty();
	}

	public boolean hasParentStation() {
		return parentStation != null;
	}
	
	public void setRootParentStation(Stop rootParentStation) {
		this.rootParentStation = rootParentStation;
	}
	
	public Stop getRootParentStation() {
		return rootParentStation;
	}

	public boolean hasRootParentStation() {
		return rootParentStation != null;
	}
}
