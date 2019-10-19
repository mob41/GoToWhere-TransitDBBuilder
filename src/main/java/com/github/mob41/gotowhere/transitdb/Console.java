package com.github.mob41.gotowhere.transitdb;

public class Console {

	private static String lastMessage = null;
	
	public static void println(String text) {
		if (lastMessage != null) {
			if (text.length() < lastMessage.length()) {
				text += spaces(lastMessage.length() - text.length());
			}
			lastMessage = null;
			System.out.println("\r" + text);
		} else {
			System.out.println(text);
		}
	}
	
	public static void sameLine(String text) {
		if (lastMessage != null) {
			if (text.length() < lastMessage.length()) {
				text += spaces(lastMessage.length() - text.length());
			}
		}
		lastMessage = text;
		System.out.print("\r" + text);
	}
	
	public static String spaces(int c) {
		String text = "";
		for (int i = 0; i < c; i++) {
			text += " ";
		}
		return text;
	}
	
}
