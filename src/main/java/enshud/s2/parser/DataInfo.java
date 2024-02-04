package enshud.s2.parser;

import java.util.HashSet;


/**
 * 
 * @author antia
 *
 *	(代入の際に参照されたレジスタを親レジスタ、このレジスタを参照して代入されたレジスタを子レジスタとする。)
 *
 * メモ
 *	- 参照された場合に新しく生成
 *	- 子レジスタが0または子レジスタがすべて無効の場合、そのレジスタも無効
 *	- レジスタが無効になった場合、親レジスタも無効になっていないか確認する。
 *	- 無効レジスタへの代入行は消してよい。
 *	- 代入以外の行は消せない。
 *	- ADDAなど演算系は定数でない限り1発で不定値。演算対象のレジスタはどちらも子レジスタを得る。	
 */

public class DataInfo {
	public boolean isValid = true;
	/**
	 * valueType
	 * 
	 * 0	: value
	 * 1	: 不明値(不明値ID + addNum)
	 */
	public int valueType;
	/**
	 * value
	 * 
	 * value		: 数値
	 * 不明値		: 不明値ID
	 */
	public int value;
	public int addNum;
	public int regNum;
	public HashSet<DataInfo> assignmentFrom;	// 親レジスタ
	public HashSet<DataInfo> references;	// 子レジスタ
	// 数値
	DataInfo(int value, int regNum){
		this.value = value;
		this.regNum = regNum;
		this.valueType = 0;
		this.assignmentFrom = new HashSet<DataInfo>();
		this.references = new HashSet<DataInfo>();
	}
	// 不明値
	DataInfo(int value, int addNum, int regNum){
		this.value = value;
		this.regNum = regNum;
		this.valueType = 1;
		this.addNum = addNum;
		this.assignmentFrom = new HashSet<DataInfo>();
		this.references = new HashSet<DataInfo>();
	}
	// 他レジスタ
	DataInfo(int valueType, int value, DataInfo assignmentFrom, int regNum){
		if (assignmentFrom.valueType == 0) {	// 数値は前計算するからいらん(定数の場合どうしよ←その場合タイプが2になる)
			assignmentFrom.isValid = false;
		}
		this.value = value;
		this.regNum = regNum;
		this.valueType = valueType;
		this.assignmentFrom = new HashSet<DataInfo>();
		this.assignmentFrom.add(assignmentFrom);
		this.references = new HashSet<DataInfo>();
	}
	DataInfo(int valueType, int value, DataInfo assignmentFrom1, DataInfo assignmentFrom2, int regNum){
		if (assignmentFrom1.valueType == 0) {	// 数値は前計算するからいらん(定数の場合どうしよ←その場合タイプが)
			assignmentFrom1.isValid = false;
		}
		if (assignmentFrom2.valueType == 0) {	// 数値は前計算するからいらん(定数の場合どうしよ←その場合タイプが)
			assignmentFrom2.isValid = false;
		}
		this.value = value;
		this.regNum = regNum;
		this.valueType = valueType;
		this.assignmentFrom = new HashSet<DataInfo>();
		this.assignmentFrom.add(assignmentFrom1);
		this.assignmentFrom.add(assignmentFrom2);
		this.references = new HashSet<DataInfo>();
	}
	// 他レジスタ(アドレス)
	DataInfo(DataInfo assignmentFrom, int regNum){
		this.value = assignmentFrom.value;
		this.regNum = regNum;
		this.valueType = 1;
		this.assignmentFrom = new HashSet<DataInfo>();
		this.assignmentFrom.add(assignmentFrom);
		this.references = new HashSet<DataInfo>();
	}
	public void referencedFrom(DataInfo reference) {
		this.references.add(reference);
	}
}
