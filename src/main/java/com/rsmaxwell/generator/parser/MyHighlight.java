package com.rsmaxwell.generator.parser;

import org.w3c.dom.Element;

public class MyHighlight extends MyElement {

	private String highlight;

	public static MyHighlight create(Element element, int level) throws Exception {

		MyHighlight myHighlight = new MyHighlight();

		String value = element.getAttribute("w:val");
		if (value != null) {
			if (value.length() > 0) {
				myHighlight.highlight = value;
			}
		}

		return myHighlight;
	}

	@Override
	public String getHighlight() {
		return highlight;
	}
}
