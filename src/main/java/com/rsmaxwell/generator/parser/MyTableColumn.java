package com.rsmaxwell.generator.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MyTableColumn {

	private List<MyElement> elements = new ArrayList<MyElement>();

	public static MyTableColumn create(Element element, int level) throws Exception {

		MyTableColumn tableColumn = new MyTableColumn();

		NodeList nList = element.getChildNodes();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node child = nList.item(temp);
			int nodeType = child.getNodeType();

			if (nodeType == Node.ELEMENT_NODE) {
				Element childElement = (Element) child;
				String nodeName = child.getNodeName();

				if ("w:tcPr".contentEquals(nodeName)) {
					// ok
				} else if ("w:p".contentEquals(nodeName)) {
					tableColumn.elements.add(MyParagraph.create(childElement, level + 1));
				} else {
					throw new Exception("unexpected element: " + nodeName);
				}
			}
		}

		return tableColumn;
	}

	public boolean isDivider() {
		return (toString().length() == 0);
	}

	public int getDayOfMonth() throws Exception {

		String string = toString();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if (!Character.isDigit(ch)) {
				break;
			}
			sb.append(ch);
		}

		String strNum = sb.toString();

		if ((strNum == null) || (strNum.length() == 0)) {
			throw new Exception("not a day-of-week: " + string);
		}

		return Integer.parseInt(strNum);
	}

	@Override
	public String toString() {

		String separator = "";
		StringBuilder sb = new StringBuilder();
		for (MyElement element : elements) {

			if (element instanceof MyParagraph) {
				sb.append(separator);
				separator = System.getProperty("line.separator");
			}
			sb.append(element.toString());
		}

		return sb.toString();
	}

	public List<String> toList() {

		List<String> lines = new ArrayList<String>();

		int paragraphCount = 0;
		StringBuilder sb = new StringBuilder();
		for (MyElement element : elements) {

			if (element instanceof MyParagraph) {
				if (paragraphCount > 0) {
					lines.add(sb.toString());
					sb.setLength(0);
				}
				paragraphCount++;
			}
			sb.append(element.toString());
		}
		if (sb.length() > 0) {
			lines.add(sb.toString());
		}

		return lines;
	}
}
