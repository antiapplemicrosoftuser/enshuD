package enshud.s2.parser;

import java.util.ArrayList;
import java.util.List;

public class NameInfo {
	public int type;
	public int id;
	public boolean isGlobal;
	public ArrayInfo arrayInfo;
	public List<Integer> parameterTypes;
	public int variableNum;
	NameInfo(int type, int id, boolean isGlobal, ArrayInfo arrayInfo){
		this.type = type;
		this.id = id;
		this.isGlobal = isGlobal;
		this.arrayInfo = arrayInfo;
		if (type == 3) {
			parameterTypes = new ArrayList<Integer>();
		}
	}
}
