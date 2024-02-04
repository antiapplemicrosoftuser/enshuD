package enshud.s2.parser;

import java.util.ArrayList;
import java.util.List;

public class ExpressionInfo {
	public int type;
	/*
	 * 0:	int
	 * 1:	char, str
	 * 2:	boolean
	 */
	public List<CasLineInfo> casStrings;
	public int length;
	ExpressionInfo(int type, List<CasLineInfo> casString, int length){
		this.type = type;
		this.casStrings = new ArrayList<CasLineInfo>();
		this.length = length;
		this.casStrings = casString;
	}
}
