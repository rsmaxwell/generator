package com.rsmaxwell.generator.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class OutputDay {

	public int year;
	public int month;
	public int day;
	public String tag;
	public String line;

	@JsonInclude(Include.NON_EMPTY)
	public int continuation;

	@JsonInclude(Include.NON_EMPTY)
	public String notes;

}
