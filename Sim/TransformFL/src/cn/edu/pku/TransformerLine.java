/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cn.edu.pku;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.edu.pku.TransformerMethod.Data;
import cn.edu.pku.util.JavaFile;
import cn.edu.pku.util.Utils;

/**
 * Transform the fault localization result at line level 
 * @author Jiajun
 * @date Oct 9, 2017
 */
public class TransformerLine {


	// line original_score max_predicate_score total predicate
	private String[] single = null;
	private String base = null;
	
	private Map<String, Integer> projects;

	private void init_predict() {
		base = System.getProperty("user.dir") + "/info/pred_data";
		single = new String[]{ "Barinel_coverage.csv", "DStar_coverage.csv", "Ochiai_coverage.csv", "Op2_coverage.csv",
		"Tarantula_coverage.csv"};
	}
	
	private void init_linearsd() {
		base = System.getProperty("user.dir") + "/info/linear_sd";
		single = new String[]{ "Barinel_coverage_sd.csv", "DStar_coverage_sd.csv", "Ochiai_coverage_sd.csv", "Op2_coverage_sd.csv",
		"Tarantula_coverage_sd.csv"};
	}
	
	private void init_statistic() {
		base = System.getProperty("user.dir") + "/info/sd_data";
		single = new String[]{ "Barinel_coverage_sd.csv", "DStar_coverage_sd.csv", "Ochiai_coverage_sd.csv", "Op2_coverage_sd.csv",
		"Tarantula_coverage_sd.csv"};
	}
	
	public static enum KIND{
		PREDICT,
		STATISTIC,
		LINEAR_SD,
	}
	
	public TransformerLine(KIND kind) {
		projects = new HashMap<>();
		projects.put("chart", 26);
		 projects.put("time", 27);
		 projects.put("lang", 65);
		 projects.put("math", 106);
		 projects.put("closure", 133);
//		 projects.put("mockito", 38);
		 switch(kind) {
		 case PREDICT:
			 init_predict();
			 break;
		 case STATISTIC:
			 init_statistic();
			 break;
		 case LINEAR_SD:
			 init_linearsd();
			 break;
		 default:
			 System.err.println("Unkown category! :" + kind);
		 }
	}

	public void transform(String tarPath) {
		transform(tarPath, single);
	}
	
	class Data {
		int length = single.length;
		double[] values = new double[length * 4];
		Map<String, Integer> map = new HashMap<>();
		public Data() {
			for(int i = 0; i < single.length; i ++){
				map.put(single[i], Integer.valueOf(i));
			}
		}
		public void setOriRank(String alg, double value) {
			values[map.get(alg) * 4] = value;
		}
		
		public void setPredRank(String alg, double value) {
			values[map.get(alg) * 4 + 1] = value;
		}
		
		public void setPredWithReplaceRank(String alg, double value) {
			values[map.get(alg) * 4 + 2] = value;
		}
		
		public void setTotalRank(String alg, double value){
			values[map.get(alg) * 4 + 3] = value;
		}
		@Override
		public String toString() {
			StringBuffer sBuffer = new StringBuffer();
			sBuffer.append(values[0]);
			for(int i = 1; i < values.length; i++){
				sBuffer.append("\t" + values[i]);
			}
			return sBuffer.toString();
		}
	}
	
