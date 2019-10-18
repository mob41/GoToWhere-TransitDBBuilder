package com.github.mob41.gotowhere.transitdb;

import java.util.Observable;
import java.util.Observer;

import com.github.mob41.gotowhere.transitdb.build.TransitDatabaseBuilder;

public class ConsoleObserver implements Observer {
	
	private final String builderName;
	
	private final TransitDatabaseBuilder builder;

	public ConsoleObserver(String builderName, TransitDatabaseBuilder builder) {
		super();
		this.builderName = builderName;
		this.builder = builder;
	}



	@Override
	public void update(Observable o, Object arg) {
		String text = "\rBuilding \"" + builderName + "\" database... " + builder.getProgress() + "%";
		
		String msg = builder.getMessage();
		if (msg != null) {
			text += " :: " + msg;
		}
		
		System.out.println(text);
	}

}
