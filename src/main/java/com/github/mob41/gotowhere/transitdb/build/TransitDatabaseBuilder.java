package com.github.mob41.gotowhere.transitdb.build;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;

import com.github.mob41.gotowhere.transitdb.db.TransitDatabase;
import com.github.mob41.gotowhere.transitdb.db.TransitRoute;
import com.github.mob41.gotowhere.transitdb.db.TransitStop;
import com.github.mob41.gotowhere.transitdb.db.TransitType;

public abstract class TransitDatabaseBuilder extends Observable{

	private final TransitType transitType;
	
	private final String providerName;
	
	private final List<TransitRoute> routes;
	
	private final List<TransitStop> stops;
	
	private int progress;
	
	public TransitDatabaseBuilder(TransitType transitType, String providerName) {
		this.transitType = transitType;
		this.providerName = providerName;
		routes = new ArrayList<TransitRoute>();
		stops = new ArrayList<TransitStop>();
		progress = 0;
	}

	public abstract boolean build();
	
	public abstract String generateVersion(Calendar cal, TransitRoute[] routes, TransitStop[] stops);
	
	public final void reportProgress(int progress) {
		this.progress = progress;
		this.setChanged();
		this.notifyObservers();
	}
	
	public final void addRoute(TransitRoute route) {
		routes.add(route);
	}
	
	public final void addStop(TransitStop stop) {
		stops.add(stop);
	}

	public final TransitType getTransitType() {
		return transitType;
	}

	public final String getProviderName() {
		return providerName;
	}

	public int getProgress() {
		return progress;
	}
	
	public TransitDatabase create() {
		TransitRoute[] outRoutes = new TransitRoute[routes.size()];
		for (int i = 0; i < outRoutes.length; i++) {
			outRoutes[i] = routes.get(i);
		}
		
		TransitStop[] outStops = new TransitStop[stops.size()];
		for (int i = 0; i < outStops.length; i++) {
			outStops[i] = stops.get(i);
		}
		
		Calendar cal = Calendar.getInstance();
		
		TransitDatabase inst = new TransitDatabase(transitType, providerName, generateVersion(cal, outRoutes, outStops), cal.getTimeInMillis(), outRoutes, outStops);
		return inst;
	}
	
}
