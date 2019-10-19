package com.github.mob41.gotowhere.transitdb.build.impl.ctbnwfb;

import com.google.gson.annotations.SerializedName;

public class CtbNwfbStopResponse {

	public String type;
	
	public String version;
	
	//TODO: Email them about the extra space =.=
	@SerializedName("generated_timestamp ")
	public String generated_timestamp;
	
	public CtbNwfbStop data;
	
}
