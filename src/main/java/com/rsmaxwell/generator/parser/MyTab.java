package com.rsmaxwell.generator.parser;

import org.w3c.dom.Element;

public class MyTab extends MyElement {

	public static MyTab create(Element element, int level) throws Exception {
		MyTab myTab = new MyTab();
		return myTab;
	}

	@Override
	public String toString() {
		return "    ";
	}
}
