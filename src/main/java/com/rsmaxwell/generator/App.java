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
				            .hasArg()
				            .desc("set the year")
				            .build();
		
		Option url = Option.builder("u")
				            .longOpt("url")
				            .argName("url")
				            .hasArg()
				            .desc("set the url of the cgi program")
				            .build();
		
		Option inputDir = Option.builder("i")
				            .longOpt("input")
				            .argName("input dir")
				            .hasArg()
				            .desc("set the input dir")
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
		options.addOption(url);
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

	public static void main(String[] args) throws Exception {

		CommandLine line = getCommandLine(args);

		if (!line.hasOption('u')) {
			System.out.println("Missing required option -u | --url");
			return;
		}
		String url = line.getOptionValue("u", "?");

		if (!line.hasOption('i')) {
			System.out.println("Missing required option -i | --input");
			return;
		}
		String inputDirName = line.getOptionValue("i");

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

		Generator generator = new Generator(url, inputDirName, outputDirName, year);
		generator.summary();
		generator.toHtml();
	}
}
