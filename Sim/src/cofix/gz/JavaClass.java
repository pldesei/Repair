package cofix.gz;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JavaClass implements Serializable {
	private static final long serialVersionUID = 1L;
	private String pathSep = File.separator;

	public String filePath;
	private String packageName;
	public List<String> imports;
	public String className, superClassName;

	public String fullClassName;
	public String fullSuperClassName;

	public int classStart, classEnd;

	public JavaClass(String filePath, String packageName, List<String> imports, String className,
			String superClassName) {
		this.filePath = filePath;
		this.packageName = packageName;
		this.imports = imports;
		this.className = className;
		this.superClassName = superClassName;
		setFullClassName();
	}

	public void setClassRange(int classStart, int classEnd) {
		this.classStart = classStart;
		this.classEnd = classEnd;
	}

	private void setFullClassName() {
		if (this.packageName == null)
			this.fullClassName = this.className;
		else
			this.fullClassName = packageName + "." + className;
	}

	public void setFullSuperClassName(List<JavaClass> classes) {
		if (superClassName == null) {
			this.fullSuperClassName = null;
			return;
		}
		List<String> possibleSuperPkgList = new ArrayList<String>();
		String possibleSuperFullNameImported = null;
		String possibleSuperFullNameSamePkg = null;
		if (this.packageName != null)
			possibleSuperFullNameSamePkg = this.packageName + "." + this.superClassName;
		else
			possibleSuperFullNameSamePkg = this.superClassName;
		for (String imp : imports) {
			String str = imp.trim();
			String impName = str.substring(7, str.length() - 1);
			if (impName.endsWith(".*")) {
				possibleSuperPkgList.add(impName.substring(0, impName.length() - 2));
			} else if (impName.endsWith("." + this.superClassName)) {
				//possibleSuperFullNameImported = impName.substring(0, impName.length() - 1);
				possibleSuperFullNameImported = impName;
			}
		}

		for (JavaClass javaClass : classes) {
			if (javaClass.fullClassName.equals(possibleSuperFullNameSamePkg)) {
				this.fullSuperClassName = possibleSuperFullNameSamePkg;
				return;
			} else if (possibleSuperFullNameImported != null
					&& javaClass.fullClassName.equals(possibleSuperFullNameImported)) {
				this.fullSuperClassName = possibleSuperFullNameImported;
				return;
			} else if (javaClass.fullClassName.endsWith("." + this.superClassName)) {
				for (String possiblePkg : possibleSuperPkgList) {
					if (javaClass.fullClassName.startsWith(possiblePkg)) {
						this.fullSuperClassName = javaClass.fullClassName;
					}
				} // end of possibleSuperPkgList
			}
		} // end of classes
	}

	public JavaClass getSuperClass(List<JavaClass> classes) {
		JavaClass superClass = null;
		for (JavaClass javaClass : classes) {
			if (javaClass.fullClassName.equals(this.fullSuperClassName)
					&& javaClass.getSrcFolder().equals(this.getSrcFolder())) {
				superClass = javaClass;
			}
		}
		return superClass;
	}

	private String getSrcFolder() {	
		if (this.packageName != null ) {
			String formattedPkgName = this.packageName.replace(".", pathSep);
			if(this.filePath.contains(formattedPkgName))
				return this.filePath.substring(0, this.filePath.indexOf(formattedPkgName));
		}
			return this.filePath.substring(0, this.filePath.lastIndexOf(pathSep));
	}

	@Override
	public boolean equals(Object o) {
		JavaClass javaClass = (JavaClass) o;
		if (this.toString().equals(javaClass.toString()))
			return true;
		return false;
	}

	public int hashCode() {
		return this.toString().hashCode();
	}

	public String toString() {
		return this.filePath + " " + this.className;
	}

	public String getFullName() {
		return this.fullClassName;
	}
}
