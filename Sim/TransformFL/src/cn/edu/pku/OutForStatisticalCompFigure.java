package cn.edu.pku;

import java.io.File;
import java.util.List;

import cn.edu.pku.util.JavaFile;

/**
 * Output data for figure drawing with separate format for each project
 * @author Jiajun
 *
 */
public class OutForStatisticalCompFigure {

	private static StringBuffer format(String projName, String key, String srcFile) {
		File file = new File(srcFile);
		if(!file.exists()) {
			System.err.println("File (" + srcFile + ") does not exist!");
			return new StringBuffer();
		}
		List<String> content = JavaFile.readFileToStringList(srcFile);
		boolean find = false;
		StringBuffer predictBuff = new StringBuffer();
		for(String string : content) {
			if(string.startsWith(key)) {
				String[] strings = string.split("\t");
				// 21 columns: 4 * 5(algorithms) + 1(id)
				if(strings.length == 21) {
					find = true;
					predictBuff.append(projName + "-predict\n");
					StringBuffer sdBuff = new StringBuffer(projName + "-statistical debugging\n");
					for(int i = 1; i < 18; i += 4) {
						for(int column = 0; column < 4; column ++) {
							String score = strings[i+column];
							int start = score.indexOf("(");
							int end = score.indexOf(")");
							String predict = score.substring(0, start);
							String sd = score.substring(start + 1, end);
							predictBuff.append(predict + ",");
							sdBuff.append(sd + ",");
						}
						predictBuff.append("\n");
						sdBuff.append("\n");
					}
					predictBuff.append("\n\n");
					predictBuff.append(sdBuff);
					predictBuff.append("\n\n");
				}
				break;
			}
		}
		if(!find) {
			System.err.println("Failed to find desired data! (" + projName + ")");
		}
		System.out.println(predictBuff);
		return predictBuff;
	}
	
	public static void main(String[] args) {
		String srcPath = System.getProperty("user.dir") + "/trans/join/line";
		String tarFile = System.getProperty("user.dir") + "/trans/join/line/figure.csv";
		String[] projs = new String[] {"chart","lang","math","time","closure"};
		StringBuffer stringBuffer = new StringBuffer();
		String key = "%top3";
		for(String projName : projs) {
			String srcFile = srcPath + "/" + projName + "/" + projName + "_join.csv";
			stringBuffer.append(OutForStatisticalCompFigure.format(projName, key, srcFile));
		}
		JavaFile.writeStringToFile(tarFile, stringBuffer.toString());
	}
	
}
