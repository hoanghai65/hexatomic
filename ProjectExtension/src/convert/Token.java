package convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Token {
	private String tok;
	private String value;
	private String label = "";
//	private String[] labels = new String[12];
	private List<String> labels = new LinkedList<>();

	public Token() {

	}

	public Token(String id, String value) {
		this.tok = id;
		this.value = value;

	}

	public Token(String id, String value, String label) {
		this.tok = id;
		this.value = value;
		this.label = label;
	}

	public Token(String id, String value, String[] labels) {
		this.tok = id;
		this.value = value;
//		this.labels = formatLabel(labels);
		this.labels = Arrays.asList(labels);
	}

	public String getTok() {
		return tok;
	}

	public void setId(String id) {
		this.tok = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
//		addMoreLabel(label);
	}

	private static String[] formatLabel(String[] labels) {

		String[] temp = new String[12];
		int index = temp.length - 1;
		for (int i = labels.length - 1; i >= 0; i--) {
			if (labels[i].trim().equals("_")) {
				continue;
			}
			temp[index--] = labels[i];
		}
		return temp;
	}

	public void addMoreLabel(String label) {
		this.labels.add(label);

	}

//	public void addMoreLabel(String lable) {
//		if(this.label.isEmpty()) {
//			labels[labels.length - 1] = lable;
//		}
//		for(int i = labels.length - 1; i >= 0; i--) {
//			if(labels[i] == null) {
//				labels[i] = lable;
//				break;
//			}
//		}
//	}
//
//	public String[] getLabels() {
//		return labels;
//	}
//
//	public void setLabels(String[] labels) {
//		this.labels = labels;
//	}
	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "tok = " + tok + " value = " + value + "label = " + label + "\n";
	}

}