	private void statistic(String srcPath) {
		for(Entry<String, Integer> entry : Utils.PROJECTS.entrySet()) {
			int bound = entry.getValue();
			Map<String, int[]> top1Map = new HashMap<>();
			Map<String, int[]> top3Map = new HashMap<>();
			Map<String, int[]> top5Map = new HashMap<>();
			Map<String, int[]> top10Map = new HashMap<>();
			Map<String, int[]> improve = new HashMap<>();
			Map<String, int[]> drop = new HashMap<>();
			int lenForAlgo = 4;
			for(int i = 0; i < single.length; i++){
				top1Map.put(single[i], new int[lenForAlgo]);
				top3Map.put(single[i], new int[lenForAlgo]);
				top5Map.put(single[i], new int[lenForAlgo]);
				top10Map.put(single[i], new int[lenForAlgo]);
				improve.put(single[i], new int[lenForAlgo]);
				drop.put(single[i], new int[lenForAlgo]);
			}
			
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("id");
			for(int i = 0; i < single.length; i++){
				String alg = single[i].substring(0, single[i].indexOf("_"));
				stringBuffer.append("\t");
				stringBuffer.append("ori_" + alg);
				stringBuffer.append("\t");
				stringBuffer.append("pred_" + alg);
				stringBuffer.append("\t");
				stringBuffer.append("predWithReplace_" + alg);
				stringBuffer.append("\t");
				stringBuffer.append("tot_" + alg);
			}
			stringBuffer.append("\n");
			double evaluateNum = 0;
			for(int id = 1; id <= bound; id++){
				Data data = new Data();
				boolean exist = true;
				for(int cycle = 0; cycle < single.length; cycle ++) {
					String algName = single[cycle];
					String srcFile = srcPath + "/" + entry.getKey() + "/" + entry.getKey() + "_" + id + "/" + algName;
					File file = new File(srcFile);
					if(!file.exists()){
						exist = false;
						break;
					}
					List<String> contents = JavaFile.readFileToStringList(srcFile);
					List<Double> originalList = new ArrayList<>();
					List<Double> predList = new ArrayList<>();
					List<Double> predWithReplacList = new ArrayList<>();
					List<Double> totalList = new ArrayList<>();
					double originalValue = -1000000;
					double predValue = -1000000;
					double predWithReplaceValue = -1000000;
					double totalValue = -1000000;
					for(int i = 1; i < contents.size(); i++) {
						String[] elements = contents.get(i).split("\t");
						if(elements.length != 6){
							System.err.println("Format error : " + contents.get(i));
							System.exit(1);
						}
						Double ori = Double.valueOf(elements[1]);
						if(Double.isInfinite(ori.doubleValue())){
							ori = 1000000000.0;
						}
						Double pred = Double.valueOf(elements[2]);
						if(Double.isInfinite(pred.doubleValue())){
							pred = 1000000000.0;
						}
						
						Double predOri = pred;
						if(elements[4].length() == 0) {
							predOri = ori;
						}
						
						Double tot = Double.valueOf(elements[3]);
						if(Double.isInfinite(tot.doubleValue())){
							tot = 1000000000.0;
						}
						
						originalList.add(ori);
						predList.add(pred);
						predWithReplacList.add(predOri);
						totalList.add(tot);
						if(elements[5].equals("GT")){
							originalValue = originalValue >= ori ? originalValue : ori;
							predValue = predValue >= pred ? predValue : pred;
							predWithReplaceValue = predWithReplaceValue >= predOri ? predWithReplaceValue : predOri;
							totalValue = totalValue >= tot ? totalValue : tot;
						}
					}
					Collections.sort(originalList, Utils.DESCENDING);
					Collections.sort(predList, Utils.DESCENDING);
					Collections.sort(predWithReplacList, Utils.DESCENDING);
					Collections.sort(totalList, Utils.DESCENDING);
					
					int oriRank = Utils.computeRank(originalList, originalValue);
					int predRank = Utils.computeRank(predList, predValue);
					int predWithReplaceRank = Utils.computeRank(predWithReplacList, predWithReplaceValue);
					int totRank = Utils.computeRank(totalList, totalValue);
					
					data.setOriRank(algName, oriRank);
					data.setPredRank(algName, predRank);
					data.setPredWithReplaceRank(algName, predWithReplaceRank);
					data.setTotalRank(algName, totRank);
					
					Utils.setRankMap(new int[]{oriRank, predRank, predWithReplaceRank, totRank}, algName, top1Map, top3Map, top5Map, top10Map);
					
					if(predRank < oriRank)	improve.get(algName)[1] ++;
					if(predRank > oriRank)  drop.get(algName)[1] ++;
					
					if(predWithReplaceRank < oriRank) improve.get(algName)[2] ++;
					if(predWithReplaceRank > oriRank) drop.get(algName)[2] ++;

					if(totRank < oriRank) improve.get(algName)[3] ++;
					if(totRank > oriRank) drop.get(algName)[3] ++;
					
				}
				
				if(exist) {
					evaluateNum ++;
					stringBuffer.append(id);
					stringBuffer.append("\t");
					stringBuffer.append(data.toString());
					stringBuffer.append("\n");
				}
				
			}
			
			StringBuilder stringBuilder = Utils.assemble(single, top1Map, top3Map, top5Map, top10Map, 4, evaluateNum);
			stringBuffer.append(stringBuilder);
			stringBuffer.append("\nimprove_drop");
			for(int i = 0; i < single.length; i++) {
				String alg = single[i];
				int[] imp = improve.get(alg);
				int[] drp = drop.get(alg);
				stringBuffer.append(String.format("\t%s_%s\t%s_%s\t%s_%s\t%s_%s", imp[0], drp[0], imp[1], drp[1], imp[2], drp[2], imp[3], drp[3]));
			}
			
			JavaFile.writeStringToFile(srcPath + "/" + entry.getKey() + "/" + entry.getKey() + "_statistic.csv", stringBuffer.toString(), false);
		}
	}
//	private void statistic(String srcPath) {
//		for(Entry<String, Integer> entry : projects.entrySet()) {
//			int bound = entry.getValue();
//			Map<String, int[]> top1Map = new HashMap<>();
//			Map<String, int[]> top3Map = new HashMap<>();
//			Map<String, int[]> top5Map = new HashMap<>();
//			Map<String, int[]> top10Map = new HashMap<>();
//			for(int i = 0; i < single.length; i++){
//				top1Map.put(single[i], new int[2]);
//				top3Map.put(single[i], new int[2]);
//				top5Map.put(single[i], new int[2]);
//				top10Map.put(single[i], new int[2]);
//			}
//			
//			StringBuffer stringBuffer = new StringBuffer();
//			stringBuffer.append("id");
//			for(int i = 0; i < single.length; i++){
//				String alg = single[i].substring(0, single[i].indexOf("_"));
//				stringBuffer.append("\t");
//				stringBuffer.append("ori_" + alg);
//				stringBuffer.append("\t");
//				stringBuffer.append("pred_" + alg);
//				stringBuffer.append("\t");
//				stringBuffer.append("predWithReplace_" + alg);
//				stringBuffer.append("\t");
//				stringBuffer.append("tot_" + alg);
//			}
//			stringBuffer.append("\n");
//			double evaluateNum = 0;
//			for(int id = 1; id <= bound; id++){
//				Data data = new Data();
//				boolean exist = true;
//				for(int cycle = 0; cycle < single.length; cycle ++) {
//					String algName = single[cycle];
//					String srcFile = srcPath + "/" + entry.getKey() + "/" + entry.getKey() + "_" + id + "/" + algName;
//					File file = new File(srcFile);
//					if(!file.exists()){
//						exist = false;
//						break;
//					}
//					List<String> contents = JavaFile.readFileToStringList(srcFile);
//					List<Double> originalList = new ArrayList<>();
//					List<Double> totalList = new ArrayList<>();
//					double originalValue = -1000000;
//					double totalValue = -1000000;
//					for(int i = 1; i < contents.size(); i++) {
//						String[] elements = contents.get(i).split("\t");
//						if(elements.length != 6){
//							System.err.println("Format error : " + contents.get(i));
//							System.exit(1);
//						}
//						Double ori = Double.valueOf(elements[1]);
//						if(Double.isInfinite(ori.doubleValue())){
//							ori = 1000000000.0;
//						}
//						Double tot = Double.valueOf(elements[3]);
//						if(Double.isInfinite(tot.doubleValue())){
//							tot = 1000000000.0;
//						}
//						originalList.add(ori);
//						totalList.add(tot);
//						if(elements[5].equals("GT")){
//							originalValue = originalValue >= ori ? originalValue : ori;
//							totalValue = totalValue >= tot ? totalValue : tot;
//						}
//					}
//					
//					Collections.sort(originalList, Utils.DESCENDING);
//					Collections.sort(totalList, Utils.DESCENDING);
//					
//					int oriRank = Utils.computeRank(originalList, originalValue);
//					int predRank = Utils.computeRank(totalList, totalValue);
//					
//					data.setOriValues(algName, oriRank);
//					data.setPredValue(algName, predRank);
//					
//					if(oriRank == -1){
//					} else if(oriRank == 1){
//						top1Map.get(algName)[0] ++;
//					} else if(oriRank <= 3){
//						top3Map.get(algName)[0] ++;
//					} else if(oriRank <= 5){
//						top5Map.get(algName)[0] ++;
//					} else if(oriRank <= 10){
//						top10Map.get(algName)[0] ++;
//					}
//					
//					if(predRank == -1){
//					} else if(predRank == 1){
//						top1Map.get(algName)[1] ++;
//					} else if(predRank <= 3){
//						top3Map.get(algName)[1] ++;
//					} else if(predRank <= 5){
//						top5Map.get(algName)[1] ++;
//					} else if(predRank <= 10){
//						top10Map.get(algName)[1] ++;
//					}
//					
//				}
//				
//				if(exist) {
//					evaluateNum ++;
//					stringBuffer.append(id);
//					stringBuffer.append("\t");
//					stringBuffer.append(data.toString());
//					stringBuffer.append("\n");
//				}
//				
//			}
//			
//			stringBuffer.append("top_1_3_5_10");
//			StringBuffer percent = new StringBuffer("percent");
//			for(int i = 0; i < single.length; i++){
//				stringBuffer.append("\t");
//				percent.append("\t");
//				
//				int count = top1Map.get(single[i])[0];
//				stringBuffer.append(count);
//				stringBuffer.append("_");
//				
//				percent.append(String.format("%.3f", count/evaluateNum));
//				percent.append("_");
//				
//				count += top3Map.get(single[i])[0];
//				stringBuffer.append(count);
//				stringBuffer.append("_");
//				
//				percent.append(String.format("%.3f", count/evaluateNum));
//				percent.append("_");
//				
//				count += top5Map.get(single[i])[0];
//				stringBuffer.append(count);
//				stringBuffer.append("_");
//				
//				percent.append(String.format("%.3f", count/evaluateNum));
//				percent.append("_");
//				
//				count += top10Map.get(single[i])[0];
//				stringBuffer.append(count);
//				
//				percent.append(String.format("%.3f", count/evaluateNum));
//				
//				
//				stringBuffer.append("\t");
//				percent.append("\t");
//				
//				count = top1Map.get(single[i])[1];
//				stringBuffer.append(count);
//				stringBuffer.append("_");
//				
//				percent.append(String.format("%.3f", count/evaluateNum));
//				percent.append("_");
//				
//				count += top3Map.get(single[i])[1];
//				stringBuffer.append(count);
//				stringBuffer.append("_");
//				
//				percent.append(String.format("%.3f", count/evaluateNum));
//				percent.append("_");
//				
//				count += top5Map.get(single[i])[1];
//				stringBuffer.append(count);
//				stringBuffer.append("_");
//				
//				percent.append(String.format("%.3f", count/evaluateNum));
//				percent.append("_");
//				
//				count += top10Map.get(single[i])[1];
//				stringBuffer.append(count);
//				
//				percent.append(String.format("%.3f", count/evaluateNum));
//			}
//			stringBuffer.append("\n");
//			stringBuffer.append(percent);
//			
//			JavaFile.writeStringToFile(srcPath + "/" + entry.getKey() + "/" + entry.getKey() + "_statistic.csv", stringBuffer.toString(), false);
//		}
//	}
	
