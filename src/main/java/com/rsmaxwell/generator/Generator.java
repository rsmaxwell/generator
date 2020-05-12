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

	private File fragmentsDirFile;
	private File outputDir;
	private File baseUri;

	private Templates templates;

	public Generator(String url, String inputDirName, String outputDirName, String year) throws Exception {

		// -------------------------------------------------------
		// Establish the input directory names
		// -------------------------------------------------------
		File inputDirFile = new File(inputDirName);
		if (!inputDirFile.exists()) {
			throw new Exception("The input director was not found: " + inputDirName);
		}

		String templatesDirName = inputDirName + "/templates";
		if (templatesDirName != null) {
			File templatesDir = new File(templatesDirName);
			if (!templatesDir.exists()) {
				throw new Exception("dir not found: " + templatesDir);
			}
			templates = new Templates(url, templatesDirName);
		}

		fragmentsDirName = inputDirName + "/fragments" + "/" + year;
		fragmentsDirFile = new File(fragmentsDirName);
		if (!fragmentsDirFile.exists()) {
			throw new Exception("Fragments dir not found: " + fragmentsDirFile.getCanonicalPath());
		}

		// -------------------------------------------------------
		// Establish directory names
		// -------------------------------------------------------
		outputDir = new File(outputDirName);
		outputDir.mkdirs();

		baseUriName = outputDirName + "/html";
		baseUri = new File(baseUriName);
		baseUri.mkdirs();
	}

	public void toHtml() throws Exception {

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
		allFragments.generateHtmlDocuments(baseUriName, new DiaryOutput() {

			@Override
			public void generate(int year, String html) throws IOException {

				String diaryName = Integer.toString(year);
				String diaryHtmlFilename = diaryName + ".html";

				String diaryHtmlPathName = baseUriName + "/" + diaryHtmlFilename;

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

	public void summary() throws IOException {
		System.out.println("Generator: " + Version.version());
		System.out.println("diaryjson: " + com.rsmaxwell.diaryjson.Version.version());
		System.out.println("Reading: " + fragmentsDirFile.getCanonicalPath());
		System.out.println("Writing: " + baseUri.getCanonicalPath());
	}
}
