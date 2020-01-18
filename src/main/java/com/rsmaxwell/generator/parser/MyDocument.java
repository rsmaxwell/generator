package com.rsmaxwell.generator.parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.rsmaxwell.generator.output.OutputDocument;

public class MyDocument {

	private MyBody body;

	public static MyDocument create(Element element, int level) throws Exception {

		MyDocument document = new MyDocument();

		NodeList children = element.getElementsByTagName("w:body");

		if (children.getLength() < 1) {
			throw new Exception("the [w:body] element could not be not found");
		} else if (children.getLength() > 1) {
			throw new Exception("too many [w:body] elements");
		}

		Element child = (Element) children.item(0);
		document.body = MyBody.create(child, level + 1);

		return document;
	}

	@Override
	public String toString() {
		return body.toString();
	}

	public OutputDocument toOutput() throws Exception {

		OutputDocument outputDocument = new OutputDocument();

		body.toOutput(outputDocument);

		return outputDocument;
	}
}
