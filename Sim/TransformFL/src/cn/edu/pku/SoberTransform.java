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

import cn.edu.pku.util.JavaFile;
import cn.edu.pku.util.Utils;

/**
 * Transform the result of "SOBER (Chao Liu)" at method level 
 * @author Jiajun
 * @date Oct 2, 2017
 */
public class SoberTransform {

	// line original_score max_predicate_score total predicate
	private String[] single = new String[]{ "Sober_coverage.csv"};

	private String base = System.getProperty("user.dir") + "/info/sober";

	public SoberTransform() {
	}

	public void transform(String tarPath) {
		transform(tarPath, single);
	}
	
	class Data {
		int length = single.length;
		double[] values = new double[length * 2];
		Map<String, Integer> map = new HashMap<>();
		public Data() {
			for(int i = 0; i < single.length; i ++){
				map.put(single[i], Integer.valueOf(i));
			}
		}
		public void setOriRank(String alg, double value) {
			values[map.get(alg) * 2] = value;
		}
		public void setTotalRank(String alg, double value){
			values[map.get(alg) * 2 + 1] = value;
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
			for(int i = 0; i < single.length; i++){
				top1Map.put(single[i], new int[2]);
				top3Map.put(single[i], new int[2]);
				top5Map.put(single[i], new int[2]);
				top10Map.put(single[i], new int[2]);
			}
			
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("id");
			for(int i = 0; i < single.length; i++){
				stringBuffer.append("\t");
				stringBuffer.append("ori_" + single[i].substring(0, single[i].indexOf("_")));
				stringBuffer.append("\t");
				stringBuffer.append("tot_" + single[i].substring(0, single[i].indexOf("_")));
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
					List<Double> totalList = new ArrayList<>();
					double originalValue = -1000000;
					double totalValue = -1000000;
					for(int i = 1; i < contents.size(); i++) {
						String[] elements = contents.get(i).split("\t");
						if(elements.length != 4){
							System.err.println("Format error : " + contents.get(i));
							System.exit(1);
						}
						// default value for original
						Double ori = Double.valueOf(0);
						Double tot = Double.valueOf(elements[1]);
						if(Double.isInfinite(tot.doubleValue())){
							tot = 1000000000.0;
						}
						originalList.add(ori);
						totalList.add(tot);
						if(elements[3].equals("GT")){
							originalValue = originalValue >= ori ? originalValue : ori;
							totalValue = totalValue >= tot ? totalValue : tot;
						}
					}
					
					Collections.sort(originalList, Utils.DESCENDING);
					Collections.sort(totalList, Utils.DESCENDING);

					int oriRank = Utils.computeRank(originalList, originalValue);
					int predRank = Utils.computeRank(totalList, totalValue);
					
					data.setOriRank(algName, oriRank);
					data.setTotalRank(algName, predRank);
					Utils.setRankMap(new int[]{oriRank, predRank}, algName, top1Map, top3Map, top5Map, top10Map);
				}
				
				if(exist) {
					evaluateNum ++;
					stringBuffer.append(id);
					stringBuffer.append("\t");
					stringBuffer.append(data.toString());
					stringBuffer.append("\n");
				}
				
			}
			
			stringBuffer.append(Utils.assemble(single, top1Map, top3Map, top5Map, top10Map, 2, evaluateNum));
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
					if(!file.  exists()){
						continue;
					}
					List<String> contents = JavaFile.readFileToStringList(path + "/" + name);
					StringBuffer stringBuffer = null;
					Set<String> methods = new HashSet<>();
					if(contents != null && contents.size() > 0){
						stringBuffer = new StringBuffer(contents.get(0) + "\tlabel");
						stringBuffer.append("\n");
						for (int i = 1; i < contents.size(); i++) {
							String[] data = contents.get(i).split("\t");
							String method = data[0].substring(0, data[0].lastIndexOf("#"));
							if(!methods.contains(method)){
								methods.add(method);
								stringBuffer.append(contents.get(i));
								if(groungTruth.contains(method)){
									stringBuffer.append("\tGT\n");
								} else {
									stringBuffer.append("\t_\n");
								}
							}
						}
					}
					JavaFile.writeStringToFile(tarFile + "/" + name, stringBuffer.toString(), false);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		String tarPath = System.getProperty("user.dir") + "/trans/sober/method";
		SoberTransform soberTransform = new SoberTransform();
		
		soberTransform.transform(tarPath);
		soberTransform.statistic(tarPath);
		
		System.out.println("Finish!");
		
	}

}
