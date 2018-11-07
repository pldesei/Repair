package cn.edu.pku.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Utils {

	/**
	 * all project information
	 */
	public final static Map<String, Integer> PROJECTS;
	
	static {
		PROJECTS = new HashMap<>();
		PROJECTS.put("chart", 26);
		PROJECTS.put("time", 27);
		PROJECTS.put("lang", 65);
		PROJECTS.put("math", 106);
		PROJECTS.put("closure", 133);
//		PROJECTS.put("mockito", 38);
	}
	
	/**
	 * sort list with descending order
	 */
	public static Comparator<Double> DESCENDING = new Comparator<Double>() {
		@Override
		public int compare(Double o1, Double o2) {
			return o2.compareTo(o1);
		}
	};
	
	public static void main(String[] args) {
		List<Double> list = new ArrayList<>(Arrays.asList(0.3, 4.0, 0.2, -0.5, Double.NaN, Double.POSITIVE_INFINITY));
		Collections.sort(list, Utils.DESCENDING);
		System.out.println(list);
	}
	
	
	/**
	 * compute the top-1-3-5-10 for each algorithm and compute corresponding percentage for each ranking
	 * 
	 * @param algorithms : all related algorithms
	 * @param top1 : record the number of top-1 correct one for each algorithm
	 * @param top3 : record the number of top-3 correct one for each algorithm
	 * @param top5 : record the number of top-5 correct one for each algorithm
	 * @param top10 : record the number of top-10 correct one for each algorithm
	 * @param dimension : dimension for the value of maps : top1, top3, top5 and top10.
	 * @param evaluateNumber
	 * @return
	 */
	public static StringBuilder assemble(String[] algorithms, Map<String, int[]> top1, Map<String, int[]> top3, Map<String, int[]> top5, Map<String, int[]> top10, int dimension, double evaluateNumber) {
		StringBuilder statistic = new StringBuilder();
		statistic.append("top_1_3_5_10");
		StringBuilder percentage = new StringBuilder();
		percentage.append("percent");
		List<Map<String, int[]>> maps = new ArrayList<>(Arrays.asList(top1, top3, top5, top10));
		for(int i = 0; i < algorithms.length; i ++) {
			int count = 0;
			String algo = algorithms[i];
			for(int dim = 0; dim < dimension; dim ++) {
				statistic.append("\t");
				percentage.append("\t");
				count = maps.get(0).get(algo)[dim];
				statistic.append(count);
				percentage.append(String.format("%.3f", count/evaluateNumber));
				for(int mapCnt = 1; mapCnt < maps.size(); mapCnt ++) {
					statistic.append('_');
					percentage.append('_');
					count += maps.get(mapCnt).get(algo)[dim];
					statistic.append(count);
					percentage.append(String.format("%.3f", count/evaluateNumber));
				}
			}
		}
		statistic.append('\n');
		statistic.append(percentage);
		return statistic;
	}
	
	/**
	 * compute ranking with considering save score
	 * 
	 * @param dataList : contains all scores with descending order
	 * @param desired : desired score
	 * @return rank for the desired score in the given list
	 */
	public static int computeRank(List<Double> dataList, double desired) {
		int start = -1;
		int end = -1;
		for(int i = 0; i < dataList.size(); i++){
			if(Math.abs(dataList.get(i) - desired) < 1e-7){
				if(start == -1){
					start = i + 1;
					end = i + 1;
				} else {
					end = i + 1;
				}
			} else if(end != -1){
				break;
			}
		}
		int rank = (int) (start == end ? start : Math.ceil((end + start) / 2.0));
		return rank;
	}
	
	/**
	 * set corresponding ranking information for each 'rank' in {@code ranks}
	 * @param ranks : all ranks to be observed, NOTE, the order is important
	 * @param algName : algorithm name
	 * @param top1Map : record top1 for each algorithm
	 * @param top3Map : record top3
	 * @param top5Map : record top5
	 * @param top10Map : record top10
	 */
	public static void setRankMap(int[] ranks, String algName, Map<String, int[]> top1Map,  Map<String, int[]> top3Map,  Map<String, int[]> top5Map,  Map<String, int[]> top10Map) {
		for(int index = 0; index < ranks.length; index++) {
			int rank = ranks[index];
			if(rank == -1){
			} else if(rank == 1){
				top1Map.get(algName)[index] ++;
			} else if(rank <= 3){
				top3Map.get(algName)[index] ++;
			} else if(rank <= 5){
				top5Map.get(algName)[index] ++;
			} else if(rank <= 10){
				top10Map.get(algName)[index] ++;
			}
		}
	}
	
	/**
	 * get ground truth from formatted data
	 * @param projName
	 * @param id
	 * @return
	 */
	public static Set<String> getGroundTruth(String projName, int id) {
		Set<String> methods = new HashSet<>();
		String path = System.getProperty("user.dir") + "/groundTruth/" + projName + "/" + id + ".txt"; 
		List<String> contents = JavaFile.readFileToStringList(path);
		methods.addAll(contents);
		return methods;
	}
	
	/**
	 * format method fault localization data (ground truth) based on those for David Lo's
	 */
	public static void transformGroundTruth(Map<String, Integer> projects) {
		for(Entry<String, Integer> entry : projects.entrySet()) {
			for(int id = 1; id <= entry.getValue(); id ++){
				String srcFile = System.getProperty("user.dir") + "/faulty_methods/" + entry.getKey() + "/" + id + ".txt";
				String tarFile = System.getProperty("user.dir") + "/groundTruth/" + entry.getKey() + "/" + id + ".txt";
				List<String> contents = JavaFile.readFileToStringList(srcFile);
				StringBuffer stringBuffer = new StringBuffer();
				for(String string : contents){
					if(string.endsWith("<clinit>")){
						stringBuffer.append(string);
						stringBuffer.append("\n");
						continue;
					}
					string = string.replaceAll("\\s+", " ");
					string = string.replace(", ", ",");
//					int firstSpaceIndex = string.indexOf(" ");
					int left = string.indexOf("(");
					int firstSpaceIndex = string.lastIndexOf(" ", left - 1);
					String retType;
					String detail;
					if(firstSpaceIndex < 0 || firstSpaceIndex > left){
						retType = "?";
						detail = string;
					} else {
						retType = string.substring(0, firstSpaceIndex);
						detail = string.substring(firstSpaceIndex + 1);
					}
					
					left = detail.indexOf("(");
					int right = detail.indexOf(")");
					if(left == -1){
						System.err.println("left : " + string);
					}
					if(right == -1){
						System.err.println("right : " + string);
					}
					String params;
					params = "?";
					if(left + 1 < right){
						params += "," + detail.substring(left + 1, right);
					}
					
					String pathAndMethod = detail.substring(0, left);
					int index = pathAndMethod.lastIndexOf(".");
					String path = pathAndMethod.substring(0, index);
					String name = pathAndMethod.substring(index + 1);
					
					// full path
					stringBuffer.append(path);
					if(Character.isUpperCase(name.charAt(0))){
						stringBuffer.append("." + name);
					}
					// return type
					stringBuffer.append("#" + retType);
					// name
					stringBuffer.append("#" + name);
					// parameters
					stringBuffer.append("#" + params);
					//
					stringBuffer.append("\n");
				}
				JavaFile.writeStringToFile(tarFile, stringBuffer.toString(), false);
			}
		}
	}
}
