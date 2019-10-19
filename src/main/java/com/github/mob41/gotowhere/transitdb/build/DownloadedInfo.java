package com.github.mob41.gotowhere.transitdb.build;

import java.util.Calendar;

public class DownloadedInfo {

	private final long timeMs;
	
	private final String providerName;
	
	private final String downloadedKey;

	public DownloadedInfo(long timeMs, String providerName, String downloadedKey) {
		super();
		this.timeMs = timeMs;
		this.providerName = providerName;
		this.downloadedKey = downloadedKey;
	}

	public long getTimeMs() {
		return timeMs;
	}

	public String getProviderName() {
		return providerName;
	}

	public String getDownloadedKey() {
		return downloadedKey;
	}
	
}
