package cofix.gz;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import cofix.core.parser.NodeUtils;

public class TestCodeSearch {
	
	private CompilationUnit _unit = null;
	private List<ASTNode> _nodes = new ArrayList<>(); 
	private int startLine;
	private int endLine;
	private int _currentLines = 0;
	private int _lineRange;
	private int _extendedLine = 0;
	private Statement _extendedStatement = null;
	
	public List<ASTNode> getASTNodes(){
		return _nodes;
	}
	
	public TestCodeSearch(CompilationUnit unit, int startLine, int endLine) {
		_unit = unit;
		this.startLine = startLine;
		this.endLine = endLine;
		_lineRange = endLine - startLine + 1;
		_extendedLine = startLine;
		search();
	}
	
	private void search(){
		// if the extended line is not given
		if(_extendedStatement == null){
			int position = _unit.getPosition(startLine, 0);
			NodeFinder finder = new NodeFinder(_unit, position, 20);
			ASTNode prefind = finder.getCoveringNode();
			while (prefind != null && !(prefind instanceof Statement)) {
				prefind = prefind.getParent();
			}
			if(prefind != null){
				prefind.accept(new FindExactLineVisitor());
			} else {
				_unit.accept(new Traverse());
			}
			//System.out.println("hello " + this.startLine + ":" + _extendedStatement);
		}
		// extend the statement to meet the requirement
		if(_extendedStatement != null){
			_currentLines = NodeUtils.getValidLineNumber(_extendedStatement);
			if(_lineRange - _currentLines > 0){
				_currentLines = 0;
				_nodes = extend(_extendedStatement);
			} else {
				if(_extendedStatement instanceof Block){
					ASTNode node = _extendedStatement.getParent();
					if (node instanceof IfStatement || node instanceof SwitchCase || node instanceof ForStatement
							|| node instanceof EnhancedForStatement || node instanceof WhileStatement) {
						_extendedStatement = (Statement) node;
					}
				}
				_nodes.add(_extendedStatement);
			}
		}
	}
	
	private List<ASTNode> extend(ASTNode node){
		List<ASTNode> result = new ArrayList<>();
	    List<ASTNode> list = NodeUtils.getAllSiblingNodes(node);
	    int selfIndex = -1;
	    for(int i = 0; i < list.size(); i++){
	    	if(list.get(i) == node){
	    		selfIndex = i;
	    		break;
	    	}
	    }
	    // find self position
	    if(selfIndex != -1){
	    	int left = selfIndex - 1;
	    	int right = selfIndex + 1;
	    	boolean leftExt = true;
	    	boolean rightExt = true;
	    	while(_lineRange - _currentLines > 0){
	    		boolean extended = false;
	    		int leftLine = Integer.MAX_VALUE;
	    		int rightLine = Integer.MAX_VALUE;
	    		/*if(left >= 0 && leftExt){
	    			leftLine = NodeUtils.getValidLineNumber(list.get(left));
	    			if(list.get(left) instanceof SwitchCase){
	    				leftExt = false;
	    			}
	    			if((_currentLines + leftLine - _lineRange) < 5 ){
	    				_currentLines += leftLine;
	    				left --;
	    				extended = true;
	    			}
	    		}*/
	    		if(right < list.size() && rightExt){
	    			rightLine = NodeUtils.getValidLineNumber(list.get(right));
	    			if(list.get(right) instanceof SwitchCase){
	    				rightExt = false;
	    			}
	    			if((_currentLines + rightLine - _lineRange) < 5){
	    				_currentLines += rightLine;
	    				right ++;
	    				extended = true;
	    			}
	    		}
	    		if(!extended){
	    			if(leftLine != Integer.MAX_VALUE || rightLine != Integer.MAX_VALUE){
	    				if(leftLine < rightLine){
	    					_currentLines += leftLine;
	    					left --;
	    				}
	    			}
	    			break;
	    		}
	    	}
			if ((_currentLines - _lineRange) < 5 && _lineRange - _currentLines > 0
					&& !(node.getParent() instanceof MethodDeclaration) && !(node.getParent() instanceof SwitchStatement)) {
				_currentLines = 0;
				result.addAll(extend(node.getParent()));
			} else {
				boolean first = true;
		    	for(int i = left + 1; i < right; i ++){
		    		if(first && left >= 0 && list.get(left) instanceof SwitchCase){
		    			result.add(list.get(left));
		    		}
		    		first = false;
		    		result.add(list.get(i));
		    	}
	    	}
	    } else {
	    	ASTNode parent = node.getParent();
	    	int line = NodeUtils.getValidLineNumber(parent);
	    	if(line < _lineRange){
	    		if(parent instanceof MethodDeclaration){
	    			result.add(node);
	    		} else {
	    			result.addAll(extend(parent));
	    		}
	    	} else {
	    		if(line - _lineRange > 5 || parent instanceof MethodDeclaration){
	    			result.add(node);
	    		} else {
	    			result.add(parent);
	    		}
	    	}
	    }
	    return result;
	}

	class Traverse extends ASTVisitor {

		public boolean visit(MethodDeclaration node){
			
			int start = _unit.getLineNumber(node.getStartPosition());
			int end = _unit.getLineNumber(node.getStartPosition() + node.getLength());
			if(start <= _extendedLine && _extendedLine <= end){
				FindExactLineVisitor visitor = new FindExactLineVisitor();
				node.accept(visitor);
				return false;
			}
			return true;
		}
		
	}
	
	/**
	 * find statement of exact line number
	 * @author Jiajun
	 * @date Jun 14, 2017
	 */
	class FindExactLineVisitor extends ASTVisitor{
		
		public boolean visit(AssertStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(BreakStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(Block node) {
			return true;
		}
		
		public boolean visit(ConstructorInvocation node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(ContinueStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(DoStatement node) {
			int start = _unit.getLineNumber(node.getExpression().getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(EmptyStatement node) {
			return true;
		}
		
		public boolean visit(EnhancedForStatement node) {
			int start = _unit.getLineNumber(node.getExpression().getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(ExpressionStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(ForStatement node) {
			int position = 0;
			if(node.getExpression() != null){
				position = node.getExpression().getStartPosition();
			} else if(node.initializers() != null && node.initializers().size() > 0){
				position = ((ASTNode)node.initializers().get(0)).getStartPosition();
			} else if(node.updaters() != null && node.updaters().size() > 0){
				position = ((ASTNode)node.updaters().get(0)).getStartPosition();
			}
			int start = _unit.getLineNumber(position);
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(IfStatement node) {
			int start = _unit.getLineNumber(node.getExpression().getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(LabeledStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(ReturnStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(SuperConstructorInvocation node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(SwitchCase node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(SwitchStatement node) {
			return true;
		}
		
		public boolean visit(SynchronizedStatement node) {
			return true;
		}
		
		public boolean visit(ThrowStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(TryStatement node) {
			return true;
		}
		
		public boolean visit(TypeDeclarationStatement node){
			return true;
		}
		
		public boolean visit(VariableDeclarationStatement node){
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(WhileStatement node) {
			int start = _unit.getLineNumber(node.getExpression().getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
	}
}