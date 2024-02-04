package enshud.s2.parser;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
	public int id;	// 変更(Checkerで使うため)
	public String token;	// 変更(Checkerで使うため)
	public ASTNode parent;	// 変更(Checkerで使うため)
	public List<ASTNode> children;	// 変更(Checkerで使うため)
	int childPointer = 0;

	ASTNode(int id, String token) {
		this.id = id;
		this.token = token;
		parent = null;
		children = new ArrayList<ASTNode>();
	}
}
