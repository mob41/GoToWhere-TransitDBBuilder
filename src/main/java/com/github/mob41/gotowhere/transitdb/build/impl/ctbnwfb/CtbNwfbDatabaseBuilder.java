package com.github.mob41.gotowhere.transitdb.build.impl.ctbnwfb;

import com.github.mob41.gotowhere.transitdb.build.TransitDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.db.TransitRoute;
import com.github.mob41.gotowhere.transitdb.db.TransitStop;
import com.github.mob41.gotowhere.transitdb.db.TransitType;

public class CtbNwfbDatabaseBuilder extends TransitDatabaseBuilder {

	public CtbNwfbDatabaseBuilder() {
		super(TransitType.TRANSIT_BUS, "CTBNWFB");
	}

	@Override
	public boolean build() throws Exception {
		
		return false;
	}

	@Override
	public String generateVersion(long currentTimeMs, TransitRoute[] routes, TransitStop[] stops) {
		return Long.toString(currentTimeMs);
	}

}
