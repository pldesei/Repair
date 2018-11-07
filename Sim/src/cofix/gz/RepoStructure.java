package cofix.gz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;

public class RepoStructure implements Serializable {
	private static final long serialVersionUID = 1L;

	String repoPath;
	public List<JavaClass> classes;
	public HashSet<ClassNode> classFamilies;
	private HashMap<JavaClass, ClassNode> classNodeMap;

	public RepoStructure(String repoPath) {
		this.repoPath = repoPath;
		classes = new ArrayList<JavaClass>();
		classFamilies = new HashSet<ClassNode>();
		classNodeMap = new HashMap<JavaClass, ClassNode>();
	}

	public void analyzeStructure() {
		List<String> allJavaFile = new ArrayList<String>();
		getAllJavas(repoPath, allJavaFile);
		for (String javaPath : allJavaFile) {
			analyzeJavaFile(javaPath);
		}

		for (JavaClass javaClass : classes) {
			javaClass.setFullSuperClassName(classes);
			ClassNode classNode = new ClassNode(javaClass);
			classNodeMap.put(javaClass, classNode);
		}

		ClassNode classFamilyRoot = null;
		for (JavaClass javaClass : classes) {
			ClassNode parentNode = classNodeMap.get(javaClass.getSuperClass(classes));
			if (parentNode != null)
				parentNode.children.add(classNodeMap.get(javaClass));
			else {
				classFamilyRoot = classNodeMap.get(javaClass);
				classFamilyRoot.hashCode();
				
				classFamilies.add(classFamilyRoot);
			}
		}
	}

	public void outputClassFamilies() {
		try {
			File file = new File(this.repoPath + "/classFamilies.txt");
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (ClassNode rootNode : classFamilies) {
				if (rootNode.children.size() > 0) {
					String familyInfo = "*****************\n";
					familyInfo += rootNode.toString();
					familyInfo += "\n*****************\n";
					bw.write(familyInfo);
				}
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void writeObj(File objFile) {
		outputClassFamilies();//For Test
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(objFile));
			oos.writeObject(this);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getAllJavas(String path, List<String> allJavaFile) {
		File file = new File(path);
		if (!file.isDirectory()) {
			if (path.endsWith(".java")) {
				allJavaFile.add(file.getAbsolutePath());
			}
		} else {
			for (File subDir : file.listFiles()) {
				getAllJavas(subDir.getAbsolutePath(), allJavaFile);
			}
		}
	}

	private void analyzeJavaFile(String javaPath) {
		try {
			CompilationUnit cu = JavaParser.parse(new File(javaPath));
			PackageDeclaration packageDec = cu.getPackage();
			String packageName = null;
			if (packageDec != null) {
				String pkgLine = packageDec.toString().trim();
				packageName = pkgLine.substring(8, pkgLine.length() - 1).trim();
			}

			List<ImportDeclaration> imports = cu.getImports();
			if (imports == null) {
				imports = new ArrayList<ImportDeclaration>();
			}
			List<String> strImports = new ArrayList<String>();
			for (ImportDeclaration imp : imports) {
				strImports.add(imp.toString());
			}

			List<TypeDeclaration> types = cu.getTypes();
			if (types == null)
				return;
			for (TypeDeclaration type : types) {
				if (type instanceof ClassOrInterfaceDeclaration) {
					ClassOrInterfaceDeclaration classDec = (ClassOrInterfaceDeclaration) type;
					String className = classDec.getName();
					String superClassName = null;
					List<ClassOrInterfaceType> extendList = classDec.getExtends();
					if (extendList != null)
						superClassName = extendList.get(0).getName();
					JavaClass javaClass = new JavaClass(javaPath, packageName, strImports, className, superClassName);
					javaClass.setClassRange(classDec.getBeginLine(), classDec.getEndLine());				
					this.classes.add(javaClass);
				}
			}
		} catch (Exception e) {
			// test files in eclipse may encounter a parser exception
			//System.out.println("ParserExceptionFilePath:" + javaPath);
			//e.printStackTrace();
		}catch(Error e){
			//source files in ant may encounter a parser exception or error
			//System.out.println("ParserErrorFilePath:" + javaPath);
			//e.printStackTrace();
		}
	}
}
