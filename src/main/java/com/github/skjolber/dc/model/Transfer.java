package com.github.skjolber.dc.model;

public class Transfer {

	private Stop fromStop;
	private Stop toStop;
	
	private int type;

	private Trip fromTrip;
	private Trip toTrip;

	public Stop getFromStop() {
		return fromStop;
	}

	public void setFromStop(Stop fromStop) {
		this.fromStop = fromStop;
	}

	public Stop getToStop() {
		return toStop;
	}

	public void setToStop(Stop toStop) {
		this.toStop = toStop;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Trip getFromTrip() {
		return fromTrip;
	}

	public void setFromTrip(Trip fromTrip) {
		this.fromTrip = fromTrip;
	}

	public Trip getToTrip() {
		return toTrip;
	}

	public void setToTrip(Trip toTrip) {
		this.toTrip = toTrip;
	}
}
