package com.github.skjolber.dc.model;

public class Transfer {

	private Stop from;
	private Stop to;
	
	private int type;
	
	private int minTime = -1;

	public Stop getFrom() {
		return from;
	}

	public void setFrom(Stop from) {
		this.from = from;
	}

	public Stop getTo() {
		return to;
	}

	public void setTo(Stop to) {
		this.to = to;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getMinTime() {
		return minTime;
	}

	public void setMinTime(int minTime) {
		this.minTime = minTime;
	}
	
	
}
