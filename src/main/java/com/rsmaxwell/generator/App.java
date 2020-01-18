package com.rsmaxwell.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {

	private static CommandLine getCommandLine(String[] args) throws ParseException {

		// @formatter:off
		Option version = Option.builder("v")
				               .longOpt("version")
				               .argName("version")
				               .desc("show program version")
				               .build();
		
		Option help = Option.builder("h")
				            .longOpt("help")
				            .argName("help")
				            .desc("show program help")
				            .build();
		
		Option file = Option.builder("f")
				            .longOpt("file")
				            .argName("file")
				            .hasArg()
				            .desc("set the input word file")
				            .build();
		
		Option year = Option.builder("y")
				            .longOpt("year")
				            .argName("year")
				            .hasArg()
				            .desc("set the year")
				            .build();
		
		Option workingDir = Option.builder("w")
	                        .longOpt("workingDir")
	                        .argName("workingDir")
	                        .hasArg()
	                        .desc("set the working directory")
	                        .build();
		
		Option outputDir = Option.builder("o")
                            .longOpt("outputDir")
                            .argName("outputDir")
                            .hasArg()
                            .desc("set the output directory")
                            .build();
		// @formatter:on

		Options options = new Options();
		options.addOption(version);
		options.addOption(help);
		options.addOption(file);
		options.addOption(year);
		options.addOption(workingDir);
		options.addOption(outputDir);

		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(options, args);

		if (line.hasOption("version")) {
			System.out.println("version:   " + Version.version());
			System.out.println("buildDate: " + Version.buildDate());
			System.out.println("gitCommit: " + Version.gitCommit());
			System.out.println("gitBranch: " + Version.gitBranch());
			System.out.println("gitURL:    " + Version.gitURL());

		} else if (line.hasOption('h')) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("generator <OPTION> ", options);
		} else if (!line.hasOption('y')) {
			System.out.println("Missing required option -y | --year");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("generator <OPTION> ", options);
		}

		return line;
	}

	private static void clearWorkingDirectory(String relativeWorkingDirName) throws IOException {
		File relativeWorkingDir = new File(relativeWorkingDirName);
		String workingDirName = relativeWorkingDir.getCanonicalPath();
		Path workingDir = Paths.get(workingDirName);

		if (Files.exists(workingDir)) {
			Files.walk(workingDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}

		if (Files.exists(workingDir)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		Files.createDirectory(workingDir);
	}

	public static void main(String[] args) throws Exception {

		CommandLine line = getCommandLine(args);

		String outputDirName = line.getOptionValue("w", "./output");

		String workingDirName = line.getOptionValue("w", "./working");
		clearWorkingDirectory(workingDirName);

		Generator generator = Generator.INSTANCE;

		String yearString = line.getOptionValue("y");
		int year = Integer.parseInt(yearString);

		String filename = line.getOptionValue("f");
		generator.unzip(filename, workingDirName);

		generator.tag = getBaseName(filename);
		String outputFileName = outputDirName + "/" + generator.tag + ".json";
		generator.toJson(workingDirName, outputFileName, year);
	}

	private static String getBaseName(String filename) {
		File file = new File(filename);
		String fileName = file.getName();
		if (fileName.indexOf(".") > 0) {
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}
		return fileName;
	}
}
