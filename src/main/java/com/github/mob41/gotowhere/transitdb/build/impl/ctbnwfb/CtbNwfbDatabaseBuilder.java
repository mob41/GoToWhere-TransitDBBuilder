package com.github.mob41.gotowhere.transitdb.build.impl.ctbnwfb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.github.mob41.gotowhere.transitdb.Console;
import com.github.mob41.gotowhere.transitdb.build.TransitDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.db.AddressKey;
import com.github.mob41.gotowhere.transitdb.db.StopNameKey;
import com.github.mob41.gotowhere.transitdb.db.TransitRoute;
import com.github.mob41.gotowhere.transitdb.db.TransitStop;
import com.github.mob41.gotowhere.transitdb.db.TransitType;
import com.google.gson.Gson;

public class CtbNwfbDatabaseBuilder extends TransitDatabaseBuilder {
	
	private static final String ROUTES_URL = "https://rt.data.gov.hk/v1/transport/citybus-nwfb/route/";

	private static final String ROUTE_STOP_URL = "https://rt.data.gov.hk/v1/transport/citybus-nwfb/route-stop/";
	
	private static final String STOP_URL = "https://rt.data.gov.hk/v1/transport/citybus-nwfb/stop/";
	
	private static final String[] COMPANIES = {"CTB", "NWFB"};
	
	private static final String[] BOUNDS = {"outbound", "inbound"};

	private final Gson gson;
	
	public CtbNwfbDatabaseBuilder() {
		super(TransitType.TRANSIT_BUS, "CTBNWFB");
		gson = new Gson();
	}

