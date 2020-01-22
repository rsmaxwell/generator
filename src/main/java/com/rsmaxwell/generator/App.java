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
		
		Option inputDir = Option.builder("i")
				            .longOpt("inputDir")
				            .argName("inputDir")
				            .hasArg()
				            .desc("set the input directory")
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
		options.addOption(inputDir);
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
		}

		return line;
	}

	private static void clearOutputDirectory(String relativeOutputDirName) throws IOException {
		File relativeOutputDir = new File(relativeOutputDirName);
		String outputDirName = relativeOutputDir.getCanonicalPath();
		Path outputDir = Paths.get(outputDirName);

		if (Files.exists(outputDir)) {
			Files.walk(outputDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}

		if (Files.exists(outputDir)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		Files.createDirectory(outputDir);
	}

	public static void main(String[] args) throws Exception {

		CommandLine line = getCommandLine(args);

		String inputDirName = line.getOptionValue("w", "./input");

		String outputDirName = line.getOptionValue("w", "./output");
		clearOutputDirectory(outputDirName);

		Generator generator = new Generator();
		generator.toPDF(inputDirName, outputDirName);
	}
}
