package enshud.s1.lexer;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class Lexer {
	
	private final Map<String, Integer> TokenID = new HashMap<String, Integer>() {
		{
			put("and", 0);
			put("array", 1);
			put("begin", 2);
			put("boolean", 3);
			put("char", 4);
			put("div", 5);	// "div"と"/"は同じ意味を持つ。
			put("/", 5);	// "div"と"/"は同じ意味を持つ。
			put("do", 6);
			put("else", 7);
			put("end", 8);
			put("false", 9);
			put("if", 10);
			put("integer", 11);
			put("mod", 12);
			put("not", 13);
			put("of", 14);
			put("or", 15);
			put("procedure", 16);
			put("program", 17);
			put("readln", 18);
			put("then", 19);
			put("true", 20);
			put("var", 21);
			put("while", 22);
			put("writeln", 23);
			put("=", 24);
			put("<>", 25);
			put("<", 26);
			put("<=", 27);
			put(">=", 28);
			put(">", 29);
			put("+", 30);
			put("-", 31);
			put("*", 32);
			put("(", 33);
			put(")", 34);
			put("[", 35);
			put("]", 36);
			put(";", 37);
			put(":", 38);
			put("..", 39);
			put(":= ", 40);
			put(",", 41);
			put(".", 42);
		}
	};
	private final String[] tokenName = { "SAND", "SARRAY", "SBEGIN", "SBOOLEAN", "SCHAR", "SDIVD", "SDO", "SELSE",
									"SEND", "SFALSE", "SIF", "SINTEGER", "SMOD", "SNOT", "SOF", "SOR", "SPROCEDURE",
									"SPROGRAM", "SREADLN", "STHEN", "STRUE", "SVAR", "SWHILE", "SWRITELN", "SEQUAL",
									"SNOTEQUAL", "SLESS", "SLESSEQUAL", "SGREATEQUAL", "SGREAT", "SPLUS", "SMINUS",
									"SSTAR", "SLPAREN", "SRPAREN", "SLBRACKET", "SRBRACKET", "SSEMICOLON", "SCOLON",
									"SRANGE", "SASSIGN", "SCOMMA", "SDOT", "SIDENTIFIER", "SCONSTANT", "SSTRING", "ERROR" };
	private final Set<Character> signSet = new HashSet<Character>(Arrays.asList('=', '<', '>', '+', '-', '*', '/', '(', ')', '{', '}', '[', ']', ';', ':', ',', '.'));
	
	private List<String> outputStringList = new ArrayList<String>();
	private List<String> programString;
	private int programStringPointer;
	private int numOfLines;
	
	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// normalの確認
		new Lexer().run("data/pas/normal01.pas", "tmp/out1.ts");
		new Lexer().run("data/pas/normal02.pas", "tmp/out2.ts");
		new Lexer().run("data/pas/normal03.pas", "tmp/out3.ts");
	}

	/**
	 * TODO
	 * 
	 * 開発対象となるLexer実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたpasファイルを読み込み，トークン列に分割する．
	 * トークン列は第二引数で指定されたtsファイルに書き出すこと．
	 * 正常に処理が終了した場合は標準出力に"OK"を，
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力pasファイル名
	 * @param outputFileName 出力tsファイル名
	 */
	public void run(final String inputFileName, final String outputFileName) {

		// TODO
		try {
			programString = Files.readAllLines(Paths.get(inputFileName));
            numOfLines = 0;
            programStringPointer = 0;
            while (numOfLines < programString.size()) {
	            while (programStringPointer < programString.get(numOfLines).length()) {
	            	if (programString.get(numOfLines).charAt(programStringPointer) == ':') {
	            		existEqual(':', 40, 38);
	            	} else if (programString.get(numOfLines).charAt(programStringPointer) == '<') {
	            		existEqual('<', 27, 26);
	            	} else if (programString.get(numOfLines).charAt(programStringPointer) == '>') {
	            		existEqual('>', 28, 29);
	            	} else if (programString.get(numOfLines).charAt(programStringPointer) == '.') {
	            		if (programStringPointer + 1 == programString.get(numOfLines).length()) {
	            			printFormat(".", 42);
	            		} else if (programString.get(numOfLines).charAt(programStringPointer + 1) == '.') {
	            			printFormat("..", 39);
	            			programStringPointer++;
	            		} else {
	            			printFormat(".", 42);
	            		}
	            	} else if (programString.get(numOfLines).charAt(programStringPointer) == '\'') {
	            		makeString();
	            	} else if (programString.get(numOfLines).charAt(programStringPointer) == '{') {
	            		while (programString.get(numOfLines).charAt(programStringPointer) != '}') {
	            			programStringPointer++;
	            			if (programStringPointer == programString.get(numOfLines).length()) {
	            				printFormat("closing_brace_missing", 46);
	            				break;
	            			}
	            		}
	            	} else if (signSet.contains(programString.get(numOfLines).charAt(programStringPointer))) {
	            		printFormat(String.valueOf(programString.get(numOfLines).charAt(programStringPointer)), TokenID.get(String.valueOf(programString.get(numOfLines).charAt(programStringPointer))));
	            	} else if (isAlphabet(programString.get(numOfLines).charAt(programStringPointer))) {
	            		makeReservOrIdent();
	            	} else if (isNumber(programString.get(numOfLines).charAt(programStringPointer))) {
	            		makeNumberNoSign();
	            	} else if (!isOKChar(programString.get(numOfLines).charAt(programStringPointer))) {
	            		while (!isOKChar(programString.get(numOfLines).charAt(programStringPointer))) {
	            			programStringPointer++;
	            			if (programStringPointer == programString.get(numOfLines).length()) {
	            				break;
	            			}
	            		}
	            		printFormat("not_exist_token", 46);
	            		programStringPointer--;
	            	}
	            	programStringPointer++;
	            }
	            programStringPointer = 0;
	            numOfLines++;
            }
        	Files.write(Paths.get(outputFileName), outputStringList);
            System.out.println("OK");
        } catch (IOException e) {
        	System.err.println("File not found");
        }
	}
	/**
	 * 出力しつつIDリストに追加(実際は1つ前の要素のみ見れればよいが、拡張性を考慮してすべて保存している。)
	 * @param outputString 出力する文字列
	 * @param 
	 * @param id
	 */
	private void printFormat(String outputString, int id) {
		outputStringList.add(outputString + "\t" + tokenName[id] + "\t" + id + "\t" + (numOfLines + 1));
	}
	
	/**
	 *  "="が後ろにつくことで変換先が変わるものの処理を行う
	 *  @param id1 "="がつく場合のID
	 *  @param id2 "="がつかない場合のID
	*/
	private void existEqual(char sign, int id1, int id2) {
		if (programStringPointer + 1 == programString.get(numOfLines).length()) {
			printFormat(String.valueOf(sign), id2);
		}
		if (programString.get(numOfLines).charAt(programStringPointer + 1) == '=') {
			printFormat(sign + "=", id1);
			programStringPointer++;
		} else if (programString.get(numOfLines).charAt(programStringPointer) == '<' && programString.get(numOfLines).charAt(programStringPointer + 1) == '>') {
			printFormat("<>", 25);
			programStringPointer++;
		} else {
			printFormat(String.valueOf(sign), id2);
		}
	}
	
	private void makeReservOrIdent() {
		StringBuilder tmpString = new StringBuilder();
		while (isAlphabet(programString.get(numOfLines).charAt(programStringPointer)) || isNumber(programString.get(numOfLines).charAt(programStringPointer))) {
			tmpString.append(programString.get(numOfLines).charAt(programStringPointer));
			programStringPointer++;
			if (programStringPointer == programString.get(numOfLines).length()) {
				break;
			}
		}
		if ((TokenID.get(tmpString.toString())) == null) {
			printFormat(tmpString.toString(), 43);
		} else {
			printFormat(tmpString.toString(), TokenID.get(tmpString.toString()));
		}
		programStringPointer--;
	}
	
	private void makeNumberNoSign() {
		StringBuilder tmpString = new StringBuilder();
		while (isNumber(programString.get(numOfLines).charAt(programStringPointer))) {
			tmpString.append(programString.get(numOfLines).charAt(programStringPointer));
			programStringPointer++;
			if (programStringPointer == programString.get(numOfLines).length()) {
				break;
			}
		}
		printFormat(tmpString.toString(), 44);
		programStringPointer--;
	}
	
	private void makeString() {
		StringBuilder tmpString = new StringBuilder();
		tmpString.append(programString.get(numOfLines).charAt(programStringPointer));
		programStringPointer++;
		if (programStringPointer == programString.get(numOfLines).length()) {
			printFormat("single quote missing", 46);
			programStringPointer--;
			return;
		}
		while (programString.get(numOfLines).charAt(programStringPointer) != '\'') {
			tmpString.append(programString.get(numOfLines).charAt(programStringPointer));
			programStringPointer++;
			if (programStringPointer == programString.get(numOfLines).length()) {
				printFormat("single_quote_missing", 46);
				return;
			}
		}
		tmpString.append(programString.get(numOfLines).charAt(programStringPointer));
		printFormat(tmpString.toString(), 45);
	}
	
	private boolean isAlphabet(char chr) {
		return ('a' <= chr && chr <= 'z') || ('A' <= chr && chr <= 'Z');
	}
	private boolean isNumber(char chr) {
		return ('0' <= chr && chr <= '9');
	}
	private boolean isOKChar(char chr) {
		return (chr == ' ' || chr == '\t' || isNumber(chr) || isAlphabet(chr) || signSet.contains(chr));
	}
}
