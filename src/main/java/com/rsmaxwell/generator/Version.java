package com.rsmaxwell.generator;

public class Version {

	// version function
	public static String version() {
		return "<GENERATOR_VERSION>";
	}

	// buildDate function
	public static String buildDate() {
		return "<BUILD_DATE>";
	}

	// gitCommit function
	public static String gitCommit() {
		return "<GIT_COMMIT>";
	}

	// gitBranch function
	public static String gitBranch() {
		return "<GIT_BRANCH>";
	}

	// gitURL function
	public static String gitURL() {
		return "<GIT_URL>";
	}
}
