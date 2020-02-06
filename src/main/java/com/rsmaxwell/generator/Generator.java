package com.rsmaxwell.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.rsmaxwell.diaryjson.DayOfFragments;
import com.rsmaxwell.diaryjson.Fragment;
import com.rsmaxwell.diaryjson.Key;
import com.rsmaxwell.diaryjson.Template;

public class Generator {

	private static final String DOCUMENT_HEADER = "document-header.txt";
	private static final String DOCUMENT_FOOTER = "document-footer.txt";
	private static final String YEAR_HEADER = "year-header.txt";
	private static final String MONTH_HEADER = "month-header.txt";
	private static final String DAY_HEADER = "day-header.txt";

	private static final String lineSeperator = System.getProperty("line.separator");

	private String fragmentsDirName;
	private String baseUriName;
	private String pdfDirName;
	private String depsDirName;

	private File inputDir;
	private File fragmentsDirFile;
	private File outputDir;
	private File templateDir;
	private File baseUri;
	private File pdfDirFile;
	private File depsDirFile;

	public Generator(String inputDirName, String outputDirName) {

		inputDir = new File(inputDirName);
		outputDir = new File(outputDirName);
		outputDir.mkdirs();

		templateDir = new File(inputDir, "templates");

		// -------------------------------------------------------
		// Establish directory names
		// -------------------------------------------------------
		fragmentsDirName = outputDirName + "/fragments";
		fragmentsDirFile = new File(fragmentsDirName);
		fragmentsDirFile.mkdirs();

		baseUriName = outputDirName + "/html";
		baseUri = new File(baseUriName);
		baseUri.mkdirs();

		pdfDirName = outputDirName + "/pdf";
		pdfDirFile = new File(pdfDirName);
		pdfDirFile.mkdirs();

		depsDirName = outputDirName + "/dependancies";
		depsDirFile = new File(depsDirName);
		depsDirFile.mkdirs();

	}

