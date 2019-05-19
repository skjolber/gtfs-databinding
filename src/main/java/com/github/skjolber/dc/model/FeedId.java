package com.github.skjolber.dc.model;

public class FeedId {

    private String agencyId;

    private String id;

    public FeedId() {
    }

    public FeedId(String agencyId, String id) {
        this.agencyId = agencyId;
        this.id = id;
    }

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