	/**
	 * transform fault localization result from line level to method level and write to file
	 * also, a newly added column records the correct location with (GT) or incorrect one with (_)
	 * @param tarPath
	 * @param fileNames
	 */
	private void transform(String tarPath, String[] fileNames) {
		for (Entry<String, Integer> entry : projects.entrySet()) {
			for (int id = 1; id <= entry.getValue(); id++) {
				String path = base + "/" + entry.getKey() + "/" + entry.getKey() + "_" + id;
				String tarFile = tarPath + "/" + entry.getKey() + "/" + entry.getKey() + "_" + id;
				Set<String> groungTruth = getGroundTruth(entry.getKey(), id);
				for (String name : fileNames) {
					File file = new File(path + "/" + name);
					if(!file.exists()){
						continue;
					}
					List<String> contents = JavaFile.readFileToStringList(path + "/" + name);
					StringBuffer stringBuffer = null;
					if(contents != null && contents.size() > 0){
						stringBuffer = new StringBuffer(contents.get(0) + "\tlabel");
						stringBuffer.append("\n");
						for (int i = 1; i < contents.size(); i++) {
							String[] data = contents.get(i).split("\t");
							String[] lineInfo = data[0].split("#");
							String line = lineInfo[0] + "." + lineInfo[2] + ":" + lineInfo[4];
							stringBuffer.append(contents.get(i));
							if(groungTruth.contains(line)){
								stringBuffer.append("\tGT\n");
							} else {
								stringBuffer.append("\t_\n");
							}
						}
					}
					JavaFile.writeStringToFile(tarFile + "/" + name, stringBuffer.toString(), false);
				}
			}
		}
	}
	
