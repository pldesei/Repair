package cofix.gz;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.util.JavaFile;

public class MethodVisitor extends ASTVisitor{
	private Method m;
	private int start = -1;
	private int end = -1;
	private List<Method> allMethods = new Vector<Method>();
	private List<MethodDeclaration> allDeclas = new Vector<MethodDeclaration>();
	CompilationUnit unit;
	
	public MethodVisitor(int start, int end, CompilationUnit unit) {
		this.start = start;
		this.end = end;
		this.unit = unit;
	}
	
	public MethodVisitor(CompilationUnit unit) {
		this.unit = unit;
	}
	
	public static void main(String[] args) throws Exception {
		CompilationUnit cu = JavaFile.genASTFromFile("/root/BugRepair/defects4j/buggyVersions/chart/chart_6_buggy/source/org/jfree/chart/util/ShapeList.java");
		MethodVisitor v = new MethodVisitor(cu);
		cu.accept(v);
	}
	
	private boolean hasInterception(int methodStart, int methodEnd){
	    if(methodEnd > start && methodEnd < end){
	        return true;
	    }else if(methodStart > start && methodStart < end){
	        return true;
	    }else if(methodStart <= start && methodEnd >= end){
	        return true;
	    }
	    return false;
	}

	@Override
	public boolean visit(MethodDeclaration md){
		//System.out.println(md.toString());
		//System.out.println(md.getReturnType2() + " " + md.getName() + " " + md.parameters() + " " + md.thrownExceptionTypes());
		int startLine = unit.getLineNumber(md.getStartPosition());
		int endLine = unit.getLineNumber(md.getStartPosition() + md.getLength());
		//System.out.println(startLine + " " + endLine + ":" + md.getName().toString());
		if (this.start == -1 && this.end == -1) {
			Method method = null;
			if (md.getReturnType2() == null) {
				method = new Method(md.getName().toString(), "null");
			}
			else
				method = new Method(md.getName().toString(), md.getReturnType2().toString());
			List<String> paraNames = new ArrayList<String>();
			List<String> throwNames = new ArrayList<String>();
			if (md.parameters() != null) {
			    for(Object p : md.parameters()){
			        paraNames.add(((SingleVariableDeclaration)p).getType().toString()+" "+((SingleVariableDeclaration)p).getName().toString());
			    }
			}
			if (md.thrownExceptionTypes() != null) {
				for (Object name : md.thrownExceptionTypes()) {
					throwNames.add(((Type)name).toString());
				}
			}
			method.setParameters(paraNames);
			method.setThrows(throwNames);
			method.startLine=startLine;
			method.endLine=endLine;
			allMethods.add(method);
			allDeclas.add(md);
		}
		else {
			if(this.m == null && this.hasInterception(startLine, endLine)){
				if (md.getReturnType2() == null) {
					this.m = new Method(md.getName().toString(), "null");
				}
				else
					this.m = new Method(md.getName().toString(), md.getReturnType2().toString());
				List<String> paraNames = new ArrayList<String>();
				List<String> throwNames = new ArrayList<String>();
				if (md.parameters() != null) {
				    for(Object p : md.parameters()){
				        paraNames.add(((SingleVariableDeclaration)p).getType().toString()+" "+((SingleVariableDeclaration)p).getName().toString());
				    }
				}
				if (md.thrownExceptionTypes() != null) {
					for (Object name : md.thrownExceptionTypes()) {
						throwNames.add(((Type)name).toString());
					}
				}
				this.m.setParameters(paraNames);
				this.m.setThrows(throwNames);
				this.m.startLine=startLine;
				this.m.endLine=endLine;
				
				//System.out.println(this.m.toString());
			}
		}
		return true;
	}
	
   public List<Method> getAllMethods() {
	   return allMethods;
   }
   
   public List<MethodDeclaration> getAllDeclas() {
	   return allDeclas;
   }

	public Method getMethod() {
		return m;
	}
}