	@Override
	public boolean build() throws Exception {
		URL url;
		URLConnection conn;
		BufferedReader reader;
		String bound;
		CtbNwfbRoute route;
		CtbNwfbRoute[] routes;
		CtbNwfbRouteResponse routeResp;
		CtbNwfbRouteStop[] routeStops;
		CtbNwfbRouteStopResponse routeStopResp;
		String[] path;
		String[][] paths;
		List<CtbNwfbRouteStop[]> bounds;
		List<String> stopIds = new ArrayList<String>();
		String company;
		int reqCount = 0;
		FileOutputStream fileOut;
		PrintWriter fileWriter;
		FileInputStream fileIn;
		InputStream in;
		String fileKey;
		
		for (int x = 0; x < COMPANIES.length; x++) {
			company = COMPANIES[x];
			reportMessage("Downloading " + company + " routes...");
		    
		    fileKey = company + "-route-response.json";
		    fileIn = readDownloaded(fileKey);
		    if (fileIn == null) {
				reqCount++;
				url = new URL(ROUTES_URL + company);
			    conn = url.openConnection();
		    	in = conn.getInputStream();
		    } else {
		    	in = fileIn;
		    }

	    	reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		    routeResp = gson.fromJson(reader, CtbNwfbRouteResponse.class);
		    reader.close();
		    
		    routes = routeResp.data;
		    
		    if (routes.length == 0) {
				reportMessage("Error!");
		    	Console.println("Error: Empty " + company + " route list data array! Aborting");
		    	return false;
		    }
		    
		    if (fileIn == null) {
			    fileOut = writeDownloaded(fileKey);
				fileWriter = new PrintWriter(new OutputStreamWriter(fileOut, StandardCharsets.UTF_8));
				fileWriter.println(gson.toJson(routeResp));
				fileWriter.flush();
				fileWriter.close();
				fileOut.close();
		    }
		    
		    for (int i = 0; i < routes.length; i++) {
		    	route = routes[i];
		    	bounds = new ArrayList<CtbNwfbRouteStop[]>();
		    	for (int j = 0; j < BOUNDS.length; j++) {
		    		bound = BOUNDS[j];
		    		
					reportMessage(company + ": " + (i + 1) + "/" + routes.length + ": " + route.route + " " + bound);
				    
				    fileKey = company + "-" + route.route + "-" + j +  "-route-stop-response.json";
				    fileIn = readDownloaded(fileKey);
				    if (fileIn == null) {
			    		reqCount++;
			    		url = new URL(ROUTE_STOP_URL + company + "/" + route.route + "/" + bound);
					    conn = url.openConnection();
					    in = conn.getInputStream();
				    } else {
				    	in = fileIn;
				    }

			    	reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
				    routeStopResp = gson.fromJson(reader, CtbNwfbRouteStopResponse.class);
				    reader.close();
				    
				    if (routeStopResp.data.length == 0) {
				    	if (j == 0) {
				    		Console.println("Warning: No bound data received for " + route.route + "!");
				    		break;
				    	}
				    	continue;
				    }
				    
				    if (fileIn == null) {
					    fileOut = writeDownloaded(fileKey);
						fileWriter = new PrintWriter(new OutputStreamWriter(fileOut, StandardCharsets.UTF_8));
						fileWriter.println(gson.toJson(routeStopResp));
						fileWriter.flush();
						fileWriter.close();
						fileOut.close();
				    }
				    
				    bounds.add(routeStopResp.data);
		    	}
		    	
		    	paths = new String[bounds.size()][];
		    	for (int j = 0; j < bounds.size(); j++) {
		    		routeStops = bounds.get(j);
		    		path = new String[routeStops.length];
		    		for (int k = 0; k < routeStops.length; k++) {
		    			path[j] = routeStops[k].stop;
		    			if (!stopIds.contains(routeStops[k].stop)) {
		    				stopIds.add(routeStops[k].stop);
		    			}
		    		}
		    		paths[j] = path;
		    	}
		    	
		    	addRoute(new TransitRoute(
		    			TransitType.TRANSIT_BUS,
		    			company,
		    			route.route,
		    			paths));
		    	reportProgress((int) ((i + 1) / (float) routes.length * 100.0 / 3.0 + (x * 100.0 / 3.0)));
		    }
		}
		
		String stopId;
		CtbNwfbStopResponse stopResp;
		CtbNwfbStop stop;
		StopNameKey stopNameKey;
		AddressKey addrKey;
		for (int i = 0; i < stopIds.size(); i++) {
			stopId = stopIds.get(i);
			reportMessage((i + 1) + "/" + stopIds.size() + ": Stop " + stopId);
		    
		    fileKey = "stop-" + stopId + "-response.json";
		    fileIn = readDownloaded(fileKey);
		    if (fileIn == null) {
	    		reqCount++;
	    		url = new URL(STOP_URL + stopId);
			    conn = url.openConnection();
			    in = conn.getInputStream();
		    } else {
		    	in = fileIn;
		    }

	    	reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		    stopResp = gson.fromJson(reader, CtbNwfbStopResponse.class);
		    reader.close();
		    
		    stop = stopResp.data;
		    
		    if (stop.stop == null || 
		    		stop.name_tc == null || 
		    		stop.name_en == null || 
		    		stop.lat == null || 
		    		stop.lng == null || 
		    		stop.name_sc == null || 
		    		stop.data_timestamp == null) {
		    	Console.println("Warning: Could not fetch stop " + stopId + " data!");
		    	continue;
		    }
		    
		    if (fileIn == null) {
			    fileOut = writeDownloaded(fileKey);
				fileWriter = new PrintWriter(new OutputStreamWriter(fileOut, StandardCharsets.UTF_8));
				fileWriter.println(gson.toJson(stopResp));
				fileWriter.flush();
				fileWriter.close();
				fileOut.close();
		    }
		    
		    stopNameKey = new StopNameKey(stop.name_en);
		    stopNameKey.setLocale("en", stop.name_en);
		    stopNameKey.setLocale("zh", stop.name_tc);
		    stopNameKey.setLocale("zh-hk", stop.name_tc);
		    stopNameKey.setLocale("zh-cn", stop.name_sc);
		    
		    addrKey = new AddressKey(stop.name_en);
		    addrKey.setLocale("en", stop.name_en);
		    addrKey.setLocale("zh", stop.name_tc);
		    addrKey.setLocale("zh-hk", stop.name_tc);
		    addrKey.setLocale("zh-cn", stop.name_sc);
		    
		    addStop(new TransitStop(
		    		TransitType.TRANSIT_BUS,
		    		"NWFB", //We put it in NWFB's db only, and put nothing in CTB to prevent duplication
		    		stop.stop,
		    		stopNameKey,
		    		addrKey,
		    		Double.parseDouble(stop.lat),
		    		Double.parseDouble(stop.lng)));
		    reportProgress((int) ((i + 1) / (float) stopIds.size() * 100.0 / 3.0 + 200 / 3.0));
		}
	    
		Console.println(reqCount + " request(s) have been made.");
		return true;
	}

	@Override
	public String generateVersion(long currentTimeMs, TransitRoute[] routes, TransitStop[] stops) {
		return Long.toString(currentTimeMs);
	}

}
