package com.github.skjolber.dc.model;

public class Route {

	private static final int MISSING_VALUE = -999;
	
	private String id;

	private String shortName;
	
	private Agency agency;

    private int type;
    
    private String longName;

    private String desc;

    private String url;

    private int color;

    private int textColor;

    private int sortOrder = MISSING_VALUE;

    private String brandingUrl;    

    /**
     * 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes NOT allowed
     */
    private int bikesAllowed = 0;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setAgency(Agency agency) {
		this.agency = agency;
	}
	
	public Agency getAgency() {
		return agency;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getShortName() {
		return shortName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getBikesAllowed() {
		return bikesAllowed;
	}

	public void setBikesAllowed(int bikesAllowed) {
		this.bikesAllowed = bikesAllowed;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getBrandingUrl() {
		return brandingUrl;
	}

	public void setBrandingUrl(String brandingUrl) {
		this.brandingUrl = brandingUrl;
	}
	
	
}
