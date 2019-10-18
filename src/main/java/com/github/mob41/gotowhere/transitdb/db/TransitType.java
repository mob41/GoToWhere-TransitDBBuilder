package com.github.mob41.gotowhere.transitdb.db;

public enum TransitType {

	TRANSIT_BUS("TRANSIT_BUS"), TRANSIT_METRO("TRANSIT_METRO"), TRANSIT_TRAIN("TRANSIT_TRAIN"), TRANSIT_FERRY("TRANSIT_FERRY");
	
	private final String val;
	
	public String toString() {
		return val;
	}
	
	private TransitType(String val) {
		this.val = val;
	}
	
}
