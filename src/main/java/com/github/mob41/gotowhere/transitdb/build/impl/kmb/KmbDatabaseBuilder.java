package com.github.mob41.gotowhere.transitdb.build.impl.kmb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.github.mob41.gotowhere.transitdb.build.TransitDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.db.AddressKey;
import com.github.mob41.gotowhere.transitdb.db.StopNameKey;
import com.github.mob41.gotowhere.transitdb.db.TransitRoute;
import com.github.mob41.gotowhere.transitdb.db.TransitStop;
import com.github.mob41.gotowhere.transitdb.db.TransitType;
import com.google.gson.Gson;

public class KmbDatabaseBuilder extends TransitDatabaseBuilder {
	
	private static final String ROUTES_URL = "http://etadatafeed.kmb.hk:1933/GetData.ashx?type=ETA_R";
	
	private static final String ROUTE_SEARCH_URL = "http://www.kmb.hk/ajax/getRouteMapByBusno.php";
	
	private Gson gson;

	public KmbDatabaseBuilder() {
		super(TransitType.TRANSIT_BUS, "KMB");
		gson = new Gson();
	}

	@Override
	public boolean build() throws Exception {
		reportMessage("Downloading list of routes...");
		
		URL url = new URL(ROUTES_URL);
	    URLConnection conn = url.openConnection();
	    
	    String[] routes = null;
	    try {
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		    KmbRoutes[] raw = gson.fromJson(reader, KmbRoutes[].class);
		    
		    if (raw.length != 1) {
				reportMessage("Error!");
		    	System.err.println("\rError: Received routes JSON array is not holding one JSON. " + raw.length + " found.");
		    	return false;
		    }
		    
		    String rno = raw[0].r_no;
		    
		    if (rno == null) {
				reportMessage("Error!");
		    	System.err.println("\rError: Received routes JSON does not contain \"r_no\" parameter.");
		    	return false;
		    }
		    
		    routes = rno.split(",");
	    } catch (Exception e){
	    	return false;
	    }
	    
	    if (routes == null || routes.length == 0) {
			reportMessage("Error!");
	    	System.err.println("\rError: No routes received. Routes is null or with zero length.");
	    	return false;
	    }

	    List<String> stopIds = new ArrayList<String>();
	    
	    StopNameKey stopNameKey;
	    AddressKey addrKey;
	    KmbStop[] stops;
	    String[] pathStops;
	    String[][] paths;
	    List<String[]> bounds;
	    for (int i = 0; i < routes.length; i++) {
	    	reportMessage((i + 1) +  ": " + routes[i]);
	    	
	    	bounds = new ArrayList<String[]>();
	    	for (int j = 1; j <= 2; j++) {
	    		stops = searchRoute(routes[i], j);
	    		if (stops == null) {
	    			if (j == 1) {
	    				System.err.println("\rWarning: No bound data received for " + routes[i]);
	    			}
	    			break;
	    		}
	    		
	    		pathStops = new String[stops.length];
	    		for (int x = 0; x < stops.length; x++) {
	    			if (!stopIds.contains(stops[x].subarea)) {
	    				stopIds.add(stops[x].subarea);
	    				
	    				stopNameKey = new StopNameKey(stops[x].title_eng);
	    				stopNameKey.setLocale("en", stops[x].title_eng);
	    				stopNameKey.setLocale("zh", stops[x].title_chi);
	    				
	    				addrKey = new AddressKey(stops[x].address_eng);
	    				addrKey.setLocale("en", stops[x].address_eng);
	    				addrKey.setLocale("zh", stops[x].address_chi);
	    				
	    				addStop(new TransitStop(
	    						transitType,
	    						providerName,
	    						stops[x].subarea,
	    						stopNameKey,
	    						addrKey,
	    						Double.parseDouble(stops[x].lat),
	    						Double.parseDouble(stops[x].lng)));
	    			}
	    			pathStops[x] = stops[x].subarea;
	    		}
	    		bounds.add(pathStops);
	    	}
	    	
	    	paths = new String[bounds.size()][];
	    	for (int j = 0; j < bounds.size(); j++) {
	    		paths[j] = bounds.get(j);
	    	}
	    	
	    	addRoute(new TransitRoute(transitType, providerName, routes[i], paths));
	    	reportProgress((int) ((i + 1) / (float) routes.length * 100.0));
	    }
    	reportMessage("Done!");
	    
		return true;
	}
	
	private KmbStop[] searchRoute(String bn, int dir) throws Exception{
		try {
			URL url = new URL(ROUTE_SEARCH_URL);
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);

		    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

		    writer.write("bn=" + bn + "&dir=" + dir);
		    writer.flush();
		    String line;
		    String data = "";
		    try {
		    	BufferedReader reader = new BufferedReader(new 
		                InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
			    while ((line = reader.readLine()) != null) {
			        data += line;
			     }
		    } catch (Exception e){
		    	System.err.println("\rError: Read error. Auto-recovering. Repeating the same request in 10 seconds.");
		    	Thread.sleep(10000);
		    	return searchRoute(bn, dir);
		    }
		    
		    if (data == ""){
		    	return null;
		    }
		    
		    data = "[" + data.substring(2, data.length() - 2) + "]";
		    
		    if (data.equals("[]")){
		    	return null;
		    }
		    
		    return gson.fromJson(data, KmbStop[].class);
		} catch (Exception e){
	    	System.err.println("\rError: Connection error. Auto-recovering. Repeating the same request in 10 seconds.");
	    	Thread.sleep(10000);
	    	return searchRoute(bn, dir);
		}
	}

	@Override
	public String generateVersion(long currentTimeMs, TransitRoute[] routes, TransitStop[] stops) {
		return Long.toString(currentTimeMs);
	}

}
