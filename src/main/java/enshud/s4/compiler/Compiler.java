package enshud.s4.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import enshud.casl.CaslSimulator;
import enshud.s2.parser.Parser;

public class Compiler {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// Compilerを実行してcasを生成する
		new Compiler().run("data/ts/normal01.ts", "tmp/out01.cas");
		new Compiler().run("data/ts/normal02.ts", "tmp/out02.cas");
		new Compiler().run("data/ts/normal03.ts", "tmp/out03.cas");
		new Compiler().run("data/ts/normal04.ts", "tmp/out04.cas");
		new Compiler().run("data/ts/normal05.ts", "tmp/out05.cas");
		new Compiler().run("data/ts/normal06.ts", "tmp/out06.cas");
		new Compiler().run("data/ts/normal07.ts", "tmp/out07.cas");
		new Compiler().run("data/ts/normal08.ts", "tmp/out08.cas");
		new Compiler().run("data/ts/normal09.ts", "tmp/out09.cas");
		new Compiler().run("data/ts/normal10.ts", "tmp/out10.cas");
		new Compiler().run("data/ts/normal11.ts", "tmp/out11.cas");
		new Compiler().run("data/ts/normal12.ts", "tmp/out12.cas");
		new Compiler().run("data/ts/normal13.ts", "tmp/out13.cas");
		new Compiler().run("data/ts/normal14.ts", "tmp/out14.cas");
		new Compiler().run("data/ts/normal15.ts", "tmp/out15.cas");
		new Compiler().run("data/ts/normal16.ts", "tmp/out16.cas");
		new Compiler().run("data/ts/normal17.ts", "tmp/out17.cas");
		new Compiler().run("data/ts/normal18.ts", "tmp/out18.cas");
		new Compiler().run("data/ts/normal19.ts", "tmp/out19.cas");
		new Compiler().run("data/ts/normal20.ts", "tmp/out20.cas");
		
		// new Compiler().run("src/main/java/enshud/s4/compiler/CompTest03", "tmp/out.cas");

		// 上記casを，CASLアセンブラ & COMETシミュレータで実行する
		CaslSimulator.run("tmp/out.cas", "tmp/out.ans");
	}

	/**
	 * TODO
	 * 
	 * 開発対象となるCompiler実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，CASL IIプログラムにコンパイルする．
	 * コンパイル結果のCASL IIプログラムは第二引数で指定されたcasファイルに書き出すこと．
	 * 構文的もしくは意味的なエラーを発見した場合は標準エラーにエラーメッセージを出力すること．
	 * （エラーメッセージの内容はChecker.run()の出力に準じるものとする．）
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力tsファイル名
	 * @param outputFileName 出力casファイル名
	 */
	public void run(final String inputFileName, final String outputFileName) {

		// TODO
		Parser programParser = new Parser();
		programParser.check(inputFileName);
		switch (programParser.printMode) {
		case 0:
			try {
				Files.write(Paths.get(outputFileName), programParser.casStringToFile);
			} catch (IOException e) {
				System.err.println("file error");
			}
            System.out.println("OK");
			break;
		case 1:
			System.err.println("Syntax error: line " + programParser.errFlag);
			break;
		case 2:
			// System.out.println("OK");	// Parserではこうする
			for (int line: programParser.semErrList) {
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
