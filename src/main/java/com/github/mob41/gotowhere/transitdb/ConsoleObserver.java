package com.github.mob41.gotowhere.transitdb;

import java.util.Observable;
import java.util.Observer;

import com.github.mob41.gotowhere.transitdb.build.TransitDatabaseBuilder;

public class ConsoleObserver implements Observer {
	
	private final String builderName;
	
	private final TransitDatabaseBuilder builder;
	
	private String lastMessage;

	public ConsoleObserver(String builderName, TransitDatabaseBuilder builder) {
		super();
		this.builderName = builderName;
		this.builder = builder;
		this.lastMessage = null;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		String text = "\rBuilding \"" + builderName + "\" database... " + builder.getProgress() + "%";
		
		String msg = builder.getMessage();
		if (msg != null) {
			text += " :: " + msg;
		}
		
		if (lastMessage != null && msg.length() < lastMessage.length()) {
			text += spaces(lastMessage.length() - msg.length() + 4);
		}
		lastMessage = msg;
		
		System.out.print(text);
	}
	
	public static String spaces(int c) {
		String text = "";
		for (int i = 0; i < c; i++) {
			text += " ";
		}
		return text;
	}

}
