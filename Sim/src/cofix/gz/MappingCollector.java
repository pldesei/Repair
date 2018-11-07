package cofix.gz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class MappingCollector {
	
	private RepoStructure repo;
	
	private HashMap<ClassNode, List<JavaClass>> rootWithHisChildren = new HashMap<>();
	
	public static void main(String[] args) {
		//All usage is in Repair.java, and _subject is one of the variables in Repair.java
		
		//For GZ search usage:
		//SameMethodFinder sameMethods = new SameMethodFinder(oneBuggyBlock);
		//List<Pair<CodeBlock, Double>> candidates = sameMethods.getSameMethods(_subject.getHome());
		//MappingCollector mc = new MappingCollector(sameMethods.getRepo());
		//mc.getSiblingClasses("XXX");
		
		//For SimFix search usage:
		//MappingCollector mc = new MappingCollector(_subject.getHome());
		//mc.getSiblingClasses("XXX");
	}
	
	public MappingCollector(RepoStructure repo) {
		this.repo = repo;
	}
	
	public MappingCollector(String folderPath) {
		getClassFamilies(folderPath);
	}

	public List<String> getSiblingClasses(String className) {
		List<String> ret = new ArrayList<>();
		HashSet<ClassNode> classFamilies = repo.classFamilies;
		JavaClass currentClass = null;
		for (JavaClass jc : repo.classes) {
			if (jc.className.equals(className)) {
				currentClass = jc;
				ClassNode root = null;
				for (ClassNode classFamily : classFamilies) {
					if (classFamily.containClass(currentClass)) {
						root = classFamily;
						break;
					}
				}
				List<JavaClass> relatives = new ArrayList<>();
				getAllClasses(root, relatives);
				for (JavaClass rjc : relatives) {
					if (rjc.superClassName.equals(currentClass.superClassName)) {
						ret.add(rjc.className);
					}
				}
			}
		}
		return ret;
	}
	
	private void getAllClasses(ClassNode root, List<JavaClass> ret) {
		if (rootWithHisChildren.containsKey(root)) {
			ret = rootWithHisChildren.get(root);
			return;
		}
		ret.add(root.curJavaClass);
		for (ClassNode cn : root.children) {
			if (cn.children.size() > 0) {
				getAllClasses(cn, ret);
			}
		}
		rootWithHisChildren.put(root, ret);
	}
	
	private void getClassFamilies(String folderPath) {
		repo = new RepoStructure(folderPath);
		repo.analyzeStructure();
		repo.outputClassFamilies();
	}
}