	/**
	 * get ground truth from formatted data
	 * @param projName
	 * @param id
	 * @return
	 */
	private Set<String> getGroundTruth(String projName, int id) {
		Set<String> allBuggyLines = new HashSet<>();
		String path = System.getProperty("user.dir") + "/ActualFaultStatement/" + projName + "/" + id; 
		List<String> contents = JavaFile.readFileToStringList(path);
		for(String string : contents){
			String[] lines = string.split("\\|\\|");
			for(String line : lines) {
				line = line.trim();
				if(line.length() > 0) {
					allBuggyLines.add(line);
				}
			}
		}
		return allBuggyLines;
	}

	public static void main(String[] args) {
		// uncomment these two lines of code if compute predict result
//		String tarPath = System.getProperty("user.dir") + "/trans/predict/line";
//		TransformerLine transformer = new TransformerLine(KIND.PREDICT);
		
		// uncomment these two lines of code if compute statistic debugging result
//		String tarPath = System.getProperty("user.dir") + "/trans/statistic/line";
//		TransformerLine transformer = new TransformerLine(KIND.STATISTIC);
		
		// uncomment these two lines of code if compute statistic debugging result
		String weight = "0.8";
		String tarPath = System.getProperty("user.dir") + "/trans/linear_sd-" + weight + "/line";
		TransformerLine transformer = new TransformerLine(KIND.LINEAR_SD);
				
		transformer.transform(tarPath);
		transformer.statistic(tarPath);
		
		System.out.println("Finish!");
	}
	
}
