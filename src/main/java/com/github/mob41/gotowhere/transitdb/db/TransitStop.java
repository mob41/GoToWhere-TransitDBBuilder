package com.github.mob41.gotowhere.transitdb.db;

import java.util.HashMap;
import java.util.Map;

public class TransitStop{

	private final String stopId;

	private final StopNameKey stopName;

	private final AddressKey addr;

	private final double lat;
	
	private final double lng;
	
	private final TransitType transitType;
	
	private final String providerName;

	public TransitStop(TransitType transitType, String providerName, String stopId, StopNameKey stopName, AddressKey addr, double lat, double lng) {
		this.stopId = stopId;
		this.stopName = stopName;
		this.addr = addr;
		this.lat = lat;
		this.lng = lng;
		this.transitType = transitType;
		this.providerName = providerName;
	}

	public String getStopId() {
		return stopId;
	}

	public StopNameKey getStopName() {
		return stopName;
	}

	public AddressKey getAddr() {
		return addr;
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}
	
	public Map<String, Object> getMap(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("transit", transitType.toString());
		map.put("provider", providerName);
		map.put("stopId", stopId);
		map.putAll(stopName.getMap());
		map.putAll(addr.getMap());
		map.put("lat", lat);
		map.put("lng", lng);
		return map;
	}
	
}
