package com.rsmaxwell.generator.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rsmaxwell.generator.Generator;
import com.rsmaxwell.generator.Month;
import com.rsmaxwell.generator.output.OutputDocument;

public class MyBody {

	private List<MyElement> elements = new ArrayList<MyElement>();

	public static MyBody create(Element element, int level) throws Exception {

		MyBody body = new MyBody();

		NodeList nList = element.getChildNodes();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node child = nList.item(temp);
			int nodeType = child.getNodeType();

			if (nodeType == Node.ELEMENT_NODE) {
				Element childElement = (Element) child;
				String nodeName = child.getNodeName();
				if ("w:p".contentEquals(nodeName)) {
					body.elements.add(MyParagraph.create(childElement, level + 1));
				} else if ("w:tbl".contentEquals(nodeName)) {
					body.elements.add(MyTable.create(childElement, level + 1));
				} else if ("w:sectPr".contentEquals(nodeName)) {
					// ok
				} else {
					throw new Exception("unexpected element: " + nodeName);
				}
			}
		}

		return body;
	}

	@Override
	public String toString() {

		String eol = System.getProperty("line.separator");

		StringBuilder sb = new StringBuilder();

		for (MyElement element : elements) {

			if (element instanceof MyParagraph) {

				String string = element.toString();
				try {
					Generator.INSTANCE.month = Month.toInt(string);

					sb.append("---[ ");
					sb.append(string);
					sb.append(" ]------------------------------------------------------------------------");
					sb.append(eol);
				} catch (Exception e) {
				}

			} else {
				sb.append(element.toString());
			}
		}

		return sb.toString();
	}

	public void toOutput(OutputDocument outputDocument) throws Exception {

		for (MyElement element : elements) {

			if (element instanceof MyParagraph) {

				String string = element.toString();
				try {
					Generator.INSTANCE.month = Month.toInt(string);

				} catch (Exception e) {
				}

			} else if (element instanceof MyTable) {
				MyTable myTable = (MyTable) element;
				myTable.toOutput(outputDocument);
			} else {
				throw new Exception("Unexpected element type: " + element.getClass().getSimpleName());
			}
		}

	}
}
