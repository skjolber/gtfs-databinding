package com.github.skjolber.dc.model;

public class FeedInfo {

    private String id;

    private String publisherName;

    private String publisherUrl;

    private String lang;

    private String version;

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherUrl() {
        return publisherUrl;
    }

    public void setPublisherUrl(String publisherUrl) {
        this.publisherUrl = publisherUrl;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
		return id;
	}
    
    public void setId(String id) {
		this.id = id;
	}

}
