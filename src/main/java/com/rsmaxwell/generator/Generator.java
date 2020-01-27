package com.rsmaxwell.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.rsmaxwell.diaryjson.OutputDay;

public class Generator {

	public void toPDF(String outputDirName, String... inputDirNames) throws Exception {

		// ----------------------------------------------------------
		// - List the json files, ordered by date
		// ----------------------------------------------------------
		File outputDir = new File(outputDirName);
		outputDir.mkdirs();

		List<File> allFiles = new ArrayList<File>();

		for (String inputDirName : inputDirNames) {
			File inputDir = new File(inputDirName);

			File[] files = inputDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					boolean ok = name.toLowerCase().endsWith(".json");
					return ok;
				}
			});
			allFiles.addAll(Arrays.asList(files));
		}

		// ----------------------------------------------------------
		// - Parse the json files into OutputDay objects and collect each year
		// separately
		// - (There may be more than one OutputDay object for an actual day)
		// ----------------------------------------------------------
		TreeMap<Integer, TreeSet<OutputDay>> mapOfYears = new TreeMap<Integer, TreeSet<OutputDay>>();

		for (File file : allFiles) {

			ObjectMapper objectMapper = new ObjectMapper();
			OutputDay day = null;

			try {
				day = objectMapper.readValue(file, OutputDay.class);
			} catch (Exception e) {
				throw new Exception(file.getCanonicalPath(), e);
			}

			TreeSet<OutputDay> set = mapOfYears.get(day.year);
			if (set == null) {
				set = new TreeSet<OutputDay>();
				mapOfYears.put(day.year, set);
			}
			set.add(day);
		}

		// ----------------------------------------------------------
		// - Output an HTML document for each year, by looping through each day in order
		// - combining continuation days as appropriate.
		// ----------------------------------------------------------
		for (Integer year : mapOfYears.keySet()) {

			System.out.println("---[ " + year + "]-----------------------");

			StringBuilder sb = new StringBuilder();

			TreeSet<OutputDay> setOfDays = mapOfYears.get(year);

			int previousYear = 0;
			int previousMonth = 0;
			int previousDay = 0;

			for (OutputDay day : setOfDays) {

				if ((previousYear == day.year) && (previousMonth == day.month) && (previousDay == day.day)) {
					sb.append(" ");
				} else {
					String key = String.format("%04d-%02d-%02d  %s", day.year, day.month, day.day, day.page);
					sb.append("<h2>" + key + "</h2>");
				}
				sb.append(day.html.trim());

				previousYear = day.year;
				previousMonth = day.month;
				previousDay = day.day;
			}

			String regex = "[\\s]*[\\.]{3}</p> <p>[\\.]{3}[\\s]*";
			String html = sb.toString().replaceAll(regex, " ");

			File htmlDir = new File(outputDir, "html");
			htmlDir.mkdirs();

			File htmlFile = new File(htmlDir, Integer.toString(year) + ".html");
			Path path = htmlFile.toPath();
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
				writer.write(html);
			}

			File pdfDir = new File(outputDir, "pdf");
			pdfDir.mkdirs();

			File pdfFile = new File(pdfDir, Integer.toString(year) + ".pdf");

			ConverterProperties properties = new ConverterProperties();
			properties.setBaseUri("output/html");
			HtmlConverter.convertToPdf(htmlFile, pdfFile, properties);
		}
	}
}
