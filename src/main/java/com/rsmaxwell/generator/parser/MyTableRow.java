package com.rsmaxwell.generator.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rsmaxwell.generator.Day;
import com.rsmaxwell.generator.Generator;
import com.rsmaxwell.generator.output.OutputDay;
import com.rsmaxwell.generator.output.OutputDocument;

public class MyTableRow {

	private ArrayList<MyTableColumn> columns = new ArrayList<MyTableColumn>();

	public static MyTableRow create(Element element, int level) throws Exception {

		MyTableRow tableRow = new MyTableRow();

		NodeList nList = element.getChildNodes();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node child = nList.item(temp);
			int nodeType = child.getNodeType();

			if (nodeType == Node.ELEMENT_NODE) {
				Element childElement = (Element) child;
				String nodeName = child.getNodeName();
				if ("w:tc".contentEquals(nodeName)) {
					tableRow.columns.add(MyTableColumn.create(childElement, level + 1));
				} else if ("w:tblPrEx".contentEquals(nodeName)) {
					// ok
				} else if ("w:trPr".contentEquals(nodeName)) {
					// ok
				} else {
					throw new Exception("unexpected element: " + nodeName);
				}
			}
		}

		return tableRow;
	}

	@Override
	public String toString() {

		String eol = System.getProperty("line.separator");

		// Check the the day-of-the-month
		if ((columns.size() == 3) && (columns.get(0).isDivider()) && (columns.get(2).isDivider())) {
			try {
				int dayOfMonth = columns.get(1).getDayOfMonth();
				return "---[ " + dayOfMonth + " ]------------ " + eol;
			} catch (Exception e) {
			}
		}

		// Concatenate the block of text
		StringBuilder sb1 = new StringBuilder();
		for (MyTableColumn column : columns) {
			sb1.append(column.toString());
		}
		String string = sb1.toString();

		StringBuilder sb2 = new StringBuilder();

		// Check the the day-name
		Matcher matcher = Day.getMatcher(string);
		if (matcher.find()) {
			String day = matcher.group(1);
			String text = matcher.group(2);

			sb2.append("---[ " + day + " ]---" + eol);
			sb2.append(text + eol);
		} else {
			// Otherwise it is plain text
			sb2.append(string);
		}

		return sb2.toString();
	}

	public void toOutput(OutputDocument outputDocument) throws Exception {

		// Check the the day-of-month
		if ((columns.size() == 3) && (columns.get(0).isDivider()) && (columns.get(2).isDivider())) {
			try {
				Generator.INSTANCE.day = columns.get(1).getDayOfMonth();

				OutputDay outputDay = new OutputDay();
				outputDay.day = Generator.INSTANCE.day;
				outputDay.month = Generator.INSTANCE.month;
				outputDay.year = Generator.INSTANCE.year;
				outputDay.tag = Generator.INSTANCE.tag;

				outputDocument.days.add(outputDay);
				return;

			} catch (Exception e) {
			}
		}

		// Check there is at least one day in the given outputMonth
		int size = outputDocument.days.size();
		if (size <= 0) {
			throw new Exception("The month does not contain any days");
		}

		// Check there are exactly 2 columns in this row
		if (columns.size() != 2) {
			return;
		}

		// Concatenate the block of lines (from column 0) and notes (from column 1)
		List<String> lines = new ArrayList<String>();
		List<String> notes = new ArrayList<String>();

		lines.addAll(columns.get(0).toList());
		notes.addAll(columns.get(1).toList());

		// Check there is at least one line
		if (lines.size() <= 0) {
			return;
		}

		// If the first line starts with the day-name, remove it
		String line = lines.get(0);
		Matcher matcher = Day.getMatcher(line);
		if (matcher.find()) {

			String string = matcher.group(1);
			int dayOfWeek = Day.toInt(string);
			Day.check(Generator.INSTANCE.year, Generator.INSTANCE.month, Generator.INSTANCE.day, dayOfWeek);

			lines.set(0, matcher.group(2));
		}

		OutputDay outputDay = outputDocument.days.get(size - 1);

		outputDay.line = buildLine(lines);
		outputDay.notes = buildLine(notes);
	}

	private String buildLine(List<String> array) throws Exception {
		String line = null;
		String separator = " ";
		for (int i = 0; i < array.size(); i++) {
			String next = array.get(i);
			if (next.length() == 0) {
				separator = "</p><p>";
			} else {
				line = join(line, next, separator);
				separator = " ";
			}
		}
		return (line == null) ? null : "<p>" + line + "</p>";
	}

	private String join(String one, String two, String separator) throws Exception {
		if (one == null) {
			return two.trim();
		}

		return one.trim() + separator + two.trim();
	}
}
