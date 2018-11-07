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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.map.CaseInsensitiveMap;

import java.util.Set;

import cn.edu.pku.util.JavaFile;
import cn.edu.pku.util.Utils;

/**
 * Transform the fault localization result at method level
 * @author Jiajun
 * @date Oct 2, 2017
 */
public class TransformerMethod {

	// line original_score max_predicate_score total predicate
	private String[] single = null;

	private String base = null;
	
	public static enum KIND{
		PREDICT,
		PREDICT_WITH_SCORE,
		PREDICT_REPLACE_COND,
		BRANCH_COVER,
		LINEAR_SD,
		IN_OUT,
		STATISTIC,
		COMBINE,
		PREDICT_PLUS_STATISTICDEBUGGING,
		PREDICT_PLUS_BARINEL
	}

	private void init_branch() {
		base = System.getProperty("user.dir") + "/info/branch";
		single = new String[]{ "Barinel_coverage.csv", "DStar_coverage.csv", "Ochiai_coverage.csv", "Op2_coverage.csv",
		"Tarantula_coverage.csv"};
	}
	
	private void init_predict() {
		base = System.getProperty("user.dir") + "/info/pred_data";
		single = new String[]{ "Barinel_coverage.csv", "DStar_coverage.csv", "Ochiai_coverage.csv", "Op2_coverage.csv",
		"Tarantula_coverage.csv"};
	}
	
	private void init_predict_replace_COND() {
		base = System.getProperty("user.dir") + "/info/update_data";
		single = new String[]{ "Barinel_coverage_upd.csv", "DStar_coverage_upd.csv", "Ochiai_coverage_upd.csv", "Op2_coverage_upd.csv",
		"Tarantula_coverage_upd.csv"};
	}
	
	private void init_predict_with_score() {
		base = System.getProperty("user.dir") + "/info/score_pred";
		single = new String[]{ "Barinel_coverage.csv", "DStar_coverage.csv", "Ochiai_coverage.csv", "Op2_coverage.csv",
		"Tarantula_coverage.csv"};
	}
	
