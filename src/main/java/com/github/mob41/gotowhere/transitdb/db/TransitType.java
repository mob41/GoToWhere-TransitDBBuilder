package com.github.mob41.gotowhere.transitdb.db;

public enum TransitType {

	BUS("bus"), MINIBUS("minibus"), TRAIN("train"), TRAM("tram"), FERRY("ferry");
	
	private final String val;
	
	public String toString() {
		return val;
	}
	
	private TransitType(String val) {
		this.val = val;
	}
	
}
