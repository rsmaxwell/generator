package com.rsmaxwell.generator.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MyParagraph extends MyElement {

	private List<MyRun> runs = new ArrayList<MyRun>();

	public static MyParagraph create(Element element, int level) throws Exception {

		MyParagraph paragraph = new MyParagraph();

		NodeList nList = element.getChildNodes();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node child = nList.item(temp);
			int nodeType = child.getNodeType();

			if (nodeType == Node.ELEMENT_NODE) {
				Element childElement = (Element) child;
				String nodeName = child.getNodeName();
				if ("w:r".contentEquals(nodeName)) {
					paragraph.runs.add(MyRun.create(childElement, level + 1));
				} else if ("w:pPr".contentEquals(nodeName)) {
					// ok
				} else if ("w:proofErr".contentEquals(nodeName)) {
					// ok
				} else if ("w:bookmarkStart".contentEquals(nodeName)) {
					// ok
				} else if ("w:bookmarkEnd".contentEquals(nodeName)) {
					// ok
				} else {
					throw new Exception("unexpected element: " + nodeName);
				}
			}
		}

		return paragraph;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		for (MyRun run : runs) {
			sb.append(run.toString());
		}

		return sb.toString();
	}
}
