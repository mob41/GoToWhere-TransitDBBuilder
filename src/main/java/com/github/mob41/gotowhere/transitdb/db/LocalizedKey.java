package com.github.mob41.gotowhere.transitdb.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LocalizedKey {

	private final String key;
	
	private final String value;
	
	private final Map<String, String> localizedKeys;
	
	public LocalizedKey(String key, String value) {
		this.key = key;
		this.value = value;
		localizedKeys = new HashMap<String, String>();
	}
	
	public void setLocale(String locale, String value) {
		localizedKeys.put(locale, value);
	}
	
	public void removeLocale(String locale) {
		localizedKeys.remove(locale);
	}
	
	public Map<String, Object> getMap(){
		Map<String, Object> out = new HashMap<String, Object>();
		out.put(key, value);
		Iterator<String> keys = localizedKeys.keySet().iterator();
		String ikey;
		while(keys.hasNext()) {
			ikey = keys.next();
			out.put(key + "_" + ikey, localizedKeys.get(ikey));
		}
		return out;
	}
}
