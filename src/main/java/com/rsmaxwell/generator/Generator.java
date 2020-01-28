package com.rsmaxwell.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.rsmaxwell.diaryjson.Day;
import com.rsmaxwell.diaryjson.Month;
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
				try {
					if (previousYear != day.year) {
						String key = String.format("%04d", day.year);
						sb.append("<h2>" + key + "</h2>");
					}

					if (previousMonth != day.month) {
						String key = Month.toString(day.month);
						sb.append("<h3>" + key + "</h3>");
					}

					if (previousDay != day.day) {
						LocalDate localDate = LocalDate.of(day.year, day.month, day.day);
						DayOfWeek dayOfWeek = DayOfWeek.from(localDate);
						int val = dayOfWeek.getValue();

						String key = String.format("%s  %02d     %s", Day.toString(val), day.day, day.reference);
						sb.append("<h4>" + key + "</h4>");
					}

					if ((previousYear == day.year) && (previousMonth == day.month) && (previousDay == day.day)) {
						sb.append(" ");
					}
					sb.append(day.html.trim());

					previousYear = day.year;
					previousMonth = day.month;
					previousDay = day.day;
				} catch (Exception e) {
					String key = String.format("%04d-%02d-%02d  %s    %s", day.year, day.month, day.day, day.order,
							day.reference);
					throw new Exception("day: " + key, e);
				}
			}

			String regex = "[\\s]*[\\.]{3}</p> <p>[\\.]{3}[\\s]*";
			String html = sb.toString().replaceAll(regex, " ");

			File htmlDir = new File(outputDir, "html/" + Integer.toString(year));
			htmlDir.mkdirs();

			File htmlFile = new File(htmlDir, Integer.toString(year) + ".html");
			Path path = htmlFile.toPath();
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
				writer.write(html);
			}

			File absolutePdfDir = null;
			if (outputDir.isAbsolute()) {
				absolutePdfDir = new File(outputDir, "pdf");
			} else {
				String currentDirName = System.getProperty("user.home");
				File absoluteOutputDir = new File(currentDirName, outputDir.getName());
				absolutePdfDir = new File(absoluteOutputDir.getName(), "pdf");
			}
			absolutePdfDir.mkdirs();

			File pdfFile = new File(absolutePdfDir, Integer.toString(year) + ".pdf");
			String baseUri = "output/html/" + Integer.toString(year);

			ConverterProperties properties = new ConverterProperties();
			properties.setBaseUri(baseUri);
			HtmlConverter.convertToPdf(htmlFile, pdfFile, properties);
		}
	}
}
