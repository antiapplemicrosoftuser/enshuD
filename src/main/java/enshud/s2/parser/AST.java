package enshud.s2.parser;

public class AST {
	public ASTNode CurrentNode;	// 変更(Checkerで使うため)
	public ASTNode root;	// 変更(Checkerで使うため)
	AST(int id, String token){
		root = new ASTNode(id, token);
		CurrentNode = root;
	}
	
	void addChild(int id, String token) {
		ASTNode newChild = new ASTNode(id, token);
		newChild.parent = CurrentNode;
		CurrentNode.children.add(newChild);
	}
	
	public boolean goNextChild() {	// 変更(Checkerで使うため)
		if (CurrentNode.childPointer == CurrentNode.children.size()) {
			return false;
		}
		CurrentNode.childPointer++;
		CurrentNode = CurrentNode.children.get(CurrentNode.childPointer - 1);
		CurrentNode.childPointer = 0;
		return true;
	}
	boolean ignoreNextChild() {
		if (CurrentNode.childPointer >= CurrentNode.children.size()) {
			return false;
		}
		CurrentNode.childPointer++;
		return true;
	}
	public boolean goParent() {	// 変更(Checkerで使うため)
		if (CurrentNode.parent == null) {
			return false;
		}
		CurrentNode = CurrentNode.parent;
		return true;
	}
	private void printNodeInfo() {
		System.out.println(CurrentNode.id + ", " + CurrentNode.token);
	}
	public void reset() {	// 変更(Checkerで使うため)
		CurrentNode = root;
		CurrentNode.childPointer = 0;
	}
	
	void addAndNext(String content) {
		addChild(-1, content);
		goNextChild();
	}
	
	// デバッグ用にASTを出力
	void ASTdebugPrint() {
		reset();
		while (true) {
			printNodeInfo();
			if (goNextChild()) {
				System.out.println("↓");
				continue;
			}
			if (goParent()) {
				System.out.println("↑");
				continue;
			}
			break;
		}
	}
	
}
