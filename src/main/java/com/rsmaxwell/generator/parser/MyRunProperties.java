package com.rsmaxwell.generator.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MyRunProperties extends MyElement {

	private List<MyElement> elements = new ArrayList<MyElement>();

	public static MyRunProperties create(Element element, int level) throws Exception {

		MyRunProperties runProperties = new MyRunProperties();

		NodeList nList = element.getChildNodes();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node child = nList.item(temp);
			int nodeType = child.getNodeType();

			if (nodeType == Node.ELEMENT_NODE) {
				Element childElement = (Element) child;
				String nodeName = child.getNodeName();
				if ("w:highlight".contentEquals(nodeName)) {
					runProperties.elements.add(MyHighlight.create(childElement, level + 1));
				} else if ("w:vertAlign".contentEquals(nodeName)) {
					// ok
				} else if ("w:rFonts".contentEquals(nodeName)) {
					// ok
				} else if ("w:sz".contentEquals(nodeName)) {
					// ok
				} else if ("w:szCs".contentEquals(nodeName)) {
					// ok
				} else if ("w:b".contentEquals(nodeName)) {
					// ok
				} else if ("w:u".contentEquals(nodeName)) {
					// ok
				} else if ("w:color".contentEquals(nodeName)) {
					// ok
				} else if ("w:shd".contentEquals(nodeName)) {
					// ok
				} else {
					throw new Exception("unexpected element: " + nodeName);
				}
			}
		}

		return runProperties;
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public String getHighlight() {
		for (MyElement element : elements) {
			String highlight = element.getHighlight();
			if (highlight != null) {
				return highlight;
			}
		}
		return null;
	}
}
