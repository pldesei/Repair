package cofix.gz;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class GZCodeSearch {
	
	CompilationUnit unit;
	int startPosition;
	List<ASTNode> nodes;
	String methodContent;
	
	public GZCodeSearch(CompilationUnit unit, int startPosition) {
		this.unit = unit;
		this.startPosition = startPosition;
		search();
	}
	
	public List<ASTNode> getASTNodes(){
		return nodes;
	}
	
	private void search() {
		GZMethodVisitor mv = new GZMethodVisitor(unit, startPosition);
		unit.accept(mv);
		nodes = mv.getNodes();
	}
}

class GZMethodVisitor extends ASTVisitor {
	CompilationUnit unit;
	int startPosition;
	List<ASTNode> nodes = new ArrayList<ASTNode>();
	
	public GZMethodVisitor(CompilationUnit unit, int startPosition) {
		this.unit = unit;
		this.startPosition = startPosition;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		int position = node.getStartPosition();
		if (position == startPosition) {
			//nodes.add(node.getBody());
			nodes.addAll(node.getBody().statements());
			//System.out.println("I find the method!");
		}
		return true;
	}
	
	public List<ASTNode> getNodes() {
		return nodes;
	}
}