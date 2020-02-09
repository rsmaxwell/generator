package com.rsmaxwell.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.rsmaxwell.diaryjson.Fragment;
import com.rsmaxwell.diaryjson.Key;
import com.rsmaxwell.diaryjson.Templates;

public class Generator {

	private static final String DOCUMENT_HEADER_1 = "document-header-1";
	private static final String DOCUMENT_HEADER_2 = "document-header-2";
	private static final String DOCUMENT_FORWARD = "document-forward";
	private static final String DOCUMENT_FOOTER = "document-footer";
	private static final String YEAR_HEADER_1 = "year-header-1";
	private static final String YEAR_HEADER_2 = "year-header-2";
	private static final String YEAR_FOOTER = "year-footer";
	private static final String MONTH_HEADER_1 = "month-header-1";
	private static final String MONTH_HEADER_2 = "month-header-2";
	private static final String MONTH_FOOTER = "month-footer";
	private static final String DAY_HEADER = "day-header";
	private static final String DAY_FOOTER = "day-footer";

	private String fragmentsDirName;
	private String baseUriName;
	private String pdfDirName;
	private String depsDirName;

	private File fragmentsDirFile;
	private File outputDir;
	private File baseUri;
	private File pdfDirFile;
	private File depsDirFile;

	private Templates templates;

	public Generator(String inputDirName, String outputDirName) throws Exception {

		outputDir = new File(outputDirName);
		outputDir.mkdirs();

		templates = new Templates(new File(inputDirName, "templates"));

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
		// - List the fragment directories
		// ----------------------------------------------------------
		File[] fragmentsDir = fragmentsDirFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File file = new File(dir, name);
				return file.isDirectory();
			}
		});

		// Check there is at least one fragment directory!
		if (fragmentsDir.length <= 0) {
			throw new Exception("no fragments found in: " + fragmentsDirFile.getCanonicalPath());
		}

		// ----------------------------------------------------------
		// - Read all the fragments from file
		// ----------------------------------------------------------
		StringBuilder deps = new StringBuilder();

		TreeMap<Key, Fragment> mapOfFragments = new TreeMap<Key, Fragment>();
		{
			Fragment previousFragment = new Fragment(0, 0, 0, "");

			for (File dir : fragmentsDir) {
				try {
					// ----------------------------------------------------------
					// - Read the fragment from file and add to the list
					// ----------------------------------------------------------
					Fragment fragment = Fragment.MakeFragment(dir);

					Key key = new Key(fragment.year, fragment.month, fragment.day, fragment.order);
					mapOfFragments.put(key, fragment);

					// ----------------------------------------------------------
					// - Generate the dependencies
					// ----------------------------------------------------------
					if ((previousFragment.year == fragment.year)) {
						deps.append(" ");
						deps.append(fragmentsDirName + "/" + dir.getName() + "/fragment.json");

					} else {
						if (deps.length() >= 0) {
							String diaryName = Integer.toString(previousFragment.year);
							Path depsPath = Paths.get(depsDirName + "/" + diaryName + ".mk");
							try (BufferedWriter writer = Files.newBufferedWriter(depsPath)) {
								writer.write(deps.toString());
							}
						}

						String diaryName = Integer.toString(fragment.year);
						deps = new StringBuilder();
						deps.append(baseUriName + "/" + diaryName + ".html");
						deps.append(" :");
					}

					previousFragment = fragment;

				} catch (Exception e) {
					throw new Exception(dir.getCanonicalPath(), e);
				}
			}

			if (deps.length() >= 0) {
				String diaryName = Integer.toString(previousFragment.year);
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
		templates.addGeneratedFragments(mapOfFragments);

		// ----------------------------------------------------------
		// Output an HTML and PDF document for each year
		// ----------------------------------------------------------
		{
			Fragment previousFragment = new Fragment(0, 0, 0, "");

			StringBuilder html = new StringBuilder();

			for (Key key : mapOfFragments.keySet()) {
				Fragment fragment = mapOfFragments.get(key);

				if (previousFragment.year != fragment.year) {
					if (html.length() > 0) {
						generateHtmlAndPdfDocument(previousFragment.year, html.toString());
					}

					html = new StringBuilder();
				}

				html.append(fragment.html);
				previousFragment = fragment;
			}

			if (html.length() > 0) {
				generateHtmlAndPdfDocument(previousFragment.year, html.toString());
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