	private void init_in_out() {
		base = System.getProperty("user.dir") + "/info/in_out";
		single = new String[]{ "Barinel_coverage_sd.csv", "DStar_coverage_sd.csv", "Ochiai_coverage_sd.csv", "Op2_coverage_sd.csv",
		"Tarantula_coverage_sd.csv"};
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
	
	private void init_combine() {
		base = System.getProperty("user.dir") + "/info/combine";
		single = new String[]{ "Barinel_coverage_combine.csv", "DStar_coverage_combine.csv", "Ochiai_coverage_combine.csv", "Op2_coverage_combine.csv",
		"Tarantula_coverage_combine.csv"};
	}
	
	private void init_statisticdebugging(String name) {
		base = System.getProperty("user.dir") + "/info/statisticdebugging";
		single = new String[]{ name };
	}
	
	public TransformerMethod(KIND kind) {
		 switch(kind) {
		 case PREDICT:
			 init_predict();
			 break;
		 case PREDICT_REPLACE_COND:
			 init_predict_replace_COND();
			 break;
		 case PREDICT_WITH_SCORE:
			 init_predict_with_score();
			 break;
		 case BRANCH_COVER:
			 init_branch();
			 break;
		 case STATISTIC:
			 init_statistic();
			 break;
		 case IN_OUT:
			 init_in_out();
			 break;
		 case LINEAR_SD:
			 init_linearsd();
			 break;
		 case COMBINE:
			 init_combine();
			 break;
		 case PREDICT_PLUS_STATISTICDEBUGGING:
			 init_statisticdebugging("StatisticalDebugging_coverage.csv");
			 break;
		 case PREDICT_PLUS_BARINEL:
			 init_statisticdebugging("Barinel_coverage.csv");
			 break;
		 default:
			 System.err.println("Wrong kind!");
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
	
	/**
	 * transform fault localization result from line level to method level and write to file
	 * also, a newly added column records the correct location with (GT) or incorrect one with (_)
	 * @param tarPath
	 * @param fileNames
	 */
	private void transform(String tarPath, String[] fileNames) {
		for (Entry<String, Integer> entry : Utils.PROJECTS.entrySet()) {
			for (int id = 1; id <= entry.getValue(); id++) {
				String path = base + "/" + entry.getKey() + "/" + entry.getKey() + "_" + id;
				String tarFile = tarPath + "/" + entry.getKey() + "/" + entry.getKey() + "_" + id;
				Set<String> groungTruth = Utils.getGroundTruth(entry.getKey(), id);
				for (String name : fileNames) {
					File file = new File(path + "/" + name);
					if(!file.exists()){
						continue;
					}
					List<String> contents = JavaFile.readFileToStringList(path + "/" + name);
					StringBuffer stringBuffer = null;
					Map<String, double[]> map = new HashMap<>();
					Map<String, String> predicateMap = new HashMap<>();
					Map<String, String> method2Line = new HashMap<>();
					if(contents != null && contents.size() > 0){
						stringBuffer = new StringBuffer(contents.get(0) + "\tlabel");
						stringBuffer.append("\n");
						for (int i = 1; i < contents.size(); i++) {
							String[] data = contents.get(i).split("\t");
							String method = data[0].substring(0, data[0].lastIndexOf("#"));
							String line = data[0].substring(data[0].lastIndexOf("#") + 1);
							Double original = Double.valueOf(data[1]);
							Double pred = Double.valueOf(data[2]);
							Double total = Double.valueOf(data[3]);
							String predicate = "";
							if(data.length >= 5) {
								predicate = data[4];
							}
							double[] value = map.get(method);
							
							if(value == null) {
								value = new double[3];
								value[0] = original;
								value[1] = pred;
								value[2] = total;
								map.put(method, value);
								predicateMap.put(method, predicate);
								method2Line.put(method, line);
							} else {
								value[0] = value[0] >= original ? value[0] : original;
								value[1] = value[1] >= pred ? value[1] : pred;
								if(value[2] < total) {
									value[2] = total;
									predicateMap.put(method, predicate);
									method2Line.put(method, line);
								}
							}
						}
						
						List<Entry<String, double[]>> entries = new LinkedList<>(map.entrySet());
						Collections.sort(entries, new Comparator<Entry<String, double[]>>() {

							@Override
							public int compare(Entry<String, double[]> o1, Entry<String, double[]> o2) {
								return new Double(o2.getValue()[2]).compareTo(new Double(o1.getValue()[2]));
							}
						});
						
						
						for(Entry<String, double[]> entry2 : entries) {
							String method = entry2.getKey();
							double[] value = entry2.getValue();
							stringBuffer.append(method + "#" + method2Line.get(method) + "\t" + value[0] + "\t" + value[1] + "\t" + value[2] + "\t" + predicateMap.get(method));
							if(groungTruth.contains(entry2.getKey())) {
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
	
	public static void main(String[] args) {
		// uncomment these two lines of code if compute predict result
//		String tarPath = System.getProperty("user.dir") + "/trans/predict/method";
//		TransformerMethod transformer = new TransformerMethod(KIND.PREDICT);
		
		// uncomment these two lines of code if compute predict result with replace predicates for IF/FOR/WHILE/RETURN
//		String tarPath = System.getProperty("user.dir") + "/trans/update/method";
//		TransformerMethod transformer = new TransformerMethod(KIND.PREDICT_REPLACE_COND);
		
		// uncomment these two lines of code if compute predict result with considering predict score
//		String tarPath = System.getProperty("user.dir") + "/trans/score_pred/method";
//		TransformerMethod transformer = new TransformerMethod(KIND.PREDICT_WITH_SCORE);
		
		// uncomment these two lines of code if compute branch coverage
//		String tarPath = System.getProperty("user.dir") + "/trans/branch/method";
//		TransformerMethod transformer = new TransformerMethod(KIND.BRANCH_COVER);
		
		// uncomment these two lines of code if compute statistic debugging result
//		String tarPath = System.getProperty("user.dir") + "/trans/statistic/method";
//		TransformerMethod transformer = new TransformerMethod(KIND.STATISTIC);
		
		// uncomment these two lines of code if compute method level fault localization
		String tarPath = System.getProperty("user.dir") + "/trans/in_out/method";
		TransformerMethod transformer = new TransformerMethod(KIND.IN_OUT);
		
		// uncomment these two lines of code if compute linear combination between sd and sbfl
//		String tarPath = System.getProperty("user.dir") + "/trans/linear_sd/method";
//		TransformerMethod transformer = new TransformerMethod(KIND.LINEAR_SD);
		
		// uncomment these two lines of code if compute result for combination of predict and statistical debugging
//		String tarPath = System.getProperty("user.dir") + "/trans/combine/method";
//		TransformerMethod transformer = new TransformerMethod(KIND.COMBINE);
				
		/*****     using predict predicates and statistical debugging formula       ******/
//		String tarPath = System.getProperty("user.dir") + "/trans/statisticdebugging/method";
//		TransformerMethod transformer = new TransformerMethod(KIND.PREDICT_PLUS_STATISTICDEBUGGING);
		
//		String tarPath = System.getProperty("user.dir") + "/trans/statisticdebugging-barinel/method";
//		TransformerMethod transformer = new TransformerMethod(KIND.PREDICT_PLUS_BARINEL);
		/******   end    *****/
		
//		transformer.transformGroundTruth();
		transformer.transform(tarPath);
		transformer.statistic(tarPath);
		
		System.out.println("Finish!");
		
	}

}
