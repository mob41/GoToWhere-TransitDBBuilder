package com.github.mob41.gotowhere.transitdb.build.impl.ctbnwfb;

import com.google.gson.annotations.SerializedName;

public class CtbNwfbStop {

	public String stop;
	
	public String name_tc;
	
	public String name_en;
	
	public String lat;

	//Why they use long instead of lng?
	@SerializedName("long")
	public String lng;
	
	public String name_sc;
	
	public String data_timestamp;
	
}
