package com.github.mob41.gotowhere.transitdb;

import java.util.Observable;
import java.util.Observer;

import com.github.mob41.gotowhere.transitdb.build.TransitDatabaseBuilder;

public class ConsoleObserver implements Observer {
	
	private static final String[] SPINNER_SEQUENCE = {"\\", "\\", "|", "|", "/", "/", "-", "-"};
	
	private final String builderName;
	
	private final TransitDatabaseBuilder builder;
	
	private String lastMessage;
	
	private int spinnerIndex;

	public ConsoleObserver(String builderName, TransitDatabaseBuilder builder) {
		super();
		this.builderName = builderName;
		this.builder = builder;
		this.lastMessage = null;
		this.spinnerIndex = 0;
	}
	
	private String spinner() {
		if (spinnerIndex >= SPINNER_SEQUENCE.length) {
			spinnerIndex = 0;
		}
		return SPINNER_SEQUENCE[spinnerIndex++];
	}
	
	@Override
	public void update(Observable o, Object arg) {
		String text = spinner() + "  Building \"" + builderName + "\" database... " + builder.getProgress() + "%";
		
		String msg = builder.getMessage();
		if (msg != null) {
			text += " :: " + msg;
		}
		
		Console.sameLine(text);
	}

}
