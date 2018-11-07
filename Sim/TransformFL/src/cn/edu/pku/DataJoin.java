package cn.edu.pku;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.pku.util.JavaFile;


/**
 * Output the results of predict and predefined methods into one file for comparison convenience
 * precondition: already compute the independent results for these two
 * output: a file contains the comparison of these two methods with the form "X(Y)", 
 * where "X" denotes the result of prediction while "Y" denotes the other. 
 * @author Jiajun
 *
 */
public class DataJoin {

	
	private static void join(String srcFile1, String srcFile2, String tarFile, int last_record_line) {
		List<String> content1 = JavaFile.readFileToStringList(srcFile1);
		List<String> content2 = JavaFile.readFileToStringList(srcFile2);
		if(content1 == null || content1.size() == 0 || content2 == null || content2.size() == 0) {
			System.err.println("Empty content!");
			return ;
		}
//		if(!content1.get(0).equals(content2.get(0))) {
//			System.out.println(content1.get(0));
//			System.out.println(content2.get(0));
//			System.err.println("Different title for two files");
//			return ;
//		}
		int len = content1.get(0).split("\t").length - 1;
		Map<Integer, int[]> rank1 = new HashMap<>();
		Map<Integer, int[]> rank2 = new HashMap<>();
		int[][] improve = new int[2][len + 1];
		int[][] drop = new int[2][len + 1];
		for(int i = 1; i <= len; i ++) {
			rank1.put(i, new int[4]);
			rank2.put(i, new int[4]);
		}
		int start1 = 1;
		int start2 = 1;
		double joinNumber = 0;
		StringBuffer stringBuffer = new StringBuffer(content1.get(0));
		//TODO
		for(; start1 < content1.size() - last_record_line && start2 < content2.size() - last_record_line; ) {
			String string1 = content1.get(start1);
			String string2 = content2.get(start2);
			String[] elems1 = string1.split("\t");
			String[] elems2 = string2.split("\t");
			int id1 = Integer.parseInt(elems1[0]);
			int id2 = Integer.parseInt(elems2[0]);
			if(id1 == id2) {
				joinNumber += 1;
				stringBuffer.append("\n");
				stringBuffer.append(id1);
				int comparibleLoc1 = 10000;
				int comparibleLoc2 = 10000;
				boolean showerror = true;
				for(int i = 1; i < elems1.length; i++) {
					stringBuffer.append("\t");
					int loc1 = Double.valueOf(elems1[i]).intValue();
					int loc2 = Double.valueOf(elems2[i]).intValue();
					stringBuffer.append(loc1 + "(" + loc2 + ")");
					if((i - 1) % 4 == 0) {
						comparibleLoc1 = loc1;
						comparibleLoc2 = loc2;
					}
					if(comparibleLoc1 != comparibleLoc2 && showerror) {
						showerror = false;
						System.err.println("Different original rank : " + id1 + "\n" + srcFile1 + "\n" + srcFile2 + "\n");
					}
					
					if(loc1 == -1) {
					} else if(loc1 == 1) {
						rank1.get(i)[0] ++;
						rank1.get(i)[1] ++;
						rank1.get(i)[2] ++;
						rank1.get(i)[3] ++;
					} else if(loc1 <= 3) {
						rank1.get(i)[1] ++;
						rank1.get(i)[2] ++;
						rank1.get(i)[3] ++;
					} else if(loc1 <= 5) {
						rank1.get(i)[2] ++;
						rank1.get(i)[3] ++;
					} else if(loc1 <= 10) {
						rank1.get(i)[3] ++;
					}
					
					if(loc2 == -1) {
					} else if(loc2 == 1) {
						rank2.get(i)[0] ++;
						rank2.get(i)[1] ++;
						rank2.get(i)[2] ++;
						rank2.get(i)[3] ++;
					} else if(loc2 <= 3) {
						rank2.get(i)[1] ++;
						rank2.get(i)[2] ++;
						rank2.get(i)[3] ++;
					} else if(loc2 <= 5) {
						rank2.get(i)[2] ++;
						rank2.get(i)[3] ++;
					} else if(loc2 <= 10) {
						rank2.get(i)[3] ++;
					}
					
					/*** record improvement compare original *****/
					if(loc1 < comparibleLoc1) {
						improve[0][i] ++;
					} else if(loc1 > comparibleLoc1) {
						drop[0][i] ++;
					}
					
					if(loc2 == -1 && comparibleLoc1 != -1) {
						drop[1][i] ++; 
					} else if(loc2 != -1 && comparibleLoc1 == -1) {
						improve[1][i] ++;
					} else if(loc2 < comparibleLoc1) {
						improve[1][i] ++;
					} else if(loc2 > comparibleLoc1) {
						drop[1][i] ++;
					}
					
					/*** end of : record improvement compare original *****/
					
				}
				start1 ++;
				start2 ++;
			} else if(id1 > id2) {
				start2 ++;
			} else {
				start1 ++;
			}
		}
		String[] keys = new String[]{"top1","top3","top5","top10"};
		String[] perKeys = new String[]{"%top1","%top3","%top5","%top10"};
		StringBuffer percent = new StringBuffer();
		for(int i = 0; i < 4; i++) {
			stringBuffer.append("\n" + keys[i]);
			percent.append("\n" + perKeys[i]);
			for(int j = 1; j <= len; j++) {
				int[] count1 = rank1.get(j);
				int[] count2 = rank2.get(j);
				stringBuffer.append("\t");
				percent.append("\t");
				stringBuffer.append(count1[i] + "(" + count2[i] + ")");
				percent.append(String.format("%.3f", count1[i] / joinNumber) + "("
						+ String.format("%.3f", count2[i] / joinNumber) + ")");
			}
		}
		stringBuffer.append(percent);
		
		stringBuffer.append("\nimprove");
		for(int i = 1; i <= len; i++) {
			stringBuffer.append("\t" + improve[0][i] + "(" + improve[1][i] + ")");
		}
		stringBuffer.append("\ndrop");
		for(int i = 1; i <= len; i++) {
			stringBuffer.append("\t" + drop[0][i] + "(" + drop[1][i] + ")");
		}
		
//		
//		StringBuffer stringBuffer2 = new StringBuffer("(top1_3_5_10)");
//		StringBuffer percent2 = new StringBuffer("(percent)");
//		for(int i = 1; i <= len; i ++) {
//			int[] count = rank2.get(i);
//			stringBuffer2.append(count[0]);
//			percent2.append(count[0] / joinNumber);
//			for(int j = 1; j < count.length; j++) {
//				stringBuffer2.append("_");
//				stringBuffer2.append(count[j]);
//				percent2.append("_");
//				percent2.append(String.format("%.3f", count[j] / joinNumber));
//			}
//		}
//		
//		
//		stringBuffer.append("\n");
//		stringBuffer.append(stringBuffer2);
//		stringBuffer.append("\n");
//		stringBuffer.append(percent);
//		stringBuffer.append("\n");
//		stringBuffer.append(percent2);
		
		JavaFile.writeStringToFile(tarFile, stringBuffer.toString());
	}
	
	public static void main(String[] args) {
		String pred_base = System.getProperty("user.dir") + "/trans/linear_sd-0.5/line";
		String stats_base = System.getProperty("user.dir") + "/trans/linear_sd-0.5/line";
		String join_base = System.getProperty("user.dir") + "/trans/join/line";
		
//		String projName = "lang";
		String[] projs = new String[] {"lang","chart","time","math", "closure"};
		for(String projName : projs) {
			String predFile = pred_base + "/" + projName + "/" + projName + "_statistic.csv";
			String statsFile = stats_base + "/" + projName + "/" + projName + "_statistic.csv";
			String tarFile = join_base + "/" + projName + "/" + projName + "_join.csv";
			join(predFile, statsFile, tarFile, 3);
			System.out.println("Finish!");
		}
	}
	
}
