package enshud.s4.compiler;

import enshud.casl.CaslSimulator;

public class TestCas {
	public static void main(final String[] args) {
		// 上記casを，CASLアセンブラ & COMETシミュレータで実行する
		CaslSimulator.run("tmp/test1.cas", "tmp/test1.ans");
	}
}
