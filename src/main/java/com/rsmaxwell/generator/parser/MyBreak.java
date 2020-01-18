package com.rsmaxwell.generator.parser;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class MyBreak extends MyElement {

	private enum Type {
		NORMAL, PAGE
	}

	private Type type = Type.NORMAL;

	public static MyBreak create(Element element, int level) {

		MyBreak myBreak = new MyBreak();

		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);

			if ("w:type".contentEquals(attribute.getNodeName())) {
				if ("page".contentEquals(attribute.getNodeValue())) {
					myBreak.type = Type.PAGE;
				}
			}
		}

		return myBreak;
	}

	@Override
	public String toString() {

		String eol = System.getProperty("line.separator");

		StringBuilder sb = new StringBuilder();

		switch (type) {
		case NORMAL:
			break;
		case PAGE:
			sb.append(eol);
			sb.append("--------------------------------------------------------------");
			sb.append(eol);
		}

		return sb.toString();
	}
}
