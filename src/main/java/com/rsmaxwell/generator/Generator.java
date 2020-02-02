package com.rsmaxwell.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.rsmaxwell.diaryjson.Day;
import com.rsmaxwell.diaryjson.Fragment;
import com.rsmaxwell.diaryjson.Month;

public class Generator {

	private static final String DOCUMENT_HEADER = "document-header.txt";
	private static final String DOCUMENT_FOOTER = "document-footer.txt";
	private static final String YEAR_HEADER = "year-header.txt";
	private static final String MONTH_HEADER = "month-header.txt";
	private static final String DAY_HEADER = "day-header.txt";

	private static final String lineSeperator = System.getProperty("line.separator");

	private String inputDirName;
	private String outputDirName;

	private File inputDir;
	private File outputDir;

	public Generator(String inputDirName, String outputDirName) {
		this.inputDirName = inputDirName;
		this.outputDirName = outputDirName;

		inputDir = new File(inputDirName);
		outputDir = new File(outputDirName);
		outputDir.mkdirs();
	}

	public void toPDF() throws Exception {

		// ----------------------------------------------------------
		// - List the fragment files, ordered by date
		// ----------------------------------------------------------
		File fragmentDir = new File(outputDirName + "/fragments");
		if (!fragmentDir.exists()) {
			throw new Exception("directory not found: " + fragmentDir.getCanonicalPath());
		}

		File[] files = fragmentDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean ok = name.toLowerCase().endsWith(".json");
				return ok;
			}
		});

		// Check there is at least one fragment file!
		if (files.length <= 0) {
			throw new Exception("no fragments found in: " + fragmentDir.getCanonicalPath());
		}

		// ----------------------------------------------------------
		// - Parse the json fragments into their year
		// - (There may be more than one fragment per day)
		// ----------------------------------------------------------
		TreeMap<Integer, TreeSet<Fragment>> mapOfYears = new TreeMap<Integer, TreeSet<Fragment>>();

		for (File file : files) {

			ObjectMapper objectMapper = new ObjectMapper();
			Fragment fragment = null;

			try {
				fragment = objectMapper.readValue(file, Fragment.class);
			} catch (Exception e) {
				throw new Exception(file.getCanonicalPath(), e);
			}

			TreeSet<Fragment> set = mapOfYears.get(fragment.year);
			if (set == null) {
				set = new TreeSet<Fragment>();

				mapOfYears.put(fragment.year, set);
			}

			set.add(fragment);
		}

		// -------------------------------------------------------
		// Establish directory names
		// -------------------------------------------------------
		String fragmentDirName = outputDirName + "/fragments";
		File fragmentDirFile = new File(fragmentDirName);
		fragmentDirFile.mkdirs();

		String baseUriName = outputDirName + "/html";
		File baseUri = new File(baseUriName);
		baseUri.mkdirs();

		String pdfDirName = outputDirName + "/pdf";
		File pdfDirFile = new File(pdfDirName);
		pdfDirFile.mkdirs();

		String depsDirName = outputDirName + "/dependancies";
		File depsDirFile = new File(depsDirName);
		depsDirFile.mkdirs();

		// ----------------------------------------------------------
		// - Output an HTML document for each year, by looping through each day in order
		// - combining continuation days as appropriate.
		// ----------------------------------------------------------
		int previousYear = 0;
		int previousMonth = 0;
		int previousDay = 0;

		for (Integer year : mapOfYears.keySet()) {

			// -------------------------------------------------------
			// Establish path names
			// -------------------------------------------------------
			String diaryName = Integer.toString(year);
			String diaryHtmlFilename = diaryName + ".html";
			String diaryPdfFilename = diaryName + ".pdf";
			String diaryDepsFilename = diaryName + ".mk";

			String diaryHtmlPathName = baseUriName + "/" + diaryHtmlFilename;
			String diaryPdfPathName = pdfDirName + "/" + diaryPdfFilename;
			String diaryDepsPathName = depsDirFile + "/" + diaryDepsFilename;

			// -------------------------------------------------------
			//
			// -------------------------------------------------------

			StringBuilder deps = new StringBuilder();
			deps.append(diaryHtmlPathName);
			deps.append(" :");

			StringBuilder html = new StringBuilder();

			Map<String, String> map = new HashMap<String, String>();
			map.put("@@TITLE@@", "Diary " + year);

			html.append(getTemplate(DOCUMENT_HEADER, map));

			TreeSet<Fragment> setOfFragments = mapOfYears.get(year);

			// -------------------------------------------------------
			// Combine the fragments into an html document
			// -------------------------------------------------------
			for (Fragment fragment : setOfFragments) {
				try {
					// -------------------------------------------------------
					// If necessary, write out a header
					// -------------------------------------------------------
					if (previousYear != fragment.year) {
						html.append(getTemplate(YEAR_HEADER, map, fragment));
					}

					if (previousMonth != fragment.month) {
						html.append(getTemplate(MONTH_HEADER, map, fragment));
					}

					if (previousDay != fragment.day) {
						html.append(getTemplate(DAY_HEADER, map, fragment));
					}

					if ((previousYear == fragment.year) && (previousMonth == fragment.month)
							&& (previousDay == fragment.day)) {
						html.append(" ");
					}

					// -------------------------------------------------------
					// Write out the fragment html
					// -------------------------------------------------------
					html.append(fragment.html.trim());

					// -------------------------------------------------------
					// List the dependencies
					// -------------------------------------------------------
					deps.append(" ");
					deps.append(fragmentDirName + "/" + fragment.reference + ".json");

					// -------------------------------------------------------
					// Next...
					// -------------------------------------------------------
					previousYear = fragment.year;
					previousMonth = fragment.month;
					previousDay = fragment.day;

				} catch (Exception e) {
					String key = String.format("%04d-%02d-%02d  %s    %s", fragment.year, fragment.month, fragment.day,
							fragment.order, fragment.reference);
					throw new Exception("fragment: " + key, e);
				}
			}

			html.append(getTemplate(DOCUMENT_FOOTER, map));

			// -------------------------------------------------------
			// Maybe join paragraphs from adjacent fragments
			// -------------------------------------------------------
			String regex = "[\\s]*[\\.]{3}</p> <p>[\\.]{3}[\\s]*";
			String htmlString = html.toString().replaceAll(regex, " ");

			// -------------------------------------------------------
			// Make the html file in the baseUri directory
			// -------------------------------------------------------
			File htmlFile = new File(diaryHtmlPathName);
			Path htmlPath = htmlFile.toPath();
			try (BufferedWriter writer = Files.newBufferedWriter(htmlPath)) {
				writer.write(htmlString);
			}

			// -------------------------------------------------------
			// Convert the html into PDF
			// -------------------------------------------------------
			File pdfFile = new File(diaryPdfPathName);
			ConverterProperties properties = new ConverterProperties();
			properties.setBaseUri(baseUriName);
			HtmlConverter.convertToPdf(htmlFile, pdfFile, properties);

			// -------------------------------------------------------
			// Write out the dependency file
			// -------------------------------------------------------
			deps.append(lineSeperator);
			deps.append(lineSeperator);
			deps.append(diaryPdfPathName);
			deps.append(" : ");
			deps.append(diaryHtmlPathName);

			Path depsPath = Paths.get(diaryDepsPathName);
			try (BufferedWriter writer = Files.newBufferedWriter(depsPath)) {
				writer.write(deps.toString());
			}
		}
	}

	private String getTemplate(String template, Map<String, String> map, Fragment fragment) throws Exception {

		LocalDate localDate = LocalDate.of(fragment.year, fragment.month, fragment.day);
		DayOfWeek dayOfWeek = DayOfWeek.from(localDate);

		map.put("@@YEAR@@", Integer.toString(fragment.year));
		map.put("@@MONTH@@", Integer.toString(fragment.month));
		map.put("@@MONTH_NAME@@", Month.toString(fragment.month));
		map.put("@@DAY@@", Integer.toString(fragment.day));
		map.put("@@DAY_NAME@@", Day.toString(dayOfWeek.getValue()));
		map.put("@@REFERENCE@@", fragment.reference);

		return getTemplate(template, map);
	}

	private String getTemplate(String template, Map<String, String> map) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(inputDirName + "/templates/" + template)));

		map.put("@@BUILD_ID@@", getenv("BUILD_ID", "snapshot"));
		map.put("@@BUILD_DATE@@", getenv("BUILD_DATE", "snapshot"));
		map.put("@@GIT_COMMIT@@", getenv("GIT_COMMIT", "snapshot"));
		map.put("@@GIT_BRANCH@@", getenv("GIT_BRANCH", "snapshot"));
		map.put("@@GIT_URL@@", getenv("GIT_URL", "snapshot"));

		for (String tag : map.keySet()) {
			content = content.replaceAll(tag, map.get(tag));
		}

		return content;
	}

	private String getenv(String key, String fallback) {
		String value = System.getenv(key);
		if (value == null) {
			return fallback;
		}
		return value;
	}
}
