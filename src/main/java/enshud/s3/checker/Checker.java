package enshud.s3.checker;

import enshud.s2.parser.Parser;



public class Checker {
	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		
		// normalの確認
		new Checker().run("data/ts/normal01.ts");
		new Checker().run("data/ts/normal02.ts");
		
		// synerrの確認
		new Checker().run("data/ts/synerr01.ts");
		new Checker().run("data/ts/synerr02.ts");
		new Checker().run("data/ts/synerr03.ts");
		new Checker().run("data/ts/synerr04.ts");
		new Checker().run("data/ts/synerr05.ts");
		new Checker().run("data/ts/synerr06.ts");
		new Checker().run("data/ts/synerr07.ts");
		new Checker().run("data/ts/synerr08.ts");

		// semerrの確認
		new Checker().run("data/ts/semerr01.ts");
		new Checker().run("data/ts/semerr02.ts");
		new Checker().run("data/ts/semerr03.ts");
		new Checker().run("data/ts/semerr04.ts");
		new Checker().run("data/ts/semerr05.ts");
		new Checker().run("data/ts/semerr06.ts");
		new Checker().run("data/ts/semerr07.ts");
		new Checker().run("data/ts/semerr08.ts");
		//*/
		
		// new Checker().run("data/ts/semerr01.ts");
		
		/*
		new Checker().run("src/main/java/enshud/s2/parser/test7.txt");	// 引数がない関数に引数を渡している
		new Checker().run("src/main/java/enshud/s2/parser/test8.txt");	// 余分な引数がある
		new Checker().run("src/main/java/enshud/s2/parser/test9.txt");	// 引数が足りない
		new Checker().run("src/main/java/enshud/s2/parser/test10.txt");	// 引数のある関数に引数を渡さない
		new Checker().run("src/main/java/enshud/s2/parser/test11.txt");	// 2つの文法的な誤りがある
		new Checker().run("src/main/java/enshud/s2/parser/test12.txt");	// 定義とは異なる型の値を引数として渡す
		new Checker().run("src/main/java/enshud/s2/parser/test13.txt");	// 配列型を引数として渡す
		//*/
	}

	/**
	 * TODO
	 * 
	 * 開発対象となるChecker実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，意味解析を行う．
	 * 意味的に正しい場合は標準出力に"OK"を，正しくない場合は"Semantic error: line"という文字列とともに，
	 * 最初のエラーを見つけた行の番号を標準エラーに出力すること （例: "Semantic error: line 6"）．
	 * また，構文的なエラーが含まれる場合もエラーメッセージを表示すること（例： "Syntax error: line 1"）．
	 * 入力ファイル内に複数のエラーが含まれる場合は，最初に見つけたエラーのみを出力すること．
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力tsファイル名
	 */
	public void run(final String inputFileName) {
		Parser ProgramParser = new Parser();
		ProgramParser.check(inputFileName);
		switch (ProgramParser.printMode) {
		case 0:
			System.out.println("OK");
			break;
		case 1:
			System.err.println("Syntax error: line " + ProgramParser.errFlag);
			break;
		case 2:
			// System.out.println("OK");	// Parserではこうする
			for (int line: ProgramParser.semErrList) {
				System.err.println("Semantic error: line " + line);
			}
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
	
}