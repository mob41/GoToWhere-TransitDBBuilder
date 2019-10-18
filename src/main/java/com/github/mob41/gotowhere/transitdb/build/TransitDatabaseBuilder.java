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

	public final TransitType transitType;
	
	public final String providerName;
	
	private final List<TransitRoute> routes;
	
	private final List<TransitStop> stops;
	
	private int progress;
	
	private String msg;
	
	public TransitDatabaseBuilder(TransitType transitType, String providerName) {
		this.transitType = transitType;
		this.providerName = providerName;
		routes = new ArrayList<TransitRoute>();
		stops = new ArrayList<TransitStop>();
		progress = 0;
		msg = null;
	}

	public abstract boolean build() throws Exception;
	
	public abstract String generateVersion(long currentTimeMs, TransitRoute[] routes, TransitStop[] stops);
	
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

	public final int getProgress() {
		return progress;
	}
	
	public final void reportMessage(String msg) {
		this.msg = msg;
		this.setChanged();
		this.notifyObservers();
	}
	
	public final String getMessage() {
		return msg;
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
		
		long currentTimeMs = System.currentTimeMillis();
		
		TransitDatabase inst = new TransitDatabase(transitType, providerName, generateVersion(currentTimeMs, outRoutes, outStops), currentTimeMs, outRoutes, outStops);
		return inst;
	}
	
}
