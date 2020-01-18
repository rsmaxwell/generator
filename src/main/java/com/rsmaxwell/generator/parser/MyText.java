package com.rsmaxwell.generator.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class MyText extends MyElement {

	private List<MyNode> nodes = new ArrayList<MyNode>();

	public static MyText create(Element element, int level) throws Exception {

		MyText myText = new MyText();

		NodeList nList = element.getChildNodes();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node child = nList.item(temp);
			int nodeType = child.getNodeType();
			if (nodeType == Node.TEXT_NODE) {
				Text text = (Text) child;
				myText.nodes.add(MyTextText.create(text, level + 1));
			}
		}

		return myText;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		for (MyNode node : nodes) {
			sb.append(node.toString());
		}

		return sb.toString();
	}
}
