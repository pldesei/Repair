package cofix.gz;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.core.parser.node.CodeBlock;

public class SameMethodFinder {
	
	private CodeBlock buggyBlock;
	private RepoStructure repo;
		
	public SameMethodFinder(CodeBlock codeBlock) {
		this.buggyBlock = codeBlock;
	}
	
	public RepoStructure getRepo() {
		return repo;
	}
	
	private boolean checkIsNull(SameMethods sameMethods) {
		if (sameMethods.allMatch.size() == 0 && sameMethods.typeNamePara.size() == 0
				&& sameMethods.typeName.size() == 0 && sameMethods.namePara.size() == 0
				&& sameMethods.name.size() == 0)
			return true;
		return false;
	}
	
	public List<Pair<CodeBlock, Double>> getSameMethods(String folderPath) {
		getClassFamilies(folderPath);
		Pair<Integer, Integer> range = buggyBlock.getLineRangeInSource();
		SameMethods sameMethods = new SameMethods();
		JavaClass cl = this.getCurrentClass(buggyBlock.getFileName(), range, repo.classes);
		if (cl == null) {
			System.out.println("current class is null");
			return null;
		}
		HashSet<ClassNode> classFamilies = repo.classFamilies;
		ClassNode root = null;
		for (ClassNode classFamily : classFamilies) {
			if (classFamily.containClass(cl)) {
				root = classFamily;
				break;
			}
		}
		if (root == null)
			return null;
		checkSameMethod(root, cl, range, sameMethods);
		if (checkIsNull(sameMethods)) {
			return null;
		}
		List<Pair<CodeBlock, Double>> ret = new ArrayList<>();
		if (sameMethods.allMatch.size() > 0) {
			for (int ii = 0; ii < sameMethods.allMatch.size(); ii++) {
				Pair<CodeBlock, Double> one = new Pair<>();
				one.setSecond(1.0);
				MethodDeclaration md = sameMethods.allMatch.elementAt(ii);
				String path = sameMethods.allPaths.elementAt(ii);
				//System.out.println(path);
				//System.out.println(md.toString());
				CompilationUnit unit = JavaFile.genASTFromFile(path);
				GZCodeSearch cs = new GZCodeSearch(unit, md.getStartPosition());
				CodeBlock codeBlock = new CodeBlock(path, unit, cs.getASTNodes());
				one.setFirst(codeBlock);
				//System.out.println(codeBlock.toSrcString().toString());
				ret.add(one);
			}
		}
		if (sameMethods.typeNamePara.size() > 0) {
			for (int ii = 0; ii < sameMethods.typeNamePara.size(); ii++) {
				Pair<CodeBlock, Double> one = new Pair<>();
				one.setSecond(0.8);
				MethodDeclaration md = sameMethods.typeNamePara.elementAt(ii);
				String path = sameMethods.typeNameParaPaths.elementAt(ii);
				//System.out.println(path);
				//System.out.println(md.toString());
				CompilationUnit unit = JavaFile.genASTFromFile(path);
				GZCodeSearch cs = new GZCodeSearch(unit, md.getStartPosition());
				CodeBlock codeBlock = new CodeBlock(path, unit, cs.getASTNodes());
				one.setFirst(codeBlock);
				//System.out.println(codeBlock.toSrcString().toString());
				ret.add(one);
			}
		}
		if (sameMethods.typeName.size() > 0) {
			for (int ii = 0; ii < sameMethods.typeName.size(); ii++) {
				Pair<CodeBlock, Double> one = new Pair<>();
				one.setSecond(0.6);
				MethodDeclaration md = sameMethods.typeName.elementAt(ii);
				String path = sameMethods.typeNamePaths.elementAt(ii);
				//System.out.println(path);
				//System.out.println(md.toString());
				CompilationUnit unit = JavaFile.genASTFromFile(path);
				GZCodeSearch cs = new GZCodeSearch(unit, md.getStartPosition());
				CodeBlock codeBlock = new CodeBlock(path, unit, cs.getASTNodes());
				one.setFirst(codeBlock);
				//System.out.println(codeBlock.toSrcString().toString());
				ret.add(one);
			}
		}
		if (sameMethods.namePara.size() > 0) {
			for (int ii = 0; ii < sameMethods.namePara.size(); ii++) {
				Pair<CodeBlock, Double> one = new Pair<>();
				one.setSecond(0.4);
				MethodDeclaration md = sameMethods.namePara.elementAt(ii);
				String path = sameMethods.nameParaPathes.elementAt(ii);
				//System.out.println(path);
				//System.out.println(md.toString());
				CompilationUnit unit = JavaFile.genASTFromFile(path);
				GZCodeSearch cs = new GZCodeSearch(unit, md.getStartPosition());
				CodeBlock codeBlock = new CodeBlock(path, unit, cs.getASTNodes());
				one.setFirst(codeBlock);
				//System.out.println(codeBlock.toSrcString().toString());
				ret.add(one);
			}
		}
		if (sameMethods.name.size() > 0) {
			for (int ii = 0; ii < sameMethods.name.size(); ii++) {
				Pair<CodeBlock, Double> one = new Pair<>();
				one.setSecond(0.2);
				MethodDeclaration md = sameMethods.name.elementAt(ii);
				String path = sameMethods.namePaths.elementAt(ii);
				//System.out.println(path);
				//System.out.println(md.toString());
				CompilationUnit unit = JavaFile.genASTFromFile(path);
				GZCodeSearch cs = new GZCodeSearch(unit, md.getStartPosition());
				CodeBlock codeBlock = new CodeBlock(path, unit, cs.getASTNodes());
				one.setFirst(codeBlock);
				//System.out.println(codeBlock.toSrcString().toString());
				ret.add(one);
			}
		}
		return ret;
	}
	
