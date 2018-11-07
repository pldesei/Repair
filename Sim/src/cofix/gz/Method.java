package cofix.gz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Method implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String returnType;
	private List<String> parameters;
	
	private List<String> throwsException;
	
	public int startLine;
	public int endLine;
	public Method(String name, String returnType){
		  this.name = name;
		  this.returnType = returnType;
		  this.parameters = new ArrayList<String>();
		  this.throwsException = new ArrayList<String>();
	}
	public String getName(){
		return this.name;
	}
	public String returnType(){
		return this.returnType;
	}
	public List<String> getParameters() {
		return parameters;
	}
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	public List<String> getThrows() {
		return throwsException;
	}
	public void setThrows(List<String> throwsException) {
		this.throwsException = throwsException;
	}
	public String toString(){
        return returnType + " "+ name + "(" + parameters + ")" + this.throwsException;
	    
	}
}
