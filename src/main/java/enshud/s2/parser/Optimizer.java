package enshud.s2.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Optimizer {
	private List<DataInfo> stackState; // スタック中の変数を管理(どの変数の値でもない場合は空文字列)
	private List<DataInfo[]> variableInGR; // GRの中の変数状態を管理
	private DataInfo[] currVariableInGR;
	private List<CasLineInfo> casStringMaking; // 作成中のCasの文(全体)を行ごとに格納
	private Map<String, Integer> hashConstantID;
	private Map<List<Integer>, DataInfo> memState;
	private List<Boolean> isNecessaryLine;
	private boolean currIsNecessaryLine;
	private int nextUndefinedID; // 次の未定義の数のID
	private DataInfo dummyDataInfo; // これに参照されることでisValidがfalseにならなくなる。
	private HashSet<Integer> callLineSet;

	Optimizer() {
		stackState = new ArrayList<DataInfo>();
		isNecessaryLine = new ArrayList<Boolean>();
		variableInGR = new ArrayList<DataInfo[]>();
		hashConstantID = new HashMap<String, Integer>();
		memState = new HashMap<List<Integer>, DataInfo>();
		currVariableInGR = new DataInfo[9];
		nextUndefinedID = 0;
		for (int i = 0; i < 9; i++) {
			currVariableInGR[i] = new DataInfo(nextUndefinedID, 0, i);
			nextUndefinedID++;
		}
		dummyDataInfo = new DataInfo(0, -1);
		dummyDataInfo.isValid = true;
		registerRefAndAsn(dummyDataInfo, currVariableInGR[4]);
		registerRefAndAsn(dummyDataInfo, currVariableInGR[6]);
		registerRefAndAsn(dummyDataInfo, currVariableInGR[7]);
		registerRefAndAsn(dummyDataInfo, currVariableInGR[8]);
	}

	public void first() {
		casStringMaking = new ArrayList<CasLineInfo>();
		casStringMaking.add(new CasLineInfo("", "LD", 2, 2, true, 1));
		casStringMaking.add(new CasLineInfo("", "LD", 3, 1, true, 1));
		casStringMaking.add(new CasLineInfo("", "PUSH", "0", 2, 1));
		casStringMaking.add(new CasLineInfo("", "LD", 1, 3, true, 1));
		casStringMaking.add(new CasLineInfo("", "POP", 2, 1));
		casStringMaking.add(new CasLineInfo("", "ST", 2, "VAR", 3, 1));
		casStringMaking.add(new CasLineInfo("", "CALL", "WRTINT", 2));
		casStringMaking.add(new CasLineInfo("", "RET", -1));
		for (CasLineInfo casLineInfo : casStringMaking) {
			System.out.println(casLineInfo.getCasString());
		}
	}

	public void run(List<CasLineInfo> casStringMaking, HashSet<Integer> callLineSet) {
		this.callLineSet = callLineSet;
		/*
		for (CasLineInfo casLineInfo : casStringMaking) {
			System.out.println(casLineInfo.getCasString());
		}
		// */
		for (CasLineInfo casLineInfo : casStringMaking) {
			readCasLine(casLineInfo);
		}
		this.casStringMaking = casStringMaking;
		deleteUnnecessaryLine();
		/*
		 * for (CasLineInfo casLineInfo: this.casStringMaking) {
		 * System.out.println(casLineInfo.getCasString()); } //
		 */
		deleteInvalidReg();
	}

	private void readCasLine(CasLineInfo casLineInfo) {
		currIsNecessaryLine = true;
		if (casLineInfo.label != "") {
			// [ラベルがあった場合は0から5までの全レジスタとメモリをリセット]
			for (int i = 0; i < 6; i++) {
				registerRefAndAsn(dummyDataInfo, currVariableInGR[i]);
			}
			reset(false);
		}
		switch (casLineInfo.op) {
		case "LD":
			readLD(casLineInfo);
			break;
		case "LAD":
			readLAD(casLineInfo);
			break;
		case "ST":
			readST(casLineInfo);
			break;
		case "PUSH":
			readPUSH(casLineInfo);
			break;
		case "POP":
			readPOP(casLineInfo);
			break;
		case "ADDA":
			readCULA(casLineInfo, 0);
			break;
		case "SUBA":
			readCULA(casLineInfo, 1);
			break;
		case "AND":
			readCULA(casLineInfo, 2);
			break;
		case "OR":
			readCULA(casLineInfo, 3);
			break;
		case "XOR":
			readCULA(casLineInfo, 4);
			break;
		case "CPA":
			// ここで参照されたレジスタは絶対参照される
			readCP(casLineInfo);
			break;
		case "CPL":
			// ここで参照されたレジスタは絶対参照される
			readCP(casLineInfo);
			break;
		case "CALL":
			readCALL(casLineInfo);
			break;
		case "RET":
			readRET(casLineInfo);
			break;
		}
		// [currVariableInGRを全てvariableInGRに移動させる]
		DataInfo[] tempVariableInGR = new DataInfo[9];
		for (int i = 0; i < 9; i++) {
			tempVariableInGR[i] = currVariableInGR[i];
		}
		variableInGR.add(tempVariableInGR);
		isNecessaryLine.add(currIsNecessaryLine);
	}

	private void readLD(CasLineInfo casLineInfo) {
		DataInfo tempRegInfo = null;
		List<Integer> key;
		DataInfo data; // keyで取得した値
		switch (casLineInfo.lineType) {
		case 0: // LD GR1, GR2
			if (isSameValue(currVariableInGR[casLineInfo.gr1], currVariableInGR[casLineInfo.gr2])) {
				currIsNecessaryLine = false;
				return;
			}
			invalidData(currVariableInGR[casLineInfo.gr1]);
			if (currVariableInGR[casLineInfo.gr2].valueType == 0) {
				currVariableInGR[casLineInfo.gr1] = new DataInfo(currVariableInGR[casLineInfo.gr2].value,
						casLineInfo.gr1);
			} else {
				currVariableInGR[casLineInfo.gr1] = new DataInfo(currVariableInGR[casLineInfo.gr2].value,
						currVariableInGR[casLineInfo.gr2].addNum, casLineInfo.gr1);
			}
			registerRefAndAsn(currVariableInGR[casLineInfo.gr1], currVariableInGR[casLineInfo.gr2]);
			break;
		case 1: // LD GR1, VAR, GR2
			invalidData(currVariableInGR[casLineInfo.gr1]);
			if (currVariableInGR[casLineInfo.gr2].valueType == 0) {
				key = new ArrayList<Integer>();
				key.add(addressToInt(casLineInfo.xStr));
				key.add(currVariableInGR[casLineInfo.gr2].value);
				data = memState.get(key);
				if (data == null) {
					tempRegInfo = new DataInfo(nextUndefinedID, 0, casLineInfo.gr1);
					memState.put(key, tempRegInfo);
					data = tempRegInfo;
					nextUndefinedID++;
				} else if (data.valueType == 0) {
					tempRegInfo = new DataInfo(data.value, casLineInfo.gr1);
				} else {
					tempRegInfo = new DataInfo(data.value, data.addNum, casLineInfo.gr1);
				}
				registerRefAndAsn(tempRegInfo, data);
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr2]);
				currVariableInGR[casLineInfo.gr1] = tempRegInfo;
			} else {
				tempRegInfo = new DataInfo(nextUndefinedID, 0, casLineInfo.gr1);
				nextUndefinedID++;
				currVariableInGR[casLineInfo.gr1] = tempRegInfo;
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr2]);
			}
			break;
		case 2: // LD GR1, 0, GR2
			invalidData(currVariableInGR[casLineInfo.gr1]);
			// currVariableInGR[casLineInfo.gr2].valueType == 0 は今のところあり得ん。
			key = new ArrayList<Integer>();
			key.add(currVariableInGR[casLineInfo.gr2].value);
			key.add(currVariableInGR[casLineInfo.gr2].addNum + casLineInfo.xInt);
			data = memState.get(key);
			if (data == null) {
				tempRegInfo = new DataInfo(nextUndefinedID, 0, casLineInfo.gr1);
				memState.put(key, tempRegInfo);
				data = tempRegInfo;
				nextUndefinedID++;
				registerRefAndAsn(tempRegInfo, data);
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr2]);
			} else if (data.valueType == 0) {
				tempRegInfo = new DataInfo(data.value, casLineInfo.gr1);
				// Cas書き換え
				casLineInfo.lineType = 4;
				casLineInfo.xInt = data.value;
				casLineInfo.isEqual = true;
				currVariableInGR[casLineInfo.gr1].isValid = true;
			} else {
				tempRegInfo = new DataInfo(data.value, data.addNum, casLineInfo.gr1);
				registerRefAndAsn(tempRegInfo, data);
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr2]);
				currVariableInGR[casLineInfo.gr1] = tempRegInfo;
			}
			break;
		case 4: // LD GR1, =1
			invalidData(currVariableInGR[casLineInfo.gr1]);
			tempRegInfo = new DataInfo(casLineInfo.xInt, casLineInfo.gr1);
			currVariableInGR[casLineInfo.gr1] = tempRegInfo;
			break;
		}
	}

	private void readLAD(CasLineInfo casLineInfo) {
		DataInfo tempRegInfo;
		switch (casLineInfo.lineType) {
		case 1:
			// Casの文法上存在し得るが、今のところここに来ることはない。
			break;
		case 2:
			// Casの文法上存在し得るが、今のところここに来ることはない。
			break;
		case 3: // LAD CHAR0
			tempRegInfo = new DataInfo(addressToInt(casLineInfo.xStr), 0, casLineInfo.gr1);
			currVariableInGR[casLineInfo.gr1] = tempRegInfo;
			break;
		}
	}

	private void readST(CasLineInfo casLineInfo) {
		List<Integer> key;
		DataInfo tempRegInfo;
		switch (casLineInfo.lineType) {
		case 1: // ST GR1, VAR, GR2
			if (currVariableInGR[casLineInfo.gr2].valueType == 0) {
				key = new ArrayList<Integer>();
				key.add(addressToInt(casLineInfo.xStr));
				key.add(currVariableInGR[casLineInfo.gr2].value);
				if (memState.get(key) != null) {
					invalidData(memState.get(key));
				}
				if (currVariableInGR[casLineInfo.gr1].valueType == 0) {
					tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value, -1);
				} else {
					tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value,
							currVariableInGR[casLineInfo.gr1].addNum, -1);
				}
				memState.put(key, tempRegInfo);
				casLineInfo.distData = tempRegInfo;
				registerRefAndAsn(memState.get(key), currVariableInGR[casLineInfo.gr1]);
				registerRefAndAsn(memState.get(key), currVariableInGR[casLineInfo.gr2]);
			} else {
				key = new ArrayList<Integer>();
				key.add(nextUndefinedID);
				key.add(0);
				if (currVariableInGR[casLineInfo.gr1].valueType == 0) {
					tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value, -1);
				} else {
					tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value,
							currVariableInGR[casLineInfo.gr1].addNum, -1);
				}
				memState.put(key, tempRegInfo);
				casLineInfo.distData = tempRegInfo;
				registerRefAndAsn(memState.get(key), currVariableInGR[casLineInfo.gr1]);
				registerRefAndAsn(memState.get(key), currVariableInGR[casLineInfo.gr2]);
				nextUndefinedID++;
			}
			break;
		case 2: // ST GR1, 0, GR2
			key = new ArrayList<Integer>();
			key.add(currVariableInGR[casLineInfo.gr2].value);
			key.add(currVariableInGR[casLineInfo.gr2].addNum + casLineInfo.xInt);
			if (memState.get(key) != null) {
				invalidData(memState.get(key));
			}
			if (currVariableInGR[casLineInfo.gr1].valueType == 0) {
				tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value, -1);
			} else {
				tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value,
						currVariableInGR[casLineInfo.gr1].addNum, -1);
			}
			memState.put(key, tempRegInfo);
			casLineInfo.distData = tempRegInfo;
			registerRefAndAsn(memState.get(key), currVariableInGR[casLineInfo.gr1]);
			registerRefAndAsn(memState.get(key), currVariableInGR[casLineInfo.gr2]);
			break;
		case 3:
			// Casの文法上存在し得るが、今のところここに来ることはない。
			break;
		}
	}

	private void readPUSH(CasLineInfo casLineInfo) {
		// PUSH 0, GR1
		DataInfo tempRegInfo;
		if (currVariableInGR[casLineInfo.gr1].valueType == 0) {
			tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value, -1);
			// Cas書き換え
			casLineInfo.lineType = 6;
			casLineInfo.xStr = Integer.toString(currVariableInGR[casLineInfo.gr1].value);
		} else {
			tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value,
					currVariableInGR[casLineInfo.gr1].addNum, -1);
			registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr1]);
		}
		stackState.add(tempRegInfo);
		casLineInfo.distData = tempRegInfo;
	}

	private void readPOP(CasLineInfo casLineInfo) {
		// POP GR1
		DataInfo tempRegInfo = stackState.get(stackState.size() - 1);
		stackState.remove(stackState.size() - 1);
		invalidData(currVariableInGR[casLineInfo.gr1]);
		tempRegInfo.regNum = casLineInfo.gr1;
		currVariableInGR[casLineInfo.gr1] = tempRegInfo;
	}

	private void readCULA(CasLineInfo casLineInfo, int culcType) {
		DataInfo tempRegInfo;
		int culcResult;
		switch (casLineInfo.lineType) {
		case 0:
			if (currVariableInGR[casLineInfo.gr1].valueType == 1 && currVariableInGR[casLineInfo.gr2].valueType == 1) {
				tempRegInfo = new DataInfo(nextUndefinedID, 0, casLineInfo.gr1);
				nextUndefinedID++;
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr1]);
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr2]);
				currVariableInGR[casLineInfo.gr1] = tempRegInfo;
			} else if (currVariableInGR[casLineInfo.gr1].valueType == 0
					&& currVariableInGR[casLineInfo.gr2].valueType == 0) {
				invalidData(currVariableInGR[casLineInfo.gr1]);
				culcResult = culc(culcType, currVariableInGR[casLineInfo.gr1].value,
						currVariableInGR[casLineInfo.gr2].value);
				tempRegInfo = new DataInfo(culcResult, casLineInfo.gr1);
				// Cas書き換え
				casLineInfo.lineType = 4;
				casLineInfo.op = "LD";
				casLineInfo.xInt = culcResult;
				casLineInfo.isEqual = true;
				currVariableInGR[casLineInfo.gr1] = tempRegInfo;
				currVariableInGR[casLineInfo.gr1].isValid = true;
			} else if (currVariableInGR[casLineInfo.gr1].valueType == 1
					&& currVariableInGR[casLineInfo.gr2].valueType == 0) {
				culcResult = culc(culcType, currVariableInGR[casLineInfo.gr1].addNum,
						currVariableInGR[casLineInfo.gr2].value);
				tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value, culcResult, casLineInfo.gr1);
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr1]);
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr2]);
				currVariableInGR[casLineInfo.gr1] = tempRegInfo;
			} else {
				culcResult = culc(culcType, currVariableInGR[casLineInfo.gr1].value,
						currVariableInGR[casLineInfo.gr2].addNum);
				tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr2].value, culcResult, casLineInfo.gr1);
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr1]);
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr2]);
				currVariableInGR[casLineInfo.gr1] = tempRegInfo;
			}
			break;
		case 4:
			if (currVariableInGR[casLineInfo.gr1].valueType == 0) {
				invalidData(currVariableInGR[casLineInfo.gr1]);
				culcResult = culc(culcType, currVariableInGR[casLineInfo.gr1].value, casLineInfo.xInt);
				tempRegInfo = new DataInfo(culcResult, casLineInfo.gr1);
				// Cas書き換え
				casLineInfo.op = "LD";
				casLineInfo.lineType = 4;
				casLineInfo.xInt = culcResult;
				casLineInfo.isEqual = true;
				currVariableInGR[casLineInfo.gr1] = tempRegInfo;
				currVariableInGR[casLineInfo.gr1].isValid = true;
			} else {
				culcResult = culc(culcType, currVariableInGR[casLineInfo.gr1].addNum, casLineInfo.xInt);
				tempRegInfo = new DataInfo(currVariableInGR[casLineInfo.gr1].value, culcResult, casLineInfo.gr1);
				registerRefAndAsn(tempRegInfo, currVariableInGR[casLineInfo.gr1]);
				currVariableInGR[casLineInfo.gr1] = tempRegInfo;
			}
			break;
		}
	}

	private void readCP(CasLineInfo casLineInfo) {
		// 現状比較はレジスタ同士のみ
		registerRefAndAsn(dummyDataInfo, currVariableInGR[casLineInfo.gr1]);
		registerRefAndAsn(dummyDataInfo, currVariableInGR[casLineInfo.gr2]);
	}

	private void readCALL(CasLineInfo casLineInfo) {
		switch (casLineInfo.xStr) {
		case "MULT":
			DataInfo tempRegInfo;
			if (currVariableInGR[1].valueType == 0 && currVariableInGR[2].valueType == 0) {
				tempRegInfo = new DataInfo(currVariableInGR[1].value * currVariableInGR[2].value, 2);
				// Cas書き換え
				casLineInfo.lineType = 4;
				casLineInfo.op = "LD";
				casLineInfo.gr1 = 2;
				casLineInfo.xInt = tempRegInfo.value;
				casLineInfo.isEqual = true;
				currVariableInGR[2] = tempRegInfo;

			} else {
				tempRegInfo = new DataInfo(nextUndefinedID, 0, casLineInfo.gr1);
				nextUndefinedID++;
				registerRefAndAsn(tempRegInfo, currVariableInGR[1]);
				registerRefAndAsn(tempRegInfo, currVariableInGR[2]);
				currVariableInGR[2] = tempRegInfo;
			}
			break;
		case "DIV":
			DataInfo tempRegInfo2;
			if (currVariableInGR[1].valueType == 0 && currVariableInGR[2].valueType == 0) {
				tempRegInfo = new DataInfo(currVariableInGR[1].value / currVariableInGR[2].value, 2);
				tempRegInfo2 = new DataInfo(currVariableInGR[1].value % currVariableInGR[2].value, 1);
				registerRefAndAsn(tempRegInfo, currVariableInGR[1]);
				registerRefAndAsn(tempRegInfo, currVariableInGR[2]);
				registerRefAndAsn(tempRegInfo2, currVariableInGR[1]);
				registerRefAndAsn(tempRegInfo2, currVariableInGR[2]);
				currVariableInGR[2] = tempRegInfo;
				currVariableInGR[1] = tempRegInfo2;
			} else {
				tempRegInfo = new DataInfo(nextUndefinedID, 0, 1);
				nextUndefinedID++;
				tempRegInfo2 = new DataInfo(nextUndefinedID, 0, 2);
				nextUndefinedID++;
				registerRefAndAsn(tempRegInfo, currVariableInGR[1]);
				registerRefAndAsn(tempRegInfo, currVariableInGR[2]);
				registerRefAndAsn(tempRegInfo2, currVariableInGR[1]);
				registerRefAndAsn(tempRegInfo2, currVariableInGR[2]);
				currVariableInGR[1] = tempRegInfo;
				currVariableInGR[2] = tempRegInfo2;
			}
			break;
		case "WRTINT":
			registerRefAndAsn(dummyDataInfo, currVariableInGR[2]);
			break;
		case "WRTCH":
			registerRefAndAsn(dummyDataInfo, currVariableInGR[2]);
			break;
		case "WRTBOL":
			registerRefAndAsn(dummyDataInfo, currVariableInGR[2]);
			break;
		case "WRTSTR":
			registerRefAndAsn(dummyDataInfo, currVariableInGR[1]);
			registerRefAndAsn(dummyDataInfo, currVariableInGR[2]);
			break;
		case "WRTLN":
			break;
		default:
			for (int i = 0; i < 6; i++) {
				invalidData(currVariableInGR[i]);
			}
			reset(false);
		}
	}

	private void readRET(CasLineInfo casLineInfo) {
		for (int i = 0; i < 6; i++) {
			invalidData(currVariableInGR[i]);
		}
		reset(casLineInfo.endFlag);
	}

	// 不要な行を削除
	private void deleteUnnecessaryLine() {
		List<CasLineInfo> tempCasStringMaking = new ArrayList<CasLineInfo>();
		List<DataInfo[]> tempVariableInGR = new ArrayList<DataInfo[]>();
		for (int i = 0; i < isNecessaryLine.size(); i++) {
			if (callLineSet.contains(casStringMaking.get(i).line)) {
				tempCasStringMaking.add(casStringMaking.get(i));
				tempVariableInGR.add(variableInGR.get(i));
				continue;
			}
			if (!isNecessaryLine.get(i)) {
				continue;
			}
			tempCasStringMaking.add(casStringMaking.get(i));
			tempVariableInGR.add(variableInGR.get(i));
		}
		casStringMaking = tempCasStringMaking;
		variableInGR = tempVariableInGR;
	}

	// 無効なレジスタに代入している行を削除
	private void deleteInvalidReg() {
		List<CasLineInfo> tempCasStringMaking = new ArrayList<CasLineInfo>();
		for (int i = 0; i < casStringMaking.size(); i++) {
			if (callLineSet.contains(casStringMaking.get(i).line)) {
				tempCasStringMaking.add(casStringMaking.get(i));
				continue;
			}
			switch (casStringMaking.get(i).op) {
			case "LD":
				if (!variableInGR.get(i)[casStringMaking.get(i).gr1].isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			case "LAD":
				if (!variableInGR.get(i)[casStringMaking.get(i).gr1].isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			case "ST":
				if (!casStringMaking.get(i).distData.isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			case "PUSH":
				if (!casStringMaking.get(i).distData.isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			case "POP":
				if (!variableInGR.get(i)[casStringMaking.get(i).gr1].isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			case "ADDA":
				if (!variableInGR.get(i)[casStringMaking.get(i).gr1].isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			case "SUBA":
				if (!variableInGR.get(i)[casStringMaking.get(i).gr1].isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			case "AND":
				if (!variableInGR.get(i)[casStringMaking.get(i).gr1].isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			case "OR":
				if (!variableInGR.get(i)[casStringMaking.get(i).gr1].isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			case "XOR":
				if (!variableInGR.get(i)[casStringMaking.get(i).gr1].isValid) {
					putLabel(i, tempCasStringMaking);
					continue;
				}
				break;
			}
			tempCasStringMaking.add(casStringMaking.get(i));
		}
		casStringMaking = tempCasStringMaking;
	}

	private void invalidData(DataInfo registerInfo) {
		if (registerInfo.references.size() == 0) {
			registerInfo.isValid = false;
			for (DataInfo assignmentFrom : registerInfo.assignmentFrom) {
				invalidData(assignmentFrom);
			}
			return;
		}
		for (DataInfo reference : registerInfo.references) {
			if (reference.isValid) {
				return;
			}
		}
		registerInfo.isValid = false;
		for (DataInfo assignmentFrom : registerInfo.assignmentFrom) {
			invalidData(assignmentFrom);
		}
	}

	private void validData(DataInfo registerInfo) {
		registerInfo.isValid = true;
		for (DataInfo parentInfo : registerInfo.assignmentFrom) {
			if (!parentInfo.isValid) {
				validData(parentInfo);
			}
		}
	}

	private int addressToInt(String address) {
		if (hashConstantID.get(address) == null) {
			hashConstantID.put(address, nextUndefinedID);
			nextUndefinedID++;
		}
		return hashConstantID.get(address);
	}

	private boolean isSameValue(DataInfo gReg1, DataInfo gReg2) {
		if (gReg1 == null || gReg2 == null) {
			return false;
		}
		if (gReg1.valueType == gReg2.valueType && gReg1.value == gReg2.value && gReg1.addNum == gReg2.addNum) {
			return true;
		}
		return false;
	}

	private void registerRefAndAsn(DataInfo childReg, DataInfo parentReg) {
		parentReg.references.add(childReg);
		childReg.assignmentFrom.add(parentReg);
		validData(parentReg);
	}

	// 計算
	private int culc(int culcType, int num1, int num2) {
		switch (culcType) {
		case 0: // +
			return num1 + num2;
		case 1: // -
			return num1 - num2;
		case 2: // and
			return num1 & num2;
		case 3: // or
			return num1 | num2;
		case 4: // xor
			return num1 ^ num2;
		default:
			return num1;
		}
	}

	// レジスタとメモリのリセット
	private void reset(boolean endFlag) {
		for (int i = 0; i < 6; i++) {
			if (i == 4) {
				continue;
			}
			currVariableInGR[i] = new DataInfo(nextUndefinedID, 0, i);
			nextUndefinedID++;
		}
		int regNum;
		DataInfo tempDataInfo;
		for (Map.Entry<List<Integer>, DataInfo> entry : memState.entrySet()) {
			if (endFlag) {
				invalidData(entry.getValue());
			} else {
				validData(entry.getValue());
				registerRefAndAsn(dummyDataInfo, entry.getValue());
				regNum = entry.getValue().regNum;
				tempDataInfo = new DataInfo(nextUndefinedID, 0, regNum);
				entry.setValue(tempDataInfo);
				nextUndefinedID++;
			}
		}
	}

	private void putLabel(int index, List<CasLineInfo> tempCasStringMaking) {
		if (casStringMaking.get(index).label != "") {
			if (casStringMaking.get(index + 1).label != "") {
				tempCasStringMaking.add(new CasLineInfo(casStringMaking.get(index).label, true));
			} else {
				casStringMaking.get(index + 1).label = casStringMaking.get(index).label;
			}
		}
	}

	public List<CasLineInfo> getCasMakingList() {
		return casStringMaking;
	}
}
