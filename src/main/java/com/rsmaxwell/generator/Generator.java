package com.rsmaxwell.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.rsmaxwell.diaryjson.Templates;
import com.rsmaxwell.diaryjson.fragment.Fragment;
import com.rsmaxwell.diaryjson.template.DiaryOutput;
import com.rsmaxwell.diaryjson.template.FragmentList;

public class Generator {

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

	public Generator(String url, String templatesDirName, String outputDirName, String year) throws Exception {

		System.out.println("** Generator: url: " + url);

		outputDir = new File(outputDirName);
		outputDir.mkdirs();

		File templatesDir = new File(templatesDirName);
		if (!templatesDir.exists()) {
			throw new Exception("dir not found: " + templatesDir);
		}
		templates = new Templates(url, templatesDirName);

		// -------------------------------------------------------
		// Establish directory names
		// -------------------------------------------------------
		fragmentsDirName = outputDirName + "/fragments" + "/" + year;
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
		String[] fragmentNames = fragmentsDirFile.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File file = new File(dir, name);
				return file.isDirectory();
			}
		});

		// Check there is at least one fragment directory!
		if (fragmentNames.length <= 0) {
			throw new Exception("no fragments found in: " + fragmentsDirName);
		}

		// ----------------------------------------------------------
		// - Read the fragments from file
		// ----------------------------------------------------------
		StringBuilder deps = new StringBuilder();

		FragmentList fragments = new FragmentList();

		{
			Fragment previousFragment = new Fragment(0, 0, 0, "");

			for (String fragmentName : fragmentNames) {
				String fragmentDirName = fragmentsDirName + "/" + fragmentName;

				try {
					// ----------------------------------------------------------
					// - Read the fragment from file and add to the list
					// ----------------------------------------------------------
					Fragment fragment = Fragment.readFromFile(fragmentDirName);
					fragment.check();

					fragments.add(fragment);

					// ----------------------------------------------------------
					// - Write out the makefile dependencies for next time round
					// ----------------------------------------------------------
					if ((previousFragment.year == fragment.year)) {
						deps.append(" ");
						deps.append(fragmentDirName + "/fragment.json");

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
					throw new Exception("fragmentDirName: " + fragmentDirName, e);
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
		templates.addGeneratedFragments(fragments);

		// ----------------------------------------------------------
		// Output an HTML and PDF document for each year
		// ----------------------------------------------------------
		fragments.generateHtmlAndPdfDocuments(baseUriName, pdfDirName, new DiaryOutput() {

			@Override
			public void generate(int year, String html) throws IOException {

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
		});
	}
}
