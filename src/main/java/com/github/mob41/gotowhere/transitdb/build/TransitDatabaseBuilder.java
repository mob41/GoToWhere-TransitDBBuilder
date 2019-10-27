package com.github.mob41.gotowhere.transitdb.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;

import com.github.mob41.gotowhere.transitdb.db.TransitDatabase;
import com.github.mob41.gotowhere.transitdb.db.TransitRoute;
import com.github.mob41.gotowhere.transitdb.db.TransitStop;
import com.github.mob41.gotowhere.transitdb.db.TransitType;

public abstract class TransitDatabaseBuilder extends Observable{
	
	private static final String PRE_KEY = "gtwtdb-downloaded-";

	public final TransitType transitType;
	
	public final String providerName;
	
	private final String builderName;
	
	private final List<TransitRoute> routes;
	
	private final List<TransitStop> stops;
	
	private int progress;
	
	private String msg;
	
	private String downloadedKey;
	
	private String sharedDownloadedKey;
	
	public TransitDatabaseBuilder(TransitType transitType, String providerName, String builderName) {
		this.transitType = transitType;
		this.providerName = providerName;
		this.builderName = builderName;
		routes = new ArrayList<TransitRoute>();
		stops = new ArrayList<TransitStop>();
		progress = 0;
		msg = null;
		downloadedKey = null;
		sharedDownloadedKey = builderName;
	}
	
	public final void cleanUp() {
		routes.clear();
		stops.clear();
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
	
	public final void generateDownloadedKey() {
		downloadedKey = PRE_KEY + sharedDownloadedKey + "-" + System.currentTimeMillis();
	}
	
	public final DownloadedInfo[] getAvailableDownloaded() {
	    File currentDir = new File(".");
	    File[] files = currentDir.listFiles();
	    List<DownloadedInfo> list = new ArrayList<DownloadedInfo>();
	    String[] split;
	    long timeMs;
	    for (File file : files) {
	    	if (!file.isDirectory() || !file.getName().startsWith(PRE_KEY)) {
	    		continue;
	    	}
	    	split = file.getName().split("-");
	    	
	    	if (split.length != 4) {
	    		continue;
	    	}
	    	
	    	try {
	    		timeMs = Long.parseLong(split[3]);
	    	} catch (NumberFormatException e) {
	    		continue;
	    	}
	    	
	    	if (!split[2].equals(sharedDownloadedKey)) {
	    		continue;
	    	}
	    	
	    	list.add(new DownloadedInfo(timeMs, split[0], file.getName()));
	    }
	    
	    DownloadedInfo[] infos = new DownloadedInfo[list.size()];
	    for (int i = 0; i < infos.length; i++) {
	    	infos[i] = list.get(i);
	    }
	    return infos;
	}
	
	public final void setDownloadedKey(String downloadedKey) {
		this.downloadedKey = downloadedKey;
	}
	
	public final String getDownloadedKey() {
		return downloadedKey;
	}
	
	public final void setSharedDownloadedKey(String sharedDownloadedKey) {
		this.sharedDownloadedKey = sharedDownloadedKey;
	}
	
	public final String getSharedDownloadedKey() {
		return sharedDownloadedKey;
	}
	
	public final FileOutputStream writeDownloaded(String fileName) {
		if (downloadedKey == null) {
			generateDownloadedKey();
		}
		
		File folder = new File(downloadedKey);
		
		if (folder.exists() && !folder.isDirectory()) {
			folder.delete();
		}
		
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		File file = new File(downloadedKey + "/" + fileName);
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
			return new FileOutputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public final FileInputStream readDownloaded(String fileName) {
		if (downloadedKey == null) {
			generateDownloadedKey();
		}
		
		File folder = new File(downloadedKey);
		
		if (!folder.exists() || !folder.isDirectory()) {
			return null;
		}
		
		File file = new File(downloadedKey + "/" + fileName);
		
		if (!file.exists()) {
			return null;
		}
		
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
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

	public String getBuilderName() {
		return builderName;
	}
	
}
