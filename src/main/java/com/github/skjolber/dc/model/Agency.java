package com.github.skjolber.dc.model;

public class Agency {

    private String id;
    
    private String name;
    
    private String url;

    private String timezone;

    private String lang;

    private String phone;

    private String fareUrl;

    private String email;
    
    public void setId(String id) {
		this.id = id;
	}
    
    public String getId() {
		return id;
	}
    
    public void setName(String name) {
		this.name = name;
	}
    
    public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFareUrl() {
		return fareUrl;
	}

	public void setFareUrl(String fareUrl) {
		this.fareUrl = fareUrl;
	}

	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
    
}
