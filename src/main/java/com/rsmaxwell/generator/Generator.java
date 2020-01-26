package com.rsmaxwell.generator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmaxwell.diaryjson.OutputDay;
import com.rsmaxwell.diaryjson.OutputDocument;

public class Generator {

	public void toPDF(String inputDirName, String outputDirName) throws Exception {

		final String regex1 = "img\\d{4}(-[ab])?\\.json";

		File dir = new File(outputDirName);
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean ok = name.matches(regex1);
				return ok;
			}
		});

		HashMap<String, TreeSet<OutputDay>> mapOfDays = new HashMap<String, TreeSet<OutputDay>>();

		for (File file : files) {
			ObjectMapper objectMapper = new ObjectMapper();
			OutputDocument document = objectMapper.readValue(file, OutputDocument.class);

			for (OutputDay day : document.days) {
				String key = String.format("%04d-%02d-%02d", day.year, day.month, day.day);

				TreeSet<OutputDay> set = mapOfDays.get(key);
				if (set == null) {
					set = new TreeSet<OutputDay>();
					mapOfDays.put(key, set);
				}
				set.add(day);
			}
		}

		TreeSet<OutputDay> setOfDays = new TreeSet<OutputDay>();

		for (String key : mapOfDays.keySet()) {
			TreeSet<OutputDay> set = mapOfDays.get(key);

			if (set.size() > 1) {
				System.out.println("    continuation");
			}

			OutputDay wholeday = null;
			for (OutputDay day : set) {
				if (wholeday == null) {
					wholeday = day;
				} else {
					wholeday.page += ", " + day.page;
					wholeday.line += day.line;
				}
			}

			String regex2 = "...</p><p>...";
			wholeday.line = wholeday.line.replaceAll(regex2, " ");

			setOfDays.add(wholeday);
		}

		for (OutputDay day : setOfDays) {
			System.out.println("---------------");
			System.out.println("date: " + day.year + "-" + day.month + "-" + day.day);
			System.out.println("tag: " + day.page);
			System.out.println("line: " + day.line);
		}
	}
}
