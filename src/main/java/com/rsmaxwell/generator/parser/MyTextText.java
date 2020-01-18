package com.rsmaxwell.generator.parser;

import org.w3c.dom.Text;

public class MyTextText implements MyNode {

	private String string;

	public static MyTextText create(Text text, int level) throws Exception {

		MyTextText myTextText = new MyTextText();

		String nodeName = text.getNodeName();
		if ("#text".contentEquals(nodeName)) {
			// ok
		} else {
			throw new Exception("unexpected element: " + nodeName);
		}

		myTextText.string = text.getData();

		return myTextText;
	}

	@Override
	public String toString() {

		string = string.replace('\u2019', '\'');
		string = string.replace('\u201C', '"');
		string = string.replace('\u201D', '"');
		string = string.replace("\u2026", "...");

		return string;
	}
}
