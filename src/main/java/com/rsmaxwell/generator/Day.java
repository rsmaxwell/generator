package com.rsmaxwell.generator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day {

	static final String[] daynames = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

	static Pattern pattern;

	static {
		StringBuilder sb = new StringBuilder();
		String separator = "";
		for (String day : daynames) {
			sb.append(separator);
			sb.append(day);
			separator = "|";
		}
		String regx = "^(" + sb.toString() + ")\\s+(.*)";
		pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	}

	public static Matcher getMatcher(String line) {
		return pattern.matcher(line);
	}

	public static int toInt(String string) throws Exception {

		for (int i = 0; i < daynames.length; i++) {
			if (daynames[i].equals(string)) {
				return i + 1;
			}
		}
		throw new Exception("Invalid day name: '" + string + "'");
	}

	public static void check(int year, int month, int day, int dayOfWeek1) throws Exception {

		LocalDate localDate = LocalDate.of(year, month, day);
		DayOfWeek dayOfWeek = localDate.getDayOfWeek();

		int dayOfWeek2 = dayOfWeek.getValue();

		if (dayOfWeek1 != dayOfWeek2) {
			throw new Exception("Inconsistent day-of-week: " + year + "-" + month + "-" + day + " --> day-of-week: "
					+ dayOfWeek1 + " / " + dayOfWeek2);
		}

	}
}
