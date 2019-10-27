package com.github.mob41.gotowhere.transitdb.db;

import java.util.HashMap;
import java.util.Map;

public class TransitRoute {

	private final String routeId;
	
	private final RouteNameKey routeName;
	
	private final String[][] paths;
	
	private final String[] etaProviders;
	
	private final TransitType transitType;
	
	private final String providerName;
	
	public TransitRoute(TransitType transitType, String providerName, String routeId, RouteNameKey routeName, TransitStop[][] pathStops, String[] etaProviders) {
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
		this.routeName = routeName;
		this.etaProviders = etaProviders;
	}

	public TransitRoute(TransitType transitType, String providerName, String routeId, RouteNameKey routeName, String[][] paths, String[] etaProviders) {
		this.routeId = routeId;
		this.paths = paths;
		this.transitType = transitType;
		this.providerName = providerName;
		this.routeName = routeName;
		this.etaProviders = etaProviders;
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
		map.put("type", transitType.toString());
		map.put("provider", providerName);
		map.put("routeId", routeId);
		map.put("etaProviders", etaProviders);
		map.put("paths", paths);
		map.putAll(routeName.getMap());
		return map;
	}
	
}
