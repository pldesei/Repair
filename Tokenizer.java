import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import japa.parser.ASTParserTokenManager;
import japa.parser.JavaCharStream;
import japa.parser.Token;

public class Tokenizer {
	
	int fileCount = 0;
	int tokenCount = 0;
	static String homeFolderPath="/Users/yrr/Desktop/test/";
	static String timeFilePath="/Users/yrr/Desktop/test/chart_11.txt";
	static File timeFile=null;
	
	public static void outputCurrentTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(df.format(new Date()));
	}
	
	public static void main(String[] args) {
		System.out.print("StartTime:");
		outputCurrentTime();
		Tokenizer t = new Tokenizer();
		timeFile=new File(timeFilePath);
		
		t.readFiles(new File(homeFolderPath+"/chart_11_buggy"));
		System.out.print("EndTime:");
		outputCurrentTime();
		System.out.println("File Count:" + t.fileCount);
		System.out.println("Token Count:" + t.tokenCount);
	}
	
	public void readFiles(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File tmpFile : files) {
				readFiles(tmpFile);
			}
		}
		else {
			if (file.getName().endsWith(".java"))
				readFileToToken(file);
		}
	}
	
	private void readFileToToken(File file) {
		fileCount ++;
		ArrayList<String> tokens = new ArrayList<String>();
		try {
			FileInputStream in = new FileInputStream(file);
			ASTParserTokenManager tokenmgr = new ASTParserTokenManager(new JavaCharStream(in));
			Token token = tokenmgr.getNextToken(); 
			while (token != null && !token.image.equals("")) {
				tokens.add(token.image);
				token = tokenmgr.getNextToken();
			}
			//System.out.println(tokens);
			tokenCount += tokens.size();
//			String outputFilePath=homeFolderPath+"tokens/"+file.getAbsolutePath().substring(homeFolderPath.length());
//			outputFilePath=outputFilePath.substring(0,outputFilePath.lastIndexOf(".java"))+".txt";
//			File outputFile=new File(outputFilePath);
//			if (!outputFile.getParentFile().exists())
//				outputFile.getParentFile().mkdirs();
//			if (outputFile.exists())
//				outputFile.delete();
//			outputFile.createNewFile();
			
			//BufferedWriter w = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath() + ".token")));
			BufferedWriter w = new BufferedWriter(new FileWriter(timeFile,true));
			for (String str : tokens) {
				w.write(str + " ");
			}
			w.close();
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		catch (Error e) {
			System.out.println(file.getAbsolutePath());
			System.out.println(e.getMessage());
		}
	}

}
