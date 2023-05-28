package convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertToPropbank {
	private static ArrayList<ArrayList<Token>> listToken = new ArrayList<>();
	private static ArrayList<ArrayList<Token>> listSpan = new ArrayList<>();
	private static int FILLPRED = 0;

	private static ArrayList<ArrayList<Token>> getArrayListToken(BufferedReader br) throws IOException {
		String tk = "";
		Pattern patternTok = Pattern.compile("\"([^\"]*)\"");
		Pattern patternLetter = Pattern.compile(">([^<]*)<");
		ArrayList<ArrayList<Token>> listTokenTemp = new ArrayList<>();
		while ((tk = br.readLine()) != null) {
			Matcher matcherTok = patternTok.matcher(tk);
			Matcher matcherLetter = patternLetter.matcher(tk);
			String letter = "";
			ArrayList<Token> event = new ArrayList<>();
			while (matcherTok.find()) {
				String tok = matcherTok.group(1);
				if (matcherLetter.find()) {
					letter = matcherLetter.group(1);
				}
				Token token = new Token(tok, letter);
				event.add(token);
			}
//			System.out.println(event);
			listTokenTemp.add(event);
			if (tk.contains("/tier")) {
				break;
			}
		}
		return listTokenTemp;
	}

	private static ArrayList<ArrayList<Token>> getArrayListSpan(BufferedReader br) throws IOException {
		String tk = "";
		Pattern patternTok = Pattern.compile("\"([^\"]*)\"");
		Pattern patternLetter = Pattern.compile(">([^<]*)<");
		ArrayList<ArrayList<Token>> listSpan = new ArrayList<>();
		while ((tk = br.readLine()) != null) {
			Matcher matcherTok = patternTok.matcher(tk);
			Matcher matcherLetter = patternLetter.matcher(tk);
			String label = "";
			ArrayList<Token> event = new ArrayList<>();
			while (matcherTok.find()) {
				String tok = matcherTok.group(1);
				if (matcherLetter.find()) {
					label = matcherLetter.group(1);
				}
				Token token = new Token(tok, label, label);
				event.add(token);

			}
			listSpan.add(event);
			if (tk.contains("/tier")) {
				break;
			}
		}
		return listSpan;
	}

	private static void readFile(String fileName) {
		try {
			File file = new File(fileName);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st = "";
			while ((st = br.readLine()) != null) {
				if (st.contains("category=\"tok\"")) {
					listToken = getArrayListToken(br);
				}
				if (st.contains("category=\"cat\"")) {

					listSpan = getArrayListSpan(br);
//					System.out.println(listSpan);
				}

			}
			br.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private static ArrayList<Token> convertToConll2009() {
		ArrayList<Token> conllList = new ArrayList<>();

		for (int j = 0; j < listSpan.size() - 1; j++) {
			String startSpan = listSpan.get(j).get(0).getTok();
			String endSpan = listSpan.get(j).get(1).getTok();
			String valueSpan = listSpan.get(j).get(1).getValue();
			int indexEnd = 0;
			int indexStart = 0;
			int i = 0;
			while (i < listToken.size() - 1) {
				String startToken = listToken.get(i).get(0).getTok();
				String endToken = listToken.get(i).get(1).getTok();
				if (startSpan.equals(startToken)) {
					if (listToken.get(i).get(0).getLabel().isEmpty()) {
						listToken.get(i).get(0).addMoreLabel(valueSpan);
					} else {
						listToken.get(i).get(0).addMoreLabel(valueSpan);
						if (valueSpan.toLowerCase().equals("y")) {
							FILLPRED++;
						}
					}
					indexStart = i;
				} else if (endSpan.equals(endToken)) {
					if (listToken.get(i).get(0).getLabel().isEmpty()) {
						listToken.get(i).get(0).addMoreLabel(valueSpan);
					} else {
						listToken.get(i).get(0).addMoreLabel(valueSpan);
						if (valueSpan.toLowerCase().equals("y")) {
							FILLPRED++;
						}
					}
					indexEnd = i;
				}
				i++;
			}
			for (int k = indexStart + 1; k < indexEnd; k++) {
				listToken.get(k).get(0).addMoreLabel(valueSpan);
			}

		}
		for (int i = 0; i < listToken.size() - 1; i++) {
			conllList.add(listToken.get(i).get(0));
		}
		System.out.println(conllList);
		return conllList;

	}

	private static void writeFile(ArrayList<Token> listToken, String fileName) {
		try {
			Formatter formatter = new Formatter(new File(fileName));
			int countY = getCountY(listToken);
			for (int i = 0; i < listToken.size(); i++) {
				String value = listToken.get(i).getValue();
				List<String> labels = listToken.get(i).getLabels();
				String label = formatLabel(labels, countY+1);
				System.out.println(labels.toString() + label);

				formatter.format("%d\t%s%s\n", i+1,value, label);
			}
		

			formatter.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}

	}
	private static int getCountY(ArrayList<Token> listToken) {
		int countY = 0;
		for (int i = 0; i < listToken.size(); i++) {
			String value = listToken.get(i).getValue();
			List<String> labels = listToken.get(i).getLabels();
			Collections.reverse(labels);
			for (String l : labels) {
				if (l.toLowerCase().equals("y")) {
					countY++;
				}
			}

		}
		return countY;
	}

	private static String formatLabel(List<String> labels, int countY) {
		int length = countY + 12;
		System.out.println(countY);
		String newLabel = "";
		int indexY = labels.indexOf("Y");
		int count = 0;
		if (indexY != -1) {
			for (int i = 1; i < length; i++) {
				if (i < 12 - indexY - 1) {
					newLabel += "\t" + "_";
				} else {
					if (labels.size() > count) {
						newLabel += "\t" + labels.get(count++);
					} else {
						newLabel += "\t" + "_";
					}
				}
			}
		}else {
			for (int i = 1; i < length; i++) {
				if (i < 13) {
					newLabel += "\t" + "_";
				} else {
					if (labels.size() > count) {
						newLabel += "\t" + labels.get(count++);
					} else {
						newLabel += "\t" + "_";
					}
				}
			}
		}

		return newLabel;
	}
	
	public static void main(String[] args) throws Exception {
		readFile("src/document_3.exb");
		writeFile(convertToConll2009(), "src/probank1.txt");
	}
}
