package com.rsmaxwell.generator;

public class Month {

	static final String[] months = { "January", "February", "March", "April", "May", "June", "July", "August",
			"September", "October", "November", "December" };

	public static int toInt(String text) throws Exception {
		for (int i = 0; i < months.length; i++) {
			if (months[i].equalsIgnoreCase(text)) {
				return i + 1;
			}
		}
		throw new Exception("Unexpected month: " + text);
	}
}