	private void checkSameMethod(ClassNode root, JavaClass cl, Pair<Integer, Integer> range, SameMethods sameMethods) {
		CompilationUnit cu = JavaFile.genASTFromFile(new File(cl.filePath));
		MethodVisitor v = new MethodVisitor(range.getFirst(), range.getSecond(), cu);
		cu.accept(v);
		if (v.getMethod() == null) {
			return;
		}
		Method method = v.getMethod();
		again(root, cl, method, sameMethods);
	}
	
	private void again(ClassNode root, JavaClass cl, Method methodStandard, SameMethods sameMethods) {
		if (root.curJavaClass.equals(cl)) {}
		else {
			CompilationUnit cu = JavaFile.genASTFromFile(new File(root.curJavaClass.filePath));
			MethodVisitor v = new MethodVisitor(cu);
			cu.accept(v);
			String standardSign = methodStandard.toString();
			String standardName = methodStandard.getName();
			List<String> standardParas = methodStandard.getParameters();
			String standardType = methodStandard.returnType();
			
			for (int i = 0; i < v.getAllMethods().size(); i++) {
				Method method = v.getAllMethods().get(i);
				MethodDeclaration methodDecla = v.getAllDeclas().get(i);
				if (method.toString().equals(standardSign)) {
					sameMethods.allMatch.add(methodDecla);
					sameMethods.allPaths.add(root.curJavaClass.filePath);
					continue;
				}
				String thisName = method.getName();
				String thisType = method.returnType();
				List<String> thisPara = method.getParameters();
				if (thisName.equals(standardName) && thisType.equals(standardType) && thisPara.toString().equals(standardParas.toString())) {
					sameMethods.typeNamePara.add(methodDecla);
					sameMethods.typeNameParaPaths.add(root.curJavaClass.filePath);
					continue;
				}
				if (thisName.equals(standardName) && thisType.equals(standardType)) {
					sameMethods.typeName.add(methodDecla);
					sameMethods.typeNamePaths.add(root.curJavaClass.filePath);
					continue;
				}
				if (thisName.equals(standardName) && thisPara.toString().equals(standardParas.toString())) {
					sameMethods.namePara.add(methodDecla);
					sameMethods.nameParaPathes.add(root.curJavaClass.filePath);
					continue;
				}
				if (thisName.equals(standardName)) {
					sameMethods.name.add(methodDecla);
					sameMethods.namePaths.add(root.curJavaClass.filePath);
					continue;
				}
			}
		}
		if (root.children != null && root.children.size() > 0) {
			for (ClassNode n : root.children) {
				again(n, cl, methodStandard, sameMethods);
			}
		}
	}
	
	private JavaClass getCurrentClass(String filePath, Pair<Integer, Integer> range, List<JavaClass> classes) {
		JavaClass currentClass = null;
		int start = range.getFirst();
		int end = range.getSecond();
		for (JavaClass javaClass : classes) {
			if (javaClass.filePath.equals(filePath) && fragClassIntersect(start, end, javaClass)) {
				if (currentClass == null || hasSmallerClassRange(javaClass, currentClass))
					currentClass = javaClass;
			}
		}
		return currentClass;
	}
	
	private boolean fragClassIntersect(int start, int end, JavaClass javaClass) {
		if (javaClass.classStart <= start && start <= javaClass.classEnd)
			return true;
		if (javaClass.classStart <= end && end <= javaClass.classEnd)
			return true;
		return false;
	}

	private boolean hasSmallerClassRange(JavaClass classA, JavaClass classB) {
		if (classB.classStart <= classA.classStart && classA.classEnd <= classB.classEnd)
			return true;
		return false;
	}
	
	private void getClassFamilies(String folderPath) {
		repo = new RepoStructure(folderPath);
		repo.analyzeStructure();
		repo.outputClassFamilies();
	}

}

class SameMethods {
	Vector<String> allPaths = new Vector<String>();
	Vector<MethodDeclaration> allMatch = new Vector<MethodDeclaration>();
	Vector<String> typeNameParaPaths = new Vector<String>();
	Vector<MethodDeclaration> typeNamePara = new Vector<MethodDeclaration>();
	Vector<String> typeNamePaths = new Vector<String>();
	Vector<MethodDeclaration> typeName = new Vector<MethodDeclaration>();
	Vector<String> nameParaPathes = new Vector<String>();
	Vector<MethodDeclaration> namePara = new Vector<MethodDeclaration>();
	Vector<String> namePaths = new Vector<String>();
	Vector<MethodDeclaration> name = new Vector<MethodDeclaration>();
}
