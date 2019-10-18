package com.github.mob41.gotowhere.transitdb.db;

import java.util.HashMap;
import java.util.Map;

public class TransitRoute {

	private final String routeId;
	
	private final String[][] paths;
	
	private final TransitType transitType;
	
	private final String providerName;
	
	public TransitRoute(TransitType transitType, String providerName, String routeId, TransitStop[][] pathStops) {
		this.routeId = routeId;
		String[][] paths = new String[pathStops.length][];
		for (int i = 0; i < pathStops.length; i++) {
			for (int x = 0; x < pathStops.length; x++) {
				paths[i][x] = pathStops[i][x].getStopId();
			}
		}
		this.paths = paths;
		this.transitType = transitType;
		this.providerName = providerName;
	}

	public TransitRoute(TransitType transitType, String providerName, String routeId, String[][] paths) {
		this.routeId = routeId;
		this.paths = paths;
		this.transitType = transitType;
		this.providerName = providerName;
	}
	
	public String getRouteId() {
		return routeId;
	}

	public String[][] getPaths() {
		return paths;
	}

	public TransitType getTransitType() {
		return transitType;
	}

	public String getProviderName() {
		return providerName;
	}

	public Map<String, Object> getMap(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("transit", transitType.toString());
		map.put("provider", providerName);
		map.put("routeId", routeId);
		map.put("paths", paths);
		return map;
	}
	
}