	public void toPDF() throws Exception {

		// ----------------------------------------------------------
		// - List the fragment files, ordered by date
		// ----------------------------------------------------------
		File[] fragmentsDirs = fragmentsDirFile.listFiles();

		// Check there is at least one fragment directory!
		if (fragmentsDirs.length <= 0) {
			throw new Exception("no fragments found in: " + fragmentsDirFile.getCanonicalPath());
		}

		// ----------------------------------------------------------
		// - Read all the fragments from file
		// ----------------------------------------------------------
		ObjectMapper objectMapper = new ObjectMapper();
		StringBuilder deps = new StringBuilder();

		TreeMap<Key, DayOfFragments> mapOfDays = new TreeMap<Key, DayOfFragments>();
		{
			DayOfFragments previousDay = new DayOfFragments(0, 0, 0);
			DayOfFragments day = null;

			for (File dir : fragmentsDirs) {

				// ----------------------------------------------------------
				// - Read the fragments from file
				// ----------------------------------------------------------
				Fragment fragment = objectMapper.readValue(new File(dir, "fragment.json"), Fragment.class);
				fragment.html = new String(Files.readAllBytes(new File(dir, "fragment.html").toPath()));

				if ((previousDay.year == fragment.year) && (previousDay.month == fragment.month) && (previousDay.day == fragment.day)) {
					day.add(fragment);

				} else {
					if (day != null) {
						Key key = new Key(day.year, day.month, day.day);
						mapOfDays.put(key, day);
					}
					day = new DayOfFragments(fragment.year, fragment.month, fragment.day);
					day.add(fragment);
				}

				// ----------------------------------------------------------
				// - Generate the dependencies
				// ----------------------------------------------------------
				if ((previousDay.year == fragment.year)) {
					deps.append(" ");
					deps.append(fragmentsDirName + "/" + dir.getName() + "/fragment.json");

				} else {
					if (deps.length() >= 0) {
						String diaryName = Integer.toString(previousDay.year);
						Path depsPath = Paths.get(depsDirName + "/" + diaryName + ".mk");
						try (BufferedWriter writer = Files.newBufferedWriter(depsPath)) {
							writer.write(deps.toString());
						}
					}

					String diaryName = Integer.toString(day.year);
					deps.append(baseUriName + "/" + diaryName + ".html");
					deps.append(" :");
				}

				previousDay = day;
			}

			if (day != null) {
				Key key = new Key(day.year, day.month, day.day);
				mapOfDays.put(key, day);

				String diaryName = Integer.toString(day.year);

				Path depsPath = Paths.get(depsDirName + "/" + diaryName + ".mk");
				try (BufferedWriter writer = Files.newBufferedWriter(depsPath)) {
					writer.write(deps.toString());
				}
			}
		}

		// -------------------------------------------------------
		// Add generated fragments
		// (Use an intermediate list to avoid adding to the main
		// collection as we are traversing it)
		// -------------------------------------------------------
		List<Fragment> listOfNewFragments = new ArrayList<Fragment>();

		{
			DayOfFragments previousDay = null;

			for (Key key : mapOfDays.keySet()) {
				DayOfFragments day = mapOfDays.get(key);

				if (previousDay != null) {
					if (previousDay.year != day.year) {
						if (!previousDay.hasDocumentFooter()) {
							Fragment f = new Fragment(previousDay.year, previousDay.month, previousDay.day, "z");
							f.html = Template.getString(new File(templateDir, DOCUMENT_FOOTER), previousDay);
							listOfNewFragments.add(f);
						}
					}
				}

				if ((previousDay == null) || (previousDay.year != day.year)) {
					if (!day.hasDocumentHeader()) {
						Fragment f = new Fragment(day.year, day.month, day.day, "a");
						f.html = Template.getString(new File(templateDir, DOCUMENT_HEADER), day);
						listOfNewFragments.add(f);
					}

					if (!day.hasYearHeader()) {
						Fragment f = new Fragment(day.year, day.month, day.day, "b");
						f.html = Template.getString(new File(templateDir, YEAR_HEADER), day);
						listOfNewFragments.add(f);
					}
				}

				if ((previousDay == null) || (previousDay.month != day.month)) {
					if (!day.hasMonthHeader()) {
						Fragment f = new Fragment(day.year, day.month, day.day, "c");
						f.html = Template.getString(new File(templateDir, MONTH_HEADER), day);
						listOfNewFragments.add(f);
					}
				}

				if ((previousDay == null) || (previousDay.day != day.day)) {
					if (!day.hasDayHeader()) {
						Fragment f = new Fragment(day.year, day.month, day.day, "d");
						f.html = Template.getString(new File(templateDir, DAY_HEADER), day);
						listOfNewFragments.add(f);
					}
				}

				previousDay = day;
			}

			if (previousDay != null) {
				if (!previousDay.hasDocumentFooter()) {
					Fragment f = new Fragment(previousDay.year, previousDay.month, previousDay.day, "z");
					f.html = Template.getString(new File(templateDir, DOCUMENT_FOOTER), previousDay);
					listOfNewFragments.add(f);
				}
			}

			// -------------------------------------------------------
			// Add the generated fragments
			// -------------------------------------------------------
			for (Fragment fragment : listOfNewFragments) {
				Key key = new Key(fragment.year, fragment.month, fragment.day);
				DayOfFragments day = mapOfDays.get(key);
				day.add(fragment);
			}
		}

		// ----------------------------------------------------------
		// Copy the stylesheets (*.css) to the output html directory
		// ----------------------------------------------------------

		// ----------------------------------------------------------
		// Output an HTML and PDF document for each year
		// ----------------------------------------------------------
		{
			DayOfFragments previousDay = new DayOfFragments(0, 0, 0);

			StringBuilder html = new StringBuilder();

			for (Key key : mapOfDays.keySet()) {
				DayOfFragments day = mapOfDays.get(key);

				if (previousDay.year != day.year) {
					if (html.length() > 0) {
						generateHtmlAndPdfDocument(previousDay.year, html.toString());
					}

					html = new StringBuilder();
				}

				TreeSet<Fragment> setOfFragments = day.fragments;
				for (Fragment fragment : setOfFragments) {
					html.append(fragment.html);
				}

				// -------------------------------------------------------
				// Next ...
				// -------------------------------------------------------
				previousDay = day;
			}

			if (html.length() > 0) {
				generateHtmlAndPdfDocument(previousDay.year, html.toString());
			}
		}
	}

	private void generateHtmlAndPdfDocument(int year, String html) throws IOException {

		String diaryName = Integer.toString(year);
		String diaryHtmlFilename = diaryName + ".html";
		String diaryPdfFilename = diaryName + ".pdf";

		String diaryHtmlPathName = baseUriName + "/" + diaryHtmlFilename;
		String diaryPdfPathName = pdfDirName + "/" + diaryPdfFilename;

		// -------------------------------------------------------
		// Write out the html document
		// -------------------------------------------------------
		File htmlFile = new File(diaryHtmlPathName);
		Path htmlPath = htmlFile.toPath();
		try (BufferedWriter writer = Files.newBufferedWriter(htmlPath)) {
			writer.write(html);
		}

		// -------------------------------------------------------
		// Convert the html to PDF
		// -------------------------------------------------------
		File pdfFile = new File(diaryPdfPathName);
		ConverterProperties properties = new ConverterProperties();
		properties.setBaseUri(baseUriName);
		HtmlConverter.convertToPdf(htmlFile, pdfFile, properties);
	}
}
