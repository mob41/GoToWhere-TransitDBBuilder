package com.github.mob41.gotowhere.transitdb.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class TransitDatabase {
	
	private final TransitType transitType;
	
	private final String providerName;
	
	private final String version;

	private final long generated;
	
	private final TransitRoute[] routes;
	
	private final TransitStop[] stops;

	public TransitDatabase(TransitType transitType, String providerName, String version, long generated, TransitRoute[] routes,
			TransitStop[] stops) {
		this.transitType = transitType;
		this.providerName = providerName;
		this.version = version;
		this.generated = generated;
		this.routes = routes;
		this.stops = stops;
	}

	public TransitType getTransitType() {
		return transitType;
	}

	public String getProviderName() {
		return providerName;
	}
	
	public String getVersion() {
		return version;
	}

	public long getGenerated() {
		return generated;
	}

	public TransitRoute[] getRoutes() {
		return routes;
	}

	public TransitStop[] getStops() {
		return stops;
	}
	
	public Map<String, Object> getDatabaseMap(){
		List<Map<String, Object>> routeMaps = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < routes.length; i++) {
			routeMaps.add(routes[i].getMap());
		}
		
		List<Map<String, Object>> stopMaps = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < stops.length; i++) {
			stopMaps.add(stops[i].getMap());
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", transitType.toString());
		map.put("provider", providerName);
		map.put("generated", generated);
		map.put("version", version);
		map.put("routes", routeMaps);
		map.put("stops", stopMaps);
		return map;
	}
	
	public Map<String, Object> getVersionMap(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("transit", transitType.toString());
		map.put("provider", providerName);
		map.put("generated", generated);
		map.put("version", version);
		return map;
	}
	
	public String getDatabaseJson() {
		Gson gson = new Gson();
		Map<String, Object> map = getDatabaseMap();
		return gson.toJson(map);
	}
	
	public String getVersionJson() {
		Gson gson = new Gson();
		Map<String, Object> map = getVersionMap();
		return gson.toJson(map);
	}
	
}
