package com.rsmaxwell.generator;

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
		
		Option year = Option.builder("y")
				            .longOpt("year")
				            .argName("year")
				            .desc("set the year")
				            .build();
		
		Option templatesDir = Option.builder("t")
				            .longOpt("templates")
				            .argName("templates dir")
				            .hasArg()
				            .desc("set the templates dir")
				            .build();

		Option outputDir = Option.builder("o")
                            .longOpt("output")
                            .argName("output dir")
                            .hasArg()
                            .desc("set the output dir")
                            .build();
		// @formatter:on

		Options options = new Options();
		options.addOption(version);
		options.addOption(help);
		options.addOption(year);
		options.addOption(templatesDir);
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

	public static void main(String[] args) throws Exception {

		CommandLine line = getCommandLine(args);

		if (!line.hasOption('t')) {
			System.out.println("Missing required option -t | --templates");
			return;
		}
		String templatesDirName = line.getOptionValue("t", "templates");

		if (!line.hasOption('o')) {
			System.out.println("Missing required option -o | --output");
			return;
		}
		String outputDirName = line.getOptionValue("o", "output");

		if (!line.hasOption('y')) {
			System.out.println("Missing required option -y | --year");
			return;
		}
		String year = line.getOptionValue("y");

		Generator generator = new Generator(templatesDirName, outputDirName, year);
		generator.toPDF();
	}
}
