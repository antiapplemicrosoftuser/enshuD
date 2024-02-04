package enshud.s2.parser;

public class CasLineInfo {
	/**
	 * lineType
	 * 
	 * [example]
	 * 0	: LD	GR1, GR2
	 * 1	: LD	GR1, VAR, GR2
	 * 2	: LD	GR1, 0, GR2
	 * 3	: LAD	CHAR0
	 * 4	: LD	GR1, =1
	 * 5	: RET
	 * 6	: CALL	WRTLN
	 * 7	: POP	GR1
	 * 8	: 
	 * 9	: CHAR0	DC	'test'
	 * 10	: PUSH 0, GR1
	 */
	public int lineType;	// 記述形式の種類
	public String label;	// ラベル
	public String op;	// 命令名
	public int gr1, gr2;	// レジスタ(GR2がない場合は-1)
	public boolean isXInt;	// アドレスが数値であるかどうか
	public String xStr;	// アドレス(定数)
	public int xInt;	// アドレス(数値)
	public int line;	// 行番号(書かない場合は-1)
	public boolean isEqual;	// "アドレスに=をつけるか"
	public DataInfo distData;	// STやPUSHの際の格納先の情報
	public boolean endFlag;	// RETの区別
	CasLineInfo(String label, String op, int gr1, int gr2, int line) {
		lineType = 0;
		this.label = label;
		this.op = op;
		this.gr1 = gr1;
		this.gr2 = gr2;
		this.line = line;
	}
	CasLineInfo(String label, String op, int gr1, String x, int gr2, int line) {
		lineType = 1;
		this.label = label;
		this.op = op;
		this.gr1 = gr1;
		this.xStr = x;
		isXInt = false;
		this.gr2 = gr2;
		this.line = line;
	}
	CasLineInfo(String label, String op, int gr1, int x, int gr2, int line) {
		lineType = 2;
		this.label = label;
		this.op = op;
		this.gr1 = gr1;
		this.xInt = x;
		isXInt = true;
		this.gr2 = gr2;
		this.line = line;
	}
	CasLineInfo(String label, String op, int gr1, String x, int line) {
		lineType = 3;
		this.label = label;
		this.op = op;
		this.gr1 = gr1;
		this.xStr = x;
		isXInt = false;
		this.line = line;
	}
	CasLineInfo(String label, String op, int gr1, int x, boolean isEqual, int line) {
		lineType = 4;
		this.label = label;
		this.op = op;
		this.gr1 = gr1;
		this.xInt = x;
		isXInt = true;
		this.isEqual = isEqual;
		this.line = line;
	}
	CasLineInfo(String label, String op, int line) {
		lineType = 5;
		this.label = label;
		this.op = op;
		this.line = line;
	}
	CasLineInfo(String label, String op, int line, boolean endFlag) {
		lineType = 5;
		this.label = label;
		this.op = op;
		this.line = line;
		this.endFlag = endFlag;
	}
	CasLineInfo(String label, String op, String x, int line) {
		lineType = 6;
		this.label = label;
		this.op = op;
		this.xStr = x;
		this.line = line;
	}
	CasLineInfo(String label, String op, int gr1, int line) {
		lineType = 7;
		this.label = label;
		this.op = op;
		this.gr1 = gr1;
		this.line = line;
	}
	CasLineInfo(String label, boolean isNOP) {
		lineType = 8;
		this.label = label;
		if (isNOP) {
			this.op = "NOP";
		}
		this.line = -1;
	}
	CasLineInfo(String label, String op, String x) {
		lineType = 9;
		this.label = label;
		this.op = op;
		try {
			this.xInt = Integer.parseInt(x);
			isXInt = true;
		} catch (NumberFormatException e) {
			this.xStr = x;
			isXInt = false;
		}
		this.line = -1;
	}
	CasLineInfo(String label, String op, String x, int gr1, int line) {
		lineType = 10;
		this.label = label;
		this.op = op;
		this.gr1 = gr1;
		try {
			this.xInt = Integer.parseInt(x);
			isXInt = true;
		} catch (NumberFormatException e) {
			this.xStr = x;
			isXInt = false;
		}
		this.line = line;
	}
	public String getCasString() {
		switch (lineType) {
		case 0:
			return label + "\t" + op + "\tGR" + gr1 + ", " + (gr2 == -1 ? "" : "GR" + gr2) + lineString();
		case 1:
			return label + "\t" + op + "\tGR" + gr1 + ", " + xStr + ", GR" + gr2 + lineString();
		case 2:
			return label + "\t" + op + "\tGR" + gr1 + ", " + xInt + ", GR" + gr2 + lineString();
		case 3:
			return label + "\t" + op + "\tGR" + gr1 + ", " + (isEqual ? "=" + xStr : xStr) + lineString();
		case 4:
			return label + "\t" + op + "\tGR" + gr1 + ", " + (isEqual ? "=" + xInt : xInt) + lineString();
		case 5:
			return label + "\t" + op + lineString();
		case 6:
			return label + "\t" + op + "\t" + xStr + lineString();
		case 7:
			return label + "\t" + op + "\t" + "\tGR" + gr1 + lineString();
		case 8:
			return label + "\t" + op;
		case 9:
			return label + "\t" + op + "\t" + (isXInt ? xInt : xStr);
		case 10:
			return label + "\t" + op + "\t" + (isXInt ? xInt : xStr) + ", " + "\tGR" + gr1 + lineString();
		default:
			return "";
		}
	}
	
	private String lineString() {
		return line == -1 ? "" : "\t; line " + line;
	}
}
