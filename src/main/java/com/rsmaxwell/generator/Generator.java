package com.rsmaxwell.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

		outputDir = new File(outputDirName);
		outputDir.mkdirs();

		if (templatesDirName != null) {
			File templatesDir = new File(templatesDirName);
			if (!templatesDir.exists()) {
				throw new Exception("dir not found: " + templatesDir);
			}
			templates = new Templates(url, templatesDirName);
		}

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
		File[] monthDirs = fragmentsDirFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File file = new File(dir, name);
				return file.isDirectory();
			}
		});

		FragmentList allFragments = new FragmentList();

		for (File monthDir : monthDirs) {

			File[] fragments = monthDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					File file = new File(dir, name);
					return file.isDirectory();
				}
			});

			for (File fragmentDir : fragments) {
				try {
					Fragment fragment = Fragment.readFromFile(fragmentDir.getCanonicalPath());
					fragment.check();
					allFragments.add(fragment);

				} catch (Exception e) {
					throw new Exception("fragmentDirName: " + fragmentDir.getCanonicalPath(), e);
				}
			}
		}

		// -------------------------------------------------------
		// Add generated fragments
		// (Use an intermediate list to avoid adding to the main
		// collection as we are traversing it)
		// -------------------------------------------------------
		if (templates != null) {
			templates.addGeneratedFragments(allFragments);
		}

		// ----------------------------------------------------------
		// Output the HTML document for the year
		// ----------------------------------------------------------
		allFragments.generateHtmlDocuments(baseUriName, pdfDirName, new DiaryOutput() {

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
			}
		});
	}
}
