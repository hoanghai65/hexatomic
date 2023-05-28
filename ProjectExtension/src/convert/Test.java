package convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

public class Test {
	private static List<HashMap<String, String>> dependency = new ArrayList<>();

	private static void writetLi(ArrayList<Token> listToken, Formatter formatter) {
		for (int i = 0; i < listToken.size(); i++) {
			String tli = String.format("<tli id=\"%s\" time=\"%d\"/>", listToken.get(i).getTok(), i);
			formatter.format("%s\n", tli);
//			System.out.println(tli);
		}
	}

	private static void writeEvenToken(ArrayList<Token> listToken, Formatter formatter) {
		for (int i = 0; i < listToken.size(); i++) {
			String event = String.format("<event start=\"%s\" end=\"%s\">%s</event>", listToken.get(i).getTok(),
					"T" + (i + 1), listToken.get(i).getValue());
			formatter.format("%s\n", event);
//			System.out.println(event);
		}
	}

	private static void writeText(ArrayList<Token> listToken, String inputFile, String outputFile) {
		String text = "";
		for (Token token : listToken) {
			text += token.getValue() + " ";
		}
		try {
			File file = new File(inputFile);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			Formatter formatter = new Formatter(new File(outputFile));
			while ((line = br.readLine()) != null) {
				if (line.contains("<body>")) {
					formatter.format("%s", line);
					formatter.format("%s", text.trim());
				} else {
					formatter.format("%s\n", line);
				}
			}
			formatter.close();
			br.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private static void writeToken(ArrayList<Token> listToken, String inputFile, String outputFile) {
		try {
			int startPoint = 1;
			int endPoint = 1;
			File file = new File(inputFile);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			Formatter formatter = new Formatter(new File(outputFile));
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				formatter.format("%s\n", line);
				if (line.contains("<markList")) {
					for (int i = 0; i < listToken.size(); i++) {
						String tok = listToken.get(i).getValue();
						endPoint = tok.length();
						String comment = String.format("<!-- %s -->", tok);
						String mark = String.format(
								"<mark id=\"%s\" xlink:href=\"#xpointer(string-range(//body,'',%d,%d))\"/>",
								"t" + (i + 1), startPoint, endPoint);
						startPoint += endPoint + 1;
						formatter.format("%s\n%s\n", comment, mark);
					}
				}
			}
			formatter.close();
			br.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private static void writeSpan(ArrayList<Token> listToken, String inputFile, String outputFile) {
		try {
			File file = new File(inputFile);
			BufferedReader br = new BufferedReader(new FileReader(file));
			Formatter formatter = new Formatter(new File(outputFile));
			String labelStart = "";
			String labelEnd = "";
			String line = "";
			String commentValue = "";
			int spanId = 1;
			int size = listToken.get(0).getLabels().size();
			String tokenSpan = "";
			while ((line = br.readLine()) != null) {
				formatter.format("%s\n", line);
				if (line.contains("<markList")) {
					for (int l = size - 1; l >= 0; l--) {
						HashMap<String, String> dependencyValue = new HashMap<>();
						System.out.println("--------------------");
						for (int i = 0; i < listToken.size() - 1; i++) {
							if (listToken.get(i).getLabels().get(l) != null
									&& !listToken.get(i).getLabels().get(l).equals("_")) {
								labelStart = listToken.get(i).getLabels().get(l);
								labelEnd = listToken.get(i + 1).getLabels().get(l);
								if (labelStart.toLowerCase().equals("y") || l == 11) {
									continue;
								}
//								System.out.println("hai" + labelStart + " " + labelEnd);
								if (labelStart.equals(labelEnd)) {
									tokenSpan += String.format("#t%d ", i + 1);
									commentValue += listToken.get(i).getValue() + " ";
								} else {
									tokenSpan += String.format("#t%d ", i + 1);
									commentValue += listToken.get(i).getValue() + " ";
									String spanText = String.format("span%d_%s",spanId, labelStart.replace("-", "_"));
									String comment = String.format("<!-- %s -->", commentValue);
									String span = String.format("<mark id=\"%s\" xlink:href=\"%s\"/>", spanText,
											tokenSpan.trim());
									formatter.format("%s\n%s\n", comment, span);
									spanId++;
									tokenSpan = "";
									commentValue = "";
									dependencyValue.put(spanText, tokenSpan);
								}
							}
						}
						dependency.add(dependencyValue);
					}
				}
			}

			formatter.close();
			br.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private static void writeNote(ArrayList<Token> listToken, String inputFile, String outputFile) {
		try {
			File file = new File(inputFile);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			int index = 0;
			Formatter formatter = new Formatter(new File(outputFile));
			while ((line = br.readLine()) != null) {
				formatter.format("%s", line);
				if (line.contains("<structList")) {
					for (int i = listToken.size() - 1; i >= 0; i--) {
						List<String> labels = listToken.get(i).getLabels();
						if (labels.size() > 11 && !labels.get(11).equals("_")) {
							HashMap<String, String> dependencyValue = dependency.get(index);
							writeNoteValue(formatter, dependencyValue, labels.get(11), listToken.get(i+1).getTok());
							index++;
						}
					}
				}
			}
			formatter.close();
			br.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private static void writeNoteValue(Formatter formatter, HashMap<String, String> dependencyValue, String pred,
			String tokValue) {
		int relId = 1;
		String begin = String.format("<struct id=\"%s\">\n", pred);
		String struct = "";
		for (String value : dependencyValue.keySet()) {
			struct += String.format("<rel id=\"sDomRel%d\" xlink:href=\"no_layer.document_1.mark.xml#%s\"/>\n", relId,
					value);
			relId++;
		}
		struct += String.format("<rel id=\"sDomRel%d\" xlink:href=\"document_1.tok.xml#%s\"/>\n", relId,
				tokValue.toLowerCase());
		String end = "</struct>";
		String noteResult = begin + struct + end;
		formatter.format("%s\n", noteResult);
	}

	private static ArrayList<Token> readFile(String fileName) {
		ArrayList<Token> listToken = new ArrayList<>();
		try {
			File file = new File(fileName);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String lines = "";
			int index = 0;
			while ((lines = br.readLine()) != null) {
				if (lines.length() == 0) {
					continue;
				}
				String[] line = lines.trim().split("\t");
				if (line[0].matches("-?\\d+(\\.\\d+)?")) {
					index = Integer.parseInt(line[0]);
				} else
					index++;

				String id = "T" + (index - 1);
				Token token = null;
				token = new Token(id, line[1], Arrays.copyOfRange(line, 2, line.length));
				if (token != null) {
					listToken.add(token);
				}

//				print(token.getLabels());
//				System.out.println();
			}
			br.close();
			return listToken;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return listToken;
	}
//
//	private static void print(List<String> arr) {
//		for (String a : arr) {
//			System.out.print(a + " ");
//		}
//	}

	private static void writeFileExb(String inputFile) {

		ArrayList<Token> listToken = readFile(inputFile);
		writeToken(listToken, "src/convert/base/document_1.tok.xml", "src/convert/document/document_1.tok.xml");
		writeText(listToken, "src/convert/base/document_1.text.xml", "src/convert/document/document_1.text.xml");
		writeSpan(listToken, "src/convert/base/no_layer.document_1.mark.xml",
				"src/convert/document/no_layer.document_1.mark.xml");
		writeNote(listToken, "src/convert/base/no_layer.document_1.struct.xml",
				"src/convert/document/no_layer.document_1.struct.xml");
	}

	public static void main(String[] args) {
		String inputFile = "src/probank.txt";
		writeFileExb(inputFile);
	}
}