package com.rsmaxwell.generator.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rsmaxwell.generator.output.OutputDocument;

public class MyTable extends MyElement {

	private List<MyTableRow> rows = new ArrayList<MyTableRow>();

	public static MyTable create(Element element, int level) throws Exception {

		MyTable table = new MyTable();

		NodeList nList = element.getChildNodes();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node child = nList.item(temp);
			int nodeType = child.getNodeType();

			if (nodeType == Node.ELEMENT_NODE) {
				Element childElement = (Element) child;
				String nodeName = child.getNodeName();
				if ("w:tblPr".contentEquals(nodeName)) {
					// ok
				} else if ("w:tblGrid".contentEquals(nodeName)) {
					// ok
				} else if ("w:tr".contentEquals(nodeName)) {
					table.rows.add(MyTableRow.create(childElement, level + 1));
				} else {
					throw new Exception("unexpected element: " + nodeName);
				}
			}
		}

		return table;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		for (MyTableRow row : rows) {
			sb.append(row.toString());
		}

		return sb.toString();
	}

	public void toOutput(OutputDocument outputDocument) throws Exception {
		for (MyTableRow row : rows) {
			row.toOutput(outputDocument);
		}
	}
}
