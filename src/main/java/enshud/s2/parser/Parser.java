package enshud.s2.parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class Parser {
	
	private List<String[]> token;
	private int tokenPointer = 0;
	private AST programAST;
	public int errFlag;	// checkerで使用
	private boolean lastFlag = false;
	private int variableID;
	private int subVariableID;
	public List<Integer> semErrList;
	private HashSet<Integer> callLineSet;
	
	/**
	 * printMode
	 * 0:	OK
	 * 1:	SynErr
	 * 2:	SemErr
	 * 3:	NotFound
	 * 4:	FormatErr
	 */
	public int printMode = 0;
	
	/**
	 * NameHash
	 * 0:	int
	 * 1:	char
	 * 2:	boolean
	 * 3:	function
	 */
	private Map<String, NameInfo> nameHash;
	private Map<String, NameInfo> subNameHash;
	private List<String> nameCash;	// 変数名一時保存用
	private boolean isInSubProgram;	// 副プログラム内かどうか
	private boolean isArray;	// 配列型かどうか
	private ArrayInfo arrayInfoInRegister;
	
	/**
	 * checkMode
	 * -2:	型検査不要
	 * -1:	検査中でない
	 * 0:	Integer検査
	 * 1:	char検査
	 * 2:	boolean検査
	 */
	private int checkMode;
	
	// コンパイラ用の変数
	private List<CasLineInfo> currCasLines;	// 現在構成中の部分のCasのコード
	private List<String> dcList;	// 定数として定義するもののリスト
	private List<CasLineInfo> casStringMaking;	// 作成中のCasの文(全体)を行ごとに格納
	public List<String> casStringToFile;	// 最終的なCasの文
	private List<CasLineInfo> casStringMakingSub;
	private List<ExpressionInfo> expressionList;	// 式の並びを記録する際のCaslの文のリスト
	private List<CasLineInfo> leftSideStrings;	// 左辺の内容のCasコード
	private boolean isComplexIndex;
	private String currSubProgramName;
	private boolean isParameter;
	
	/**
	 * useGr
	 * 
	 * 1	: 2つの被演算子の計算時に使用
	 * 2	: 出力する数字がここに入る
	 * 3	: 目的の変数のアドレスの値が[VAR + GR3]にある
	 */
	private int useGr;
	private int lastLength;	// 最後に見た要素の長さ(文字列以外は1)
	private int labelID;
	private int subProgramID;
	/**
	 * mainCompFlag
	 * 0	: メインではない
	 * 1	: メイン(while)
	 * 2	: メイン(if)
	 */
	private int mainCompFlag;
	private int mainCompLabelID;
	
	
	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		new Parser().run("data/ts/normal01.ts");
		// normalの確認
		/*
		new Parser().run("data/ts/normal01.ts");
		new Parser().run("data/ts/normal02.ts");
		new Parser().run("data/ts/normal03.ts");
		new Parser().run("src/main/java/enshud/s2/parser/test6.txt");	// 何も書いていない行があるとき
		
		// synerrの確認
		new Parser().run("src/main/java/enshud/s2/parser/test1.txt");	// 46番(lexerでプログラムにあるはずのないトークンが割り当てられる)があったとき
		new Parser().run("src/main/java/enshud/s2/parser/test2.txt");	// 正しいプログラムの後に余計な行がある場合
		new Parser().run("src/main/java/enshud/s2/parser/test3.txt");	// フォーマットに沿ってない場合(4つの要素で構成されていない行があるとき)
		new Parser().run("src/main/java/enshud/s2/parser/test4.txt");	// フォーマットに沿ってない場合(3つ目の要素が整数に変換できない場合)
		new Parser().run("src/main/java/enshud/s2/parser/test5.txt");	// フォーマットに沿ってない場合(4つ目の要素が整数に変換できない場合)
		new Parser().run("data/ts/synerr01.ts");
		new Parser().run("data/ts/synerr02.ts");
		
		
		// semerrの確認
		new Parser().run("data/ts/semerr01.ts");
		new Parser().run("data/ts/semerr02.ts");
		new Parser().run("data/ts/semerr03.ts");
		new Parser().run("data/ts/semerr04.ts");
		new Parser().run("data/ts/semerr05.ts");
		new Parser().run("data/ts/semerr06.ts");
		new Parser().run("data/ts/semerr07.ts");
		new Parser().run("data/ts/semerr08.ts");
		
		*/
		// new Parser().run("data/ts/synerr08.ts");
	}

	/**
	 * TODO
	 * 
	 * 開発対象となるParser実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，構文解析を行う．
	 * 構文が正しい場合は標準出力に"OK"を，正しくない場合は"Syntax error: line"という文字列とともに，
	 * 最初のエラーを見つけた行の番号を標準エラーに出力すること （例: "Syntax error: line 1"）．
	 * 入力ファイル内に複数のエラーが含まれる場合は，最初に見つけたエラーのみを出力すること．
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力tsファイル名
	 */
	public void run(final String inputFileName) {
		// TODO
		check(inputFileName);
		switch (printMode) {
		case 0:
			System.out.println("OK");
			for (String line: casStringToFile) {
				System.out.println(line);
			}
			break;
		case 1:
			System.err.println("Syntax error: line " + errFlag);
			break;
		case 2:
			System.out.println("OK");
			break;
		case 3:
			System.err.println("File not found");
			break;
		case 4:
			System.err.println("wrong file format");
			break;
		default:
			System.err.println("invalid value: printMode");
		}
	}
	
	public void check(final String inputFileName) {
		// 変数の初期化
		errFlag = 0;
		lastFlag = false;
		token = new ArrayList<String[]>();
		nameHash = new HashMap<String, NameInfo>();
		nameCash = new ArrayList<String>();
		currCasLines = new ArrayList<CasLineInfo>();
		semErrList = new ArrayList<Integer>();
		casStringMakingSub = new ArrayList<CasLineInfo>();
		casStringMaking = new ArrayList<CasLineInfo>();
		casStringMaking.add(new CasLineInfo("BEGIN", "LAD", 6, 0, false, -1));
		casStringMaking.add(new CasLineInfo("", "LAD", 7, "LIBBUF", -1));
		isInSubProgram = false;
		isArray = false;
		checkMode = -1;
		variableID = 0;
		currSubProgramName = "";
		useGr = 2;
		dcList = new ArrayList<String>();
		arrayInfoInRegister = new ArrayInfo(false);
		isComplexIndex = false;
		labelID = 0;
		subProgramID = 0;
		isParameter = false;
		mainCompFlag = 0;
		callLineSet = new HashSet<Integer>();
		
		try {
			for (String line: Files.readAllLines(Paths.get(inputFileName))) {
				if (line.length() == 0) {
					continue;
				}
				String[] word = line.split("\t");
				if (word.length != 4) {
					printMode = 4;
					return;
				}
				try {
					Integer.parseInt(word[2]);
					Integer.parseInt(word[3]);
				} catch (NumberFormatException e) {
					printMode = 4;
					return;
				}
				token.add(word);
			}
			String[] last = {"-1", "-1", "-1", "-1"};
			token.add(last);
			program();
			if (errFlag == 0) {
				printMode = 0;
			} else if (errFlag > 0) {
				printMode = 1;
				return;
			} else {
				printMode = 2;
				return;
			}
        } catch (IOException e) {
        	printMode = 3;
        	return;
        }
		casStringMaking.add(new CasLineInfo("", "RET", -1, true));
		casStringMaking.addAll(casStringMakingSub);
		for (int i = 0; i < dcList.size(); i++) {
			casStringMaking.add(new CasLineInfo("CHAR" + i, "DC", dcList.get(i)));
		}
		if (variableID != 0) {
			casStringMaking.add(new CasLineInfo("VAR", "DS", Integer.toString(variableID)));
		}
		casStringMaking.add(new CasLineInfo("LIBBUF", "DS", "256"));
		casStringMaking.add(new CasLineInfo("", "END", -1));
		putLabel();
		
		Optimizer optimizer = new Optimizer();
		optimizer.run(casStringMaking, callLineSet);
		casStringMaking = optimizer.getCasMakingList();
		//*/
		convertToFile();
		libWrite();
	}
	
	private void synErrAction() {
		if (errFlag <= 0) {
			errFlag = Integer.parseInt(token.get(tokenPointer)[3]);
		}
		tokenPointer = token.size() - 1;
	}
	private void semErrAction() {
		if (errFlag == 0) {
			errFlag = -1;
			/*
			System.out.println(java.util.Arrays.stream(Thread.currentThread().getStackTrace())
			.skip(1).limit(4).map(t -> t.getClassName() + "." + t.getMethodName()).collect(java.util.stream.Collectors.joining(",")));
			*/
		}
		if (semErrList.size() == 0 || semErrList.get(semErrList.size() - 1) != Integer.parseInt(token.get(tokenPointer)[3])) {
			semErrList.add(Integer.parseInt(token.get(tokenPointer)[3]));
		}
		currSubProgramName = "";
	}
	
	private void program() {
		programAST = new AST(-1, "program");
		// "program"
		checkReservedWord(17);
		// プログラム名
		programName();
		// ";"
		checkReservedWord(37);
		// ブロック
		block();
		// 複合文
		compoundStatement();
		// "."
		lastFlag = true;
		checkReservedWord(42);
	}
	// プログラム名
	private void programName() {
		programAST.addAndNext("programName");
		// 識別子
		checkReservedWord(43);
		programAST.goParent();
	}
	// ブロック
	private void block() {
		programAST.addAndNext("block");
		// 変数宣言
		variableDeclaration();
		// 副プログラム宣言群
		subPrograms();
		programAST.goParent();
	}
	// 変数宣言
	private void variableDeclaration() {
		programAST.addAndNext("variableDeclaration");
		// "var"
		if (Integer.parseInt(token.get(tokenPointer)[2]) != 21) {
			programAST.goParent();
			return;
		}
		addTokenChild();
		// 変数宣言の並び
		declarations();
		programAST.goParent();
	}
	// 変数宣言の並び
	private void declarations() {
		programAST.addAndNext("declarations");
		do {
			// 変数名の並び
			variableNames();
			// ":"
			checkReservedWord(38);
			// 型
			type();
			// ";"
			checkReservedWord(37);
		} while (Integer.parseInt(token.get(tokenPointer)[2]) == 43);	// {変数名の並び ":" 型 ";"}
		programAST.goParent();
	}
	// 変数名の並び
	private void variableNames() {
		programAST.addAndNext("variables");
		// 変数名
		nameCash.add(token.get(tokenPointer)[0]);
		variableName();
		while (Integer.parseInt(token.get(tokenPointer)[2]) == 41) {
			// ","
			addTokenChild();
			// 変数名
			nameCash.add(token.get(tokenPointer)[0]);
			variableName();
		}
		programAST.goParent();
	}
	// 変数名
	private void variableName() {
		programAST.addAndNext("variableName");
		checkReservedWord(43);
		programAST.goParent();
	}
	// 型
	private void type() {
		programAST.addAndNext("type");
		if (isStandardType()){	// 標準型
			arrayInfoInRegister = new ArrayInfo(false);	//　新しく生成しないと全部上書きされちゃうよ。
			standardType();
		} else {	// 配列型
			arrayInfoInRegister = new ArrayInfo(true);	
			arrayType();
		}
		programAST.goParent();
	}
	// 標準型
	private void standardType() {
		programAST.addAndNext("standardType");
		int typeID;
		switch (Integer.parseInt(token.get(tokenPointer)[2])) {
		case 11:
			typeID = 0;
			break;
		case 4:
			typeID = 1;
			break;
		default:
			typeID = 2;
		}
		registerTypeID(typeID);
		addTokenChild();
		programAST.goParent();
	}
	// 配列型
	private void arrayType() {
		programAST.addAndNext("arrayType");
		// "array"
		checkReservedWord(1);
		// "["
		checkReservedWord(35);
		// 添字の最小値
		arrayInfoInRegister.start = parseInt(token.get(tokenPointer)[0]);
		checkInt();
		// ".."
		checkReservedWord(39);
		// 添字の最大値
		arrayInfoInRegister.end = parseInt(token.get(tokenPointer)[0]);
		checkInt();
		if (arrayInfoInRegister.end < arrayInfoInRegister.start) {
			semErrAction();
		}
		// "["
		checkReservedWord(36);
		// "of"
		checkReservedWord(14);
		// 標準型
		if (!isStandardType()){
			synErrAction();
		}
		standardType();
		programAST.goParent();
	}
	// 副プログラム宣言群
	private void subPrograms() {
		programAST.addAndNext("subPrograms");
		while (Integer.parseInt(token.get(tokenPointer)[2]) == 16) {
			// 副プログラム宣言
			subProgramDeclaration();
			subProgramID++;
			// ";"
			checkReservedWord(37);
		}
		programAST.goParent();
	}
	// 副プログラム宣言
	private void subProgramDeclaration() {
		isInSubProgram = true;
		subNameHash = new HashMap<String, NameInfo>(nameHash);
		programAST.addAndNext("subProgramDeclaration");
		casStringMakingSub.add(new CasLineInfo("PROC" + subProgramID, "\tNOP", -1));
		// 副プログラム頭部
		subProgramHead();
		// 変数宣言
		variableDeclaration();
		if (!isHashNull(nameHash, currSubProgramName)) {
			nameHash.get(currSubProgramName).variableNum = subVariableID;
		}
		// 複合文
		compoundStatement();
		casStringMakingSub.add(new CasLineInfo("", "RET", -1, false));
		programAST.goParent();
		isInSubProgram = false;
		subVariableID = 0;
		currSubProgramName = "";
	}
	// 副プログラム頭部
	private void subProgramHead() {
		subVariableID = 0;
		programAST.addAndNext("subProgramHead");
		// "procedure"
		checkReservedWord(16);
		// 手続き名
		currSubProgramName = token.get(tokenPointer)[0];
		if (nameHash.get(currSubProgramName) != null) {
			semErrAction();
		} else {
			nameHash.put(currSubProgramName, new NameInfo(3, subProgramID, false, new ArrayInfo(false)));
		}
		subProgramName();
		// 仮パラメータ
		isParameter = true;
		parameter();
		isParameter = false;
		// ";"
		checkReservedWord(37);
		programAST.goParent();
	}
	// 手続き名
	private void subProgramName() {
		programAST.addAndNext("subProgramName");
		checkReservedWord(43);
		programAST.goParent();
	}
	// 仮パラメータ
	private void parameter() {
		programAST.addAndNext("parameter");
		// "("
		if (Integer.parseInt(token.get(tokenPointer)[2]) != 33) {
			programAST.goParent();
			return;
		}
		addTokenChild();
		// 仮パラメータの並び
		parameters();
		// ")"
		checkReservedWord(34);
		programAST.goParent();
	}
	// 仮パラメータの並び
	private void parameters() {
		programAST.addAndNext("parameters");
		// 仮パラメータ名の並び
		parameterNames();
		//  ":"
		checkReservedWord(38);
		// 標準型
		if (!isStandardType()){
			synErrAction();
		}
		standardType();
		// { ";" 仮パラメータ名の並び ":" 標準型 }
		while (Integer.parseInt(token.get(tokenPointer)[2]) == 37) {
			// ";"
			addTokenChild();
			// 仮パラメータ名の並び
			parameterNames();
			//  ":"
			checkReservedWord(38);
			// 標準型
			if (!isStandardType()){
				synErrAction();
			}
			standardType();
		}
		programAST.goParent();
	}
	// 仮パラメータ名の並び
	private void parameterNames() {
		programAST.addAndNext("parameterNames");
		// 仮パラメータ名
		nameCash.add(token.get(tokenPointer)[0]);
		parameterName();
		// { "," 仮パラメータ名 }
		while (Integer.parseInt(token.get(tokenPointer)[2]) == 41) {
			// ","
			addTokenChild();
			// 仮パラメータ名
			nameCash.add(token.get(tokenPointer)[0]);
			parameterName();
		}
		programAST.goParent();
		
	}
	// 仮パラメータ名
	private void parameterName() {
		programAST.addAndNext("parameterName");
		checkReservedWord(43);
		programAST.goParent();
	}
	// 複合文
	private void compoundStatement() {
		programAST.addAndNext("compoundStatement");
		// "begin"
		checkReservedWord(2);
		// 文の並び
		statements();
		//  "end"
		checkReservedWord(8);
		programAST.goParent();
	}
	// 文の並び
	private void statements() {
		programAST.addAndNext("statements");
		do {
			// 文
			statement();
			// ";"
			checkReservedWord(37);
		} while (Integer.parseInt(token.get(tokenPointer)[2]) != 8 && errFlag <= 0);	// { 文 ";" }
		programAST.goParent();
	}
	// 文
	private void statement() {
		programAST.addAndNext("statement");
		int tempLabelID = labelID;
		mainCompLabelID = tempLabelID;
		int tempLine = Integer.parseInt(token.get(tokenPointer)[3]);
		switch (Integer.parseInt(token.get(tokenPointer)[2])) {
		case 10:	// "if"
			addTokenChild();
			labelID++;
			// 式
			checkMode = 2;
			mainCompFlag = 2;
			expression();
			addAllMakingList(currCasLines);
			currCasLines = new ArrayList<CasLineInfo>();
			checkMode = -1;
			if (mainCompFlag == 0) {
				addMakingList(new CasLineInfo("", "CPL", 2, -1, true, Integer.parseInt(token.get(tokenPointer)[3])));
				addMakingList(new CasLineInfo("", "JNZ", "ENDIF" + tempLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
			} else {
				mainCompFlag = 0;
			}
			// "then"
			checkReservedWord(19);
			// 複合文
			compoundStatement();
			// ["else" 複合文]
			if (Integer.parseInt(token.get(tokenPointer)[2]) == 7){
				// "else"
				addTokenChild();
				addMakingList(new CasLineInfo("", "JUMP", "ENDEL" + tempLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
				// 複合文
				addMakingList(new CasLineInfo("ENDIF" + tempLabelID, false));
				compoundStatement();
				addMakingList(new CasLineInfo("ENDEL" + tempLabelID, false));
			} else {
				addMakingList(new CasLineInfo("ENDIF" + tempLabelID, false));
			}
			break;
		case 22:	// "while"
			addTokenChild();
			labelID++;
			// 式
			addMakingList(new CasLineInfo("WHILE" + tempLabelID, false));
			checkMode = 2;
			mainCompFlag = 1;
			expression();
			addAllMakingList(currCasLines);
			currCasLines = new ArrayList<CasLineInfo>();
			checkMode = -1;
			if (mainCompFlag == 0) {
				addMakingList(new CasLineInfo("", "CPL", 2, -1, true, Integer.parseInt(token.get(tokenPointer)[3])));
				addMakingList(new CasLineInfo("", "JNZ", "ENDWH" + tempLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
			} else {
				mainCompFlag = 0;
			}
			// do
			checkReservedWord(6);
			// 複合文
			compoundStatement();
			addMakingList(new CasLineInfo("", "JUMP", "WHILE" + tempLabelID, tempLine));
			addMakingList(new CasLineInfo("ENDWH" + tempLabelID, false));
			labelID++;
			break;
		default:	// 基本文
			basicStatement();
		}
		programAST.goParent();
	}
	// 式
	private boolean expression() {
		programAST.addAndNext("expression");
		// 単純式
		int tempCheckMode = checkMode;
		if (checkMode == 2) {
			checkMode = -1;
		}
		// 左の被演算子計算中に比較の式が出てきても大丈夫なように
		int tempCompFlag = mainCompFlag;
		mainCompFlag = 0;
		boolean isSingle = simpleExpression();
		boolean isExistOp = false;	// 演算子が存在するかどうか
		//  [ 関係演算子 単純式 ]
		if (isRelationalOperator()) {
			List<CasLineInfo> tempCasStrings = currCasLines;
			currCasLines = new ArrayList<CasLineInfo>();
			isExistOp = true;
			int opID = Integer.parseInt(token.get(tokenPointer)[2]);
			// 関係演算子
			addTokenChild();
			// 単純式
			changeGR1and2();
			switch (opID) {
			case 24:	// =
				if (simpleExpression()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				compare();
				switch (tempCompFlag) {
				case 1:
					currCasLines.add(new CasLineInfo("", "JNZ", "ENDWH" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					mainCompFlag = tempCompFlag;
					break;
				case 2:
					currCasLines.add(new CasLineInfo("", "JNZ", "ENDIF" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					mainCompFlag = tempCompFlag;
					break;
				default:
					currCasLines.add(new CasLineInfo("", "JZE", "TRUE" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "LAD", useGr, 0, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "CONT" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("TRUE" + labelID, "LAD", useGr, 65535, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("CONT" + labelID, false));	// ラベル(このラベルは最後に次の行の命令と繋げる。+でラベルの行かどうかを判別)
					labelID++;
				}
				break;
			case 25:	// <>
				if (simpleExpression()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				compare();
				switch (tempCompFlag) {
				case 1:
					currCasLines.add(new CasLineInfo("", "JZE", "ENDWH" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					mainCompFlag = tempCompFlag;
					break;
				case 2:
					currCasLines.add(new CasLineInfo("", "JZE", "ENDIF" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					mainCompFlag = tempCompFlag;
					break;
				default:
					currCasLines.add(new CasLineInfo("", "JZE", "FALSE" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "LAD", useGr, 65535, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "CONT" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("FALSE" + labelID, "LAD", useGr, 0, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("CONT" + labelID, false));	// ラベル(このラベルは最後に次の行の命令と繋げる。+でラベルの行かどうかを判別)
					labelID++;
				}
				break;
			case 26:	// <
				if (simpleExpression()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				compare();
				switch (tempCompFlag) {
				case 1:
					currCasLines.add(new CasLineInfo("", "JMI", "SRTWH" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "ENDWH" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("SRTWH" + mainCompLabelID, false));
					mainCompFlag = tempCompFlag;
					break;
				case 2:
					currCasLines.add(new CasLineInfo("", "JMI", "IF" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "ENDIF" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("IF" + mainCompLabelID, false));
					mainCompFlag = tempCompFlag;
					break;
				default:
					currCasLines.add(new CasLineInfo("", "JMI", "TRUE" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "LAD", useGr, 0, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "CONT" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("TRUE" + labelID, "LAD", useGr, 65535, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("CONT" + labelID, false));	// ラベル(このラベルは最後に次の行の命令と繋げる。+でラベルの行かどうかを判別)
					labelID++;
				}
				break;
			case 27:	// <=
				if (simpleExpression()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				compare();
				switch (tempCompFlag) {
				case 1:
					currCasLines.add(new CasLineInfo("", "JPL", "ENDWH" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					mainCompFlag = tempCompFlag;
					break;
				case 2:
					currCasLines.add(new CasLineInfo("", "JPL", "ENDIF" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					mainCompFlag = tempCompFlag;
					break;
				default:
					currCasLines.add(new CasLineInfo("", "JPL", "FALSE" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "LAD", useGr, 65535, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "CONT" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("FALSE" + labelID, "LAD", useGr, 0, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("CONT" + labelID, false));	// ラベル(このラベルは最後に次の行の命令と繋げる。+でラベルの行かどうかを判別)
					labelID++;
				}
				break;
			case 28:	// >=
				if (simpleExpression()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				compare();
				switch (tempCompFlag) {
				case 1:
					currCasLines.add(new CasLineInfo("", "JMI", "ENDWH" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					mainCompFlag = tempCompFlag;
					break;
				case 2:
					currCasLines.add(new CasLineInfo("", "JMI", "ENDIF" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					mainCompFlag = tempCompFlag;
					break;
				default:
					currCasLines.add(new CasLineInfo("", "JMI", "FALSE" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "LAD", useGr, 65535, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "CONT" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("FALSE" + labelID, "LAD", useGr, 0, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("CONT" + labelID, false));	// ラベル(このラベルは最後に次の行の命令と繋げる。+でラベルの行かどうかを判別)
					labelID++;
				}
				break;
			case 29:	// >
				if (simpleExpression()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				compare();
				switch (tempCompFlag) {
				case 1:
					currCasLines.add(new CasLineInfo("", "JPL", "SRTWH" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "ENDWH" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("SRTWH" + mainCompLabelID, false));
					mainCompFlag = tempCompFlag;
					break;
				case 2:
					currCasLines.add(new CasLineInfo("", "JPL", "IF" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "ENDIF" + mainCompLabelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("IF" + mainCompLabelID, false));
					mainCompFlag = tempCompFlag;
					break;
				default:
					currCasLines.add(new CasLineInfo("", "JPL", "TRUE" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "LAD", useGr, 0, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("", "JUMP", "CONT" + labelID, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("TRUE" + labelID, "LAD", useGr, 65535, false, Integer.parseInt(token.get(tokenPointer)[3])));
					currCasLines.add(new CasLineInfo("CONT" + labelID, false));	// ラベル(このラベルは最後に次の行の命令と繋げる。+でラベルの行かどうかを判別)
					labelID++;
				}
				break;
			}
			checkMode = 2;
		} else if (tempCheckMode == 2 && checkMode != 2) {
			semErrAction();
		}
		if (tempCheckMode != -1 && tempCheckMode != checkMode) {
			semErrAction();
		}
		programAST.goParent();

		return (!isExistOp) && isSingle;
	}
	// 単純式
	private boolean simpleExpression() {
		boolean isMinus = false;	// マイナスの符号があるかどうか
		programAST.addAndNext("simpleExpression");
		// 符号
		if (Integer.parseInt(token.get(tokenPointer)[2]) == 30 || Integer.parseInt(token.get(tokenPointer)[2]) == 31) {
			// "-"がついているか
			if (Integer.parseInt(token.get(tokenPointer)[2]) == 31) {
				isMinus = true;
			}
			addTokenChild();
			// 符号が来たときは整数の計算である
			checkMode = 0;
		}
		// 項
		boolean isSingle = term();
		boolean isExistOp = false;
		// "-"がついているか確認
		if (isMinus) {
			currCasLines.add(new CasLineInfo("", "XOR", useGr, -1, true, Integer.parseInt(token.get(tokenPointer)[3])));
			currCasLines.add(new CasLineInfo("", "ADDA", useGr, 1, true, Integer.parseInt(token.get(tokenPointer)[3])));
		}
		// { 加法演算子 項 }
		while (isLikeAdditiveOperator()) {
			List<CasLineInfo> tempCasStrings = currCasLines;
			currCasLines = new ArrayList<CasLineInfo>();
			int opID = Integer.parseInt(token.get(tokenPointer)[2]);
			isExistOp = true;
			// 加法演算子
			checkOperator();
			addTokenChild();
			// 項
			changeGR1and2();
			switch (opID) {
			case 15:	// or
				if (term()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				if (useGr == 1) {
					currCasLines.add(new CasLineInfo("", "OR", 1, 2, Integer.parseInt(token.get(tokenPointer)[3])));
				} else {
					currCasLines.add(new CasLineInfo("", "OR", 2, 1, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				break;
			case 30:	// +
				if (term()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				if (useGr == 1) {
					currCasLines.add(new CasLineInfo("", "ADDA", 1, 2, Integer.parseInt(token.get(tokenPointer)[3])));
				} else {
					currCasLines.add(new CasLineInfo("", "ADDA", 2, 1, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				break;
			case 31:	// -
				if (term()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				if (useGr == 1) {
					currCasLines.add(new CasLineInfo("", "SUBA", 1, 2, Integer.parseInt(token.get(tokenPointer)[3])));
				} else {
					currCasLines.add(new CasLineInfo("", "SUBA", 2, 1, Integer.parseInt(token.get(tokenPointer)[3])));
				}
			}
		}
		programAST.goParent();
		return (!isExistOp) && isSingle;
	}
	// 項
	private boolean term() {
		programAST.addAndNext("term");
		// 因子
		boolean isSingle = factor();
		boolean isExistOp = false;
		// { 乗法演算子 因子 }
		while (isLikeMultiplicativeOperator()) {
			List<CasLineInfo> tempCasStrings = currCasLines;
			currCasLines = new ArrayList<CasLineInfo>();
			int opID = Integer.parseInt(token.get(tokenPointer)[2]);
			isExistOp = true;
			// 乗法演算子
			checkOperator();
			addTokenChild();
			// 因子
			changeGR1and2();
			switch (opID) {
			case 0:	// and
				if (factor()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				if (useGr == 1) {
					currCasLines.add(new CasLineInfo("", "AND", 1, 2, Integer.parseInt(token.get(tokenPointer)[3])));
				} else {
					currCasLines.add(new CasLineInfo("", "AND", 2, 1, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				break;
			case 5:	// / or div
				if (factor()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					if (useGr == 2) {
						swapGr(currCasLines);
					}
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					if (useGr == 2) {
						currCasLines.add(new CasLineInfo("", "LD", 2, 1, Integer.parseInt(token.get(tokenPointer)[3])));
					}
					currCasLines.add(new CasLineInfo("", "POP", 1, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				currCasLines.add(new CasLineInfo("", "CALL", "DIV", Integer.parseInt(token.get(tokenPointer)[3])));
				if (useGr == 1) {
					currCasLines.add(new CasLineInfo("", "LD", 1, 2, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				break;
			case 12:	// mod
				if (factor()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					if (useGr == 2) {
						swapGr(currCasLines);
					}
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					if (useGr == 2) {
						currCasLines.add(new CasLineInfo("", "LD", 2, 1, Integer.parseInt(token.get(tokenPointer)[3])));
					}
					currCasLines.add(new CasLineInfo("", "POP", 1, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				currCasLines.add(new CasLineInfo("", "CALL", "DIV", Integer.parseInt(token.get(tokenPointer)[3])));
				if (useGr == 2) {
					currCasLines.add(new CasLineInfo("", "LD", 2, 1, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				break;
			case 32:	// *
				if (factor()) {
					changeGR1and2();
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
				} else {
					changeGR1and2();
					tempCasStrings.add(new CasLineInfo("", "PUSH", "0", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
					tempCasStrings.addAll(currCasLines);
					currCasLines = tempCasStrings;
					currCasLines.add(new CasLineInfo("", "POP", useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				currCasLines.add(new CasLineInfo("", "CALL", "MULT", Integer.parseInt(token.get(tokenPointer)[3])));
				if (useGr == 1) {
					currCasLines.add(new CasLineInfo("", "LD", 1, 2, Integer.parseInt(token.get(tokenPointer)[3])));
				}
				break;
			}
		}
		programAST.goParent();
		return (!isExistOp) && isSingle;
	}
	// 因子
	private boolean factor() {
		programAST.addAndNext("factor");
		boolean isSingle = true;	// 1つの因子で構成されているか
		switch (Integer.parseInt(token.get(tokenPointer)[2])) {
		case 43:	// 変数
			variable(true);
			isSingle = isComplexIndex;
			break;
		case 33:	// "("
			addTokenChild();
			isSingle = expression();
			// ")"
			checkReservedWord(34);
			break;
		case 13:	// "not"
			if (checkMode == -1) {
				checkMode = 2;
			} else if (checkMode != 2) {
				semErrAction();
			}
			addTokenChild();
			// 因子
			isSingle = factor();
			currCasLines.add(new CasLineInfo("", "XOR", useGr, -1, true, Integer.parseInt(token.get(tokenPointer)[3])));
			break;
		default:	// 定数
			constant();
		}
		programAST.goParent();
		return isSingle;
	}
	// 変数(isCallValue→その変数の値を使うかどうか)
	private void variable(boolean isCallValue) {
		programAST.addAndNext("variable");
		// 変数名
		checkType();
		String variableName = token.get(tokenPointer)[0];
		variableName();
		// "[" 添字 "]"
		isComplexIndex = false;	// 複雑な変数(インデックス部分に計算があるなど)
		if (Integer.parseInt(token.get(tokenPointer)[2]) == 35) {
			if (!isArray) {
				semErrAction();
			}
			// "["
			addTokenChild();
			// 添字
			int tempCheckMode = checkMode;
			checkMode = 0;
			boolean tempIsArray = isArray;
			isArray = false;
			// index計算のためもともと計算していたものを退避
			currCasLines.add(new CasLineInfo("", "PUSH", "0", useGr == 1 ? 2 : 1, Integer.parseInt(token.get(tokenPointer)[3])));
			index();
			checkMode = tempCheckMode;
			isArray = tempIsArray;
			// もともと計算していたものを戻す
			currCasLines.add(new CasLineInfo("", "POP", useGr == 1 ? 2 : 1, Integer.parseInt(token.get(tokenPointer)[3])));
			// "]"
			checkReservedWord(36);
		} else if (isArray) {
			semErrAction();
		}
		if (!isHashNull(isInSubProgram ? subNameHash : nameHash, variableName)) {
			NameInfo variableInfo = getVariableInfo(variableName);
			if (isArray) {
				currCasLines.add(new CasLineInfo("", "LD", 3, variableInfo.id, true, Integer.parseInt(token.get(tokenPointer)[3])));
				currCasLines.add(new CasLineInfo("", "SUBA", useGr, variableInfo.arrayInfo.start, true, Integer.parseInt(token.get(tokenPointer)[3])));
				currCasLines.add(new CasLineInfo("", "ADDA", 3, useGr, Integer.parseInt(token.get(tokenPointer)[3])));
				if (isCallValue) {
					setVariable("LD", useGr, currCasLines, variableInfo.isGlobal);
				}
			} else {
				currCasLines.add(new CasLineInfo("", "LD", 3, variableInfo.id, true, Integer.parseInt(token.get(tokenPointer)[3])));
				if (isCallValue) {
					setVariable("LD", useGr, currCasLines, variableInfo.isGlobal);
				}
			}
		}
		isArray = false;
		programAST.goParent();
	}
	// 添字
	private void index() {
		programAST.addAndNext("index");
		isComplexIndex = expression();
		programAST.goParent();
	}
	// 定数
	private void constant() {
		programAST.addAndNext("constant");
		switch (Integer.parseInt(token.get(tokenPointer)[2])) {
		case 45:	//文字列
			if (token.get(tokenPointer)[0].length() == 3) {
				try {
					currCasLines.add(new CasLineInfo("", "LD", useGr, token.get(tokenPointer)[0].getBytes("SJIS")[1], true, Integer.parseInt(token.get(tokenPointer)[3])));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				currCasLines.add(new CasLineInfo("", "LAD", useGr, "CHAR" + dcList.size(), Integer.parseInt(token.get(tokenPointer)[3])));
			}
			dcList.add(token.get(tokenPointer)[0]);
			lastLength = token.get(tokenPointer)[0].length() - 2;
			checkConstant(1);
			break;
		case 9:	// "false"
			currCasLines.add(new CasLineInfo("", "LAD", useGr, 0, false, Integer.parseInt(token.get(tokenPointer)[3])));
			checkConstant(2);
			lastLength = 1;
			break;
		case 20:	// "true"
			currCasLines.add(new CasLineInfo("", "LAD", useGr, 65535, false, Integer.parseInt(token.get(tokenPointer)[3])));
			checkConstant(2);
			lastLength = 1;
			break;
		case 44:	// 符号なし整数
			currCasLines.add(new CasLineInfo("", "LD", useGr, Integer.parseInt(token.get(tokenPointer)[0]), true, Integer.parseInt(token.get(tokenPointer)[3])));
			checkConstant(0);
			lastLength = 1;
			break;
		default:
			synErrAction();
		}
		programAST.goParent();
	}
	// 基本文
	private void basicStatement() {
		programAST.addAndNext("basicStatement");
		switch (Integer.parseInt(token.get(tokenPointer)[2])) {
		case 43:	// 手続き呼出し文 or 代入文
			if (tokenPointer + 1 >= token.size()) {
				synErrAction();
				break;
			}
			if (Integer.parseInt(token.get(tokenPointer + 1)[2]) == 40 || Integer.parseInt(token.get(tokenPointer + 1)[2]) == 35) {
				// 代入文
				assignmentStatement();
			} else {
				// 手続き呼出し文
				callSubProgram();
			}
			break;
		case 18:	// 入力文
			inputStatement();
			break;
		case 23:	// 出力文
			outputStatement();
			break;
		case 2:	// 複合文
			compoundStatement();
			break;
		default:
			// エラー処理
			synErrAction();
		}
		
		
		programAST.goParent();
	}
	// 手続き呼出し文
	private void callSubProgram() {
		programAST.addAndNext("callSubProgram");
		String tempSubProgramName = currSubProgramName;
		// 手続き名
		if (nameHash.get(token.get(tokenPointer)[0]) == null || nameHash.get(token.get(tokenPointer)[0]).type != 3) {
			semErrAction();
		}
		currSubProgramName = token.get(tokenPointer)[0];
		boolean isNull = isHashNull(nameHash, currSubProgramName);
		subProgramName();
		if (!isNull) {
			addMakingList(new CasLineInfo("", "SUBA", 8, nameHash.get(currSubProgramName).variableNum, true, Integer.parseInt(token.get(tokenPointer)[3])));
		}
		// [ "(" 式の並び ")" ]
		if (Integer.parseInt(token.get(tokenPointer)[2]) == 33) {
			if (!isNull && nameHash.get(currSubProgramName).parameterTypes.size() == 0){
				semErrAction();
			}
			// "("
			checkReservedWord(33);
			// 式の並び
			expressions(true);
			
			// 引数を入れる処理
			for (int i = 0; i < expressionList.size(); i++) {
				addAllMakingList(expressionList.get(i).casStrings);
				addMakingList(new CasLineInfo("", "LD", 3, i, true, Integer.parseInt(token.get(tokenPointer)[3])));
				addMakingList(new CasLineInfo("", "ADDA", 3, 8, Integer.parseInt(token.get(tokenPointer)[3])));
				addMakingList(new CasLineInfo("", "ST", 2, 0, 3, Integer.parseInt(token.get(tokenPointer)[3])));
			}
			
			// ")"
			checkReservedWord(34);
		} else if (nameHash.get(currSubProgramName) != null && nameHash.get(currSubProgramName).parameterTypes != null
				&& nameHash.get(currSubProgramName).parameterTypes.size() > 0) {
			semErrAction();
		}
		if (!isNull) {
			callLineSet.add(Integer.parseInt(token.get(tokenPointer)[3]));
			addMakingList(new CasLineInfo("", "LD", 4, 8, Integer.parseInt(token.get(tokenPointer)[3])));
			addMakingList(new CasLineInfo("", "CALL", "PROC" + nameHash.get(currSubProgramName).id, Integer.parseInt(token.get(tokenPointer)[3])));
			addMakingList(new CasLineInfo("", "LD", 0, nameHash.get(currSubProgramName).variableNum, true, Integer.parseInt(token.get(tokenPointer)[3])));
			addMakingList(new CasLineInfo("", "ADDA", 4, 0, Integer.parseInt(token.get(tokenPointer)[3])));
			addMakingList(new CasLineInfo("", "ADDA", 8, 0, Integer.parseInt(token.get(tokenPointer)[3])));
		}
		currSubProgramName = tempSubProgramName;
		programAST.goParent();
	}
	// 式の並び
	private void expressions(boolean isCallingSubProgram) {
		expressionList = new ArrayList<ExpressionInfo>();
		int paramPointer = 0;
		programAST.addAndNext("expressions");
		// 式
		if (isCallingSubProgram && nameHash.get(currSubProgramName).parameterTypes.size() > 0) {
			checkMode = nameHash.get(currSubProgramName).parameterTypes.get(paramPointer);
		}
		expression();
		expressionList.add(new ExpressionInfo(checkMode, currCasLines, lastLength));
		currCasLines = new ArrayList<CasLineInfo>();
		checkMode = -1;
		// { "," 式 }
		while (Integer.parseInt(token.get(tokenPointer)[2]) == 41) {
			paramPointer++;
			if (isCallingSubProgram && paramPointer >= nameHash.get(currSubProgramName).parameterTypes.size()) {
				semErrAction();
			}
			// ","
			addTokenChild();
			// 式
			if (isCallingSubProgram && currSubProgramName.length() > 0) {
				checkMode = nameHash.get(currSubProgramName).parameterTypes.get(paramPointer);
			}
			expression();
			expressionList.add(new ExpressionInfo(checkMode, currCasLines, lastLength));
			currCasLines = new ArrayList<CasLineInfo>();
			checkMode = -1;
		}
		if (isCallingSubProgram && paramPointer != nameHash.get(currSubProgramName).parameterTypes.size() - 1
				&& nameHash.get(currSubProgramName).parameterTypes.size() > 0) {
			semErrAction();
		}
		programAST.goParent();
	}
	// 入力文
	private void inputStatement() {
		programAST.addAndNext("inputStatement");
		// "readln"(確定済み)
		addTokenChild();
		// "("
		checkReservedWord(33);
		// 変数の並び
		checkMode = -2;
		variables();
		checkMode = -1;
		// ")"
		checkReservedWord(34);
		programAST.goParent();
	}
	// 出力文
	private void outputStatement() {
		programAST.addAndNext("outputStatement");
		// "writeln"(確定済み)
		addTokenChild();
		// "("
		checkReservedWord(33);
		// 式の並び
		expressions(false);
		
		// 出力
		writeProcess();
		
		// ")"
		checkReservedWord(34);
		programAST.goParent();
	}
	// 変数の並び
	private void variables() {
		programAST.addAndNext("variables");
		// 変数
		variable(false);
		while (Integer.parseInt(token.get(tokenPointer)[2]) == 41) {
			// ","
			addTokenChild();
			// 変数
			variable(false);
		}
		programAST.goParent();
	}
	// 代入文
	private void assignmentStatement() {
		programAST.addAndNext("assignmentStatement");
		// 左辺
		String variableName = token.get(tokenPointer)[0];
		leftSide();
		boolean tempIsComplex = isComplexIndex;
		// ":="
		checkReservedWord(40);
		// 式
		expression();
		addAllMakingList(currCasLines);
		currCasLines = new ArrayList<CasLineInfo>();
		if (tempIsComplex) {
			addMakingList(new CasLineInfo("", "PUSH", "0", 2, Integer.parseInt(token.get(tokenPointer)[3])));
		}
		addAllMakingList(leftSideStrings);
		leftSideStrings = new ArrayList<CasLineInfo>();
		if (tempIsComplex) {
			addMakingList(new CasLineInfo("", "POP", 2, Integer.parseInt(token.get(tokenPointer)[3])));
		}
		if (!isHashNull(isInSubProgram ? subNameHash : nameHash, variableName)) {
			setVariable("ST", 2, isInSubProgram ? casStringMakingSub : casStringMaking, getVariableInfo(variableName).isGlobal);
		}
		checkMode = -1;
		programAST.goParent();
	}
	// 左辺
	private void leftSide() {
		programAST.addAndNext("leftSide");
		// 変数
		changeGR1and2();
		variable(false);
		changeGR1and2();
		leftSideStrings = currCasLines;
		currCasLines = new ArrayList<CasLineInfo>();
		programAST.goParent();
	}
	
	
	
	
	
	
	// checker用のやつ
	// 型登録
	private void registerTypeID(int typeID) {
		for (String name: nameCash) {
			if (isInSubProgram) {
				if (currSubProgramName.equals(name)) {
					semErrAction();
				} else if (subNameHash.get(name) != null && !subNameHash.get(name).isGlobal) {
					semErrAction();
				} else {
					subNameHash.put(name, new NameInfo(typeID, subVariableID, false, arrayInfoInRegister));
					if (arrayInfoInRegister.isArray) {
						subVariableID += arrayInfoInRegister.end - arrayInfoInRegister.start + 1;
					} else {
						subVariableID++;
					}
				}
			} else {
				if (nameHash.get(name) != null) {
					semErrAction();
				} else {
					nameHash.put(name, new NameInfo(typeID, variableID, true, arrayInfoInRegister));
					if (arrayInfoInRegister.isArray) {
						variableID += arrayInfoInRegister.end - arrayInfoInRegister.start + 1;
					} else {
						variableID++;
					}
				}
			}
			if (isParameter) {
				nameHash.get(currSubProgramName).parameterTypes.add(typeID);
			}
		}
		nameCash = new ArrayList<String>();
	}
	// 型チェック
	private void checkType() {
		if (isInSubProgram) {
			if (subNameHash.get(token.get(tokenPointer)[0]) == null) {
				semErrAction();
				return;
			}
			if (checkMode == -1) {
				checkMode = subNameHash.get(token.get(tokenPointer)[0]).type;
				if (subNameHash.get(token.get(tokenPointer)[0]).arrayInfo != null) {
					isArray = subNameHash.get(token.get(tokenPointer)[0]).arrayInfo.isArray;
				}
			} else {
				if (checkMode != subNameHash.get(token.get(tokenPointer)[0]).type) {
					semErrAction();
				} else if (subNameHash.get(token.get(tokenPointer)[0]).arrayInfo != null) {
					isArray = subNameHash.get(token.get(tokenPointer)[0]).arrayInfo.isArray;
				}
			}
		} else {
			if (nameHash.get(token.get(tokenPointer)[0]) == null) {
				semErrAction();
				return;
			}
			if (checkMode == -1) {
				checkMode = nameHash.get(token.get(tokenPointer)[0]).type;
				if (nameHash.get(token.get(tokenPointer)[0]).arrayInfo != null) {
					isArray = nameHash.get(token.get(tokenPointer)[0]).arrayInfo.isArray;
				}
			} else {
				if (checkMode != nameHash.get(token.get(tokenPointer)[0]).type) {
					semErrAction();
				} else if (nameHash.get(token.get(tokenPointer)[0]).arrayInfo != null) {
					isArray = nameHash.get(token.get(tokenPointer)[0]).arrayInfo.isArray;
				}
			}
		}
	}
	private void checkOperator() {
		switch (checkMode) {
		case 0:
			if (!isOperatorUsedInteger()) {
				semErrAction();
			}
			break;
		case 2:
			if (!isOperatorUsedBoolean()) {
				semErrAction();
			}
			break;
		case -2:
			break;
		default:
			semErrAction();
		}
	}
	// 定数チェック
	private void checkConstant(int constantType) {
		if (checkMode == -1) {
			checkMode = constantType;
		} else if (checkMode != constantType) {
			semErrAction();
		}
		addTokenChild();
	}
	
	// コンパイラで使用するメソッド
	private void writeProcess() {
		for (ExpressionInfo exp: expressionList) {
			addAllMakingList(exp.casStrings);
			switch (exp.type) {
			case 0:
				addMakingList(new CasLineInfo("", "CALL", "WRTINT", Integer.parseInt(token.get(tokenPointer)[3])));
				break;
			case 1:
				if (exp.length == 1) {
					addMakingList(new CasLineInfo("", "CALL", "WRTCH", Integer.parseInt(token.get(tokenPointer)[3])));
				} else {
					addMakingList(new CasLineInfo("", "LD", 1, exp.length, true, Integer.parseInt(token.get(tokenPointer)[3])));
					addMakingList(new CasLineInfo("", "CALL", "WRTSTR", Integer.parseInt(token.get(tokenPointer)[3])));
				}
				break;
			case 2:
				addMakingList(new CasLineInfo("", "CALL", "WRTBOL", Integer.parseInt(token.get(tokenPointer)[3])));
				break;
			}
			//	addMakingList(new CasLineInfo("", "CALL", "WRTCONM", Integer.parseInt(token.get(tokenPointer)[3])));	// 出力の区切りを", "にする場合
		}
		
		/* 力の区切りを", "にする場合
		if (isInSubProgram) {
			casStringMakingSub.remove(casStringMakingSub.size() - 1);
		} else {
			casStringMaking.remove(casStringMaking.size() - 1);
		}
		//*/
		
		addMakingList(new CasLineInfo("", "CALL", "WRTLN", Integer.parseInt(token.get(tokenPointer)[3])));
	}
	
	private void libWrite() {
		try {
			for (String line: Files.readAllLines(Paths.get("src/main/java/enshud/s2/parser/lib.cas"))) {
				casStringToFile.add(line);
			}
		} catch (IOException e) {
			System.err.print("file error");
		}
	}
	// useGrの1と2を切り替える。
	private void changeGR1and2() {
		if (useGr == 1) {
			useGr = 2;
		} else if (useGr == 2) {
			useGr = 1;
		} else {
			System.out.println("Now useGr is " + useGr);
		}
	}
	// GR1とGR2の内容を入れ替え
	private void swapGr(List<CasLineInfo> casStrings) {
		casStrings.add(new CasLineInfo("", "LD", 0, 2, Integer.parseInt(token.get(tokenPointer)[3])));
		casStrings.add(new CasLineInfo("", "LD", 2, 1, Integer.parseInt(token.get(tokenPointer)[3])));
		casStrings.add(new CasLineInfo("", "LD", 1, 0, Integer.parseInt(token.get(tokenPointer)[3])));
	}
	// ラベルをはがす
	private void convertToFile() {
		casStringToFile = new ArrayList<String>();
		casStringToFile.add("CASL\tSTART\tBEGIN\t;");
		for (CasLineInfo casLineInfo: casStringMaking) {
			casStringToFile.add(casLineInfo.getCasString());
		}
	}
	// ラベルを正常な位置につける。
	private void putLabel() {
		List<CasLineInfo> tempCasMaking = new ArrayList<CasLineInfo>();
		int i = 0;
		while (i < casStringMaking.size()) {
			if (casStringMaking.get(i).lineType == 8) {
				if (casStringMaking.get(i + 1).label != "") {
					tempCasMaking.add(new CasLineInfo(casStringMaking.get(i).label, true));
				} else {
					casStringMaking.get(i + 1).label = casStringMaking.get(i).label;
					tempCasMaking.add(casStringMaking.get(i + 1));
					i++;
				}
			} else {
				tempCasMaking.add(casStringMaking.get(i));
			}
			i++;
		}
		casStringMaking = tempCasMaking;
	}
	//	casStringMakingに文字列を追加(サブプログラムの場合はcasStringMakingSub)
	private void addMakingList(CasLineInfo casLineInfo) {
		if (isInSubProgram) {
			casStringMakingSub.add(casLineInfo);
		} else {
			casStringMaking.add(casLineInfo);
		}
	}
	//	casStringMakingにリストを追加(サブプログラムの場合はcasStringMakingSub)
	private void addAllMakingList(List<CasLineInfo> casStrings) {
		if (isInSubProgram) {
			casStringMakingSub.addAll(casStrings);
		} else {
			casStringMaking.addAll(casStrings);
		}
		if (casStrings == currCasLines) {
			currCasLines = new ArrayList<CasLineInfo>();
		}
	}
	// 変数の基準位置を求め、処理を実行するコードを記述。
	private void setVariable(String op, int setGR, List<CasLineInfo> casStrings, boolean isGlobal) {
		if (isGlobal) {
			casStrings.add(new CasLineInfo("", op, setGR, "VAR", 3, Integer.parseInt(token.get(tokenPointer)[3])));
		} else {
			casStrings.add(new CasLineInfo("", "ADDA", 3, 4, Integer.parseInt(token.get(tokenPointer)[3])));
			casStrings.add(new CasLineInfo("", op, setGR, 0, 3, Integer.parseInt(token.get(tokenPointer)[3])));
		}
	}
	// 変数情報を所得
	private NameInfo getVariableInfo(String variableName) {
		if (isInSubProgram) {
			if (subNameHash.get(variableName).isGlobal) {
				return nameHash.get(variableName);
			}
			return subNameHash.get(variableName);
		} else {
			return nameHash.get(variableName);
		}
	}
	// 比較処理の記述
	private void compare() {
		String op = (checkMode == 0) ? "CPA" : "CPL";
		if (useGr == 1) {
			currCasLines.add(new CasLineInfo("", op, 1, 2, Integer.parseInt(token.get(tokenPointer)[3])));
		} else {
			currCasLines.add(new CasLineInfo("", op, 2, 1, Integer.parseInt(token.get(tokenPointer)[3])));
		}
	}
	// Hashがnullであるか検査
	private boolean isHashNull(Map<String, NameInfo> hashMap, String string) {
		return hashMap.get(string) == null;
	}
	
	
	
	
	

	// 整数かどうかチェック
	private void checkInt() {
		// 符号
		if (Integer.parseInt(token.get(tokenPointer)[2]) == 30 || Integer.parseInt(token.get(tokenPointer)[2]) == 31) {
			addTokenChild();
		}
		// 符号なし整数
		checkReservedWord(44);
	}
	// 符号なし整数かどうかチェック
	/* checkReservedWord(44)でええやんけ!!!!!!!!!!!!!!!!!!!!!!
	private void checkUnsignedInt() {
		if (Integer.parseInt(Token.get(tokenPointer)[2]) != 44) {
			errAction(Integer.parseInt(Token.get(tokenPointer)[3]));
		}
		addTokenChild();
	}
	*/
	
	// 識別子かどうかチェック
	/* checkReservedWord(43)でええやんけ!!!!!!!!!!!!!!!!!!!!!!
	private void checkIdentifier() {
		if (Integer.parseInt(Token.get(tokenPointer)[2]) != 43) {
			errAction(Integer.parseInt(Token.get(tokenPointer)[3]));
		}
		addTokenChild();
		programAST.ignoreNextChild();
	}
	*/
	private void checkReservedWord(int id) {
		if (Integer.parseInt(token.get(tokenPointer)[2]) != id) {
			synErrAction();
		}
		addTokenChild();
	}
	
	
	
	
	
	
	private void addTokenChild() {
		programAST.addChild(Integer.parseInt(token.get(tokenPointer)[2]), token.get(tokenPointer)[0]);
		incTokenPointer();
		programAST.ignoreNextChild();
	}
	
	
	private boolean isStandardType() {
		return Integer.parseInt(token.get(tokenPointer)[2]) == 3 || Integer.parseInt(token.get(tokenPointer)[2]) == 4
				|| Integer.parseInt(token.get(tokenPointer)[2]) == 11;
	}
	// 乗法演算子
	private boolean isLikeMultiplicativeOperator() {
		return Integer.parseInt(token.get(tokenPointer)[2]) == 32 || Integer.parseInt(token.get(tokenPointer)[2]) == 5
				|| Integer.parseInt(token.get(tokenPointer)[2]) == 12 || Integer.parseInt(token.get(tokenPointer)[2]) == 0;
	}
	// 加法演算子
	private boolean isLikeAdditiveOperator() {
		return Integer.parseInt(token.get(tokenPointer)[2]) == 30 || Integer.parseInt(token.get(tokenPointer)[2]) == 31
				|| Integer.parseInt(token.get(tokenPointer)[2]) == 15;
	}
	// 関係演算子
	private boolean isRelationalOperator() {
		return 24 <= Integer.parseInt(token.get(tokenPointer)[2]) && Integer.parseInt(token.get(tokenPointer)[2]) <= 29;
	}
	// int用演算子
	private boolean isOperatorUsedInteger() {
		return Integer.parseInt(token.get(tokenPointer)[2]) == 30 || Integer.parseInt(token.get(tokenPointer)[2]) == 31
				|| Integer.parseInt(token.get(tokenPointer)[2]) == 32 || Integer.parseInt(token.get(tokenPointer)[2]) == 5
				|| Integer.parseInt(token.get(tokenPointer)[2]) == 12;
	}
	// boolean用演算子
	private boolean isOperatorUsedBoolean() {
		return Integer.parseInt(token.get(tokenPointer)[2]) == 0 || Integer.parseInt(token.get(tokenPointer)[2]) == 15;
	}
	// tokenPointerの増加
	private void incTokenPointer() {
		if (tokenPointer + 1 >= token.size()) {
			return;
		}
		if (!lastFlag && Integer.parseInt(token.get(tokenPointer + 1)[2]) == -1) {
			synErrAction();
		}
		tokenPointer++;
		if (lastFlag && Integer.parseInt(token.get(tokenPointer)[2]) != -1) {
			synErrAction();
		}
	}
	
	
	
	public AST getAST() {
		return programAST;
	}
	
	public Map<String, NameInfo> getNameHash(){
		return nameHash;
	}
	
	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e){
			return 0;
		}
	}
	
}












