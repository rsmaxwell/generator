package com.rsmaxwell.generator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmaxwell.diaryjson.OutputDay;

public class Generator {

	public void toPDF(File inputDir, File outputDir) throws Exception {

		outputDir.mkdirs();

		final String regex1 = "[\\d]{4}-[\\d]{2}-[\\d]{2}-.*";

		File[] files = inputDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean ok = name.matches(regex1);
				return ok;
			}
		});

		TreeMap<Integer, TreeSet<OutputDay>> mapOfYears = new TreeMap<Integer, TreeSet<OutputDay>>();

		for (File file : files) {
			ObjectMapper objectMapper = new ObjectMapper();
			OutputDay day = objectMapper.readValue(file, OutputDay.class);

			TreeSet<OutputDay> set = mapOfYears.get(day.year);
			if (set == null) {
				set = new TreeSet<OutputDay>();
				mapOfYears.put(day.year, set);
			}
			set.add(day);
		}

		for (Integer year : mapOfYears.keySet()) {

			System.out.println("---[ " + year + "]-----------------------");

			TreeSet<OutputDay> setOfDays = mapOfYears.get(year);

			StringBuilder sb = new StringBuilder();

			int previousYear = 0;
			int previousMonth = 0;
			int previousDay = 0;

			for (OutputDay day : setOfDays) {

				if ((previousYear == day.year) && (previousMonth == day.month) && (previousDay == day.day)) {
					System.out.println("---[continued   " + day.page + "]-----");
					sb.append(" ");
				} else {
					printLine(sb.toString());
					sb.setLength(0);

					String key = String.format("%04d-%02d-%02d  %s", day.year, day.month, day.day, day.page);
					System.out.println("---[" + key + "]------");
				}
				sb.append(day.line.trim());

				previousYear = day.year;
				previousMonth = day.month;
				previousDay = day.day;
			}

			if (sb.length() > 0) {
				printLine(sb.toString());
			}
		}
	}

	private void printLine(String line) {

		String regex = "[\\s]*[\\.]{3}</p> <p>[\\.]{3}[\\s]*";
		line = line.replaceAll(regex, " ");

		System.out.println(line);
	}
}
