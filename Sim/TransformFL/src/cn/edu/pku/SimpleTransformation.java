package cn.edu.pku;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.edu.pku.util.JavaFile;
import cn.edu.pku.util.Utils;

/**
 * Transform the result of "Simple", which use the formula as "1 - pass(s)", at method level
 * @author Jiajun
 *
 */
public class SimpleTransformation {

		private String[] single = new String[]{ "Simple_coverage.csv"};
		private String base = System.getProperty("user.dir") + "/info/simple";

		public SimpleTransformation() {
		}

		public void transform(String tarPath) {
			transform(tarPath, single);
		}
		
		class Data {
			int length = single.length;
			double[] values = new double[length * 3];
			Map<String, Integer> map = new HashMap<>();
			public Data() {
				for(int i = 0; i < single.length; i ++){
					map.put(single[i], Integer.valueOf(i));
				}
			}
			public void setOriValues(String alg, double value) {
				values[map.get(alg) * 2] = value;
			}
			public void setSimpleValue(String alg, double value){
				values[map.get(alg) * 2 + 1] = value;
			}
			public void setTotalValue(String alg, double value){
				values[map.get(alg) * 2 + 2] = value;
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
					top1Map.put(single[i], new int[3]);
					top3Map.put(single[i], new int[3]);
					top5Map.put(single[i], new int[3]);
					top10Map.put(single[i], new int[3]);
				}
				
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append("id");
				for(int i = 0; i < single.length; i++){
					stringBuffer.append("\t");
					stringBuffer.append("ori_" + single[i].substring(0, single[i].indexOf("_")));
					stringBuffer.append("\t");
					stringBuffer.append(single[i].substring(0, single[i].indexOf("_")));
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
						List<Double> simpleList = new ArrayList<>();
						List<Double> totalList = new ArrayList<>();
						double originalValue = -1000000;
						double simpleValue = -1000000;
						double totalValue = -1000000;
						for(int i = 1; i < contents.size(); i++) {
							String[] elements = contents.get(i).split("\t");
							if(elements.length != 6){
								System.err.println("Format error : " + contents.get(i));
								System.exit(1);
							}
							// default value for original
							Double ori = Double.valueOf(elements[1]);
							Double simple = Double.valueOf(elements[2]);
							Double tot = Double.valueOf(elements[3]);
							if(Double.isInfinite(tot.doubleValue())){
								tot = 1000000000.0;
							}
							originalList.add(ori);
							simpleList.add(simple);
							totalList.add(tot);
							if(elements[5].equals("GT")){
								originalValue = originalValue >= ori ? originalValue : ori;
								simpleValue = simpleValue >= simple ? simpleValue : simple;
								totalValue = totalValue >= tot ? totalValue : tot;
							}
						}
						
						Collections.sort(originalList, Utils.DESCENDING);
						Collections.sort(simpleList, Utils.DESCENDING);
						Collections.sort(totalList, Utils.DESCENDING);
						
						int oriRank = Utils.computeRank(originalList, originalValue);
						int simpleRank = Utils.computeRank(simpleList, simpleValue);
						int totalRank = Utils.computeRank(totalList, totalValue);
						
						data.setOriValues(algName, oriRank);
						data.setSimpleValue(algName, simpleRank);
						data.setTotalValue(algName, totalRank);
						
						Utils.setRankMap(new int[]{oriRank, simpleRank, totalRank}, algName, top1Map, top3Map, top5Map, top10Map);
					}
					if(exist) {
						evaluateNum ++;
						stringBuffer.append(id);
						stringBuffer.append("\t");
						stringBuffer.append(data.toString());
						stringBuffer.append("\n");
					}
				}
				
				stringBuffer.append(Utils.assemble(single, top1Map, top3Map, top5Map, top10Map, 3, evaluateNum));
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
						if(contents != null && contents.size() > 0){
							Map<String, double[]> map = new HashMap<>();
							Map<String, String> predicateMap = new HashMap<>();
							stringBuffer = new StringBuffer(contents.get(0) + "\tlabel");
							stringBuffer.append("\n");
							for (int i = 1; i < contents.size(); i++) {
								String[] data = contents.get(i).split("\t");
								String method = data[0].substring(0, data[0].lastIndexOf("#"));
								double original = Double.parseDouble(data[1]);
								double max_predicate = Double.parseDouble(data[2]);
								double total = Double.parseDouble(data[3]);
								String predicate = "";
								if(data.length > 4) {
									predicate = data[4];
								}
								
								double[] value = map.get(method);
								
								if(value == null) {
									value = new double[3];
									value[0] = original;
									value[1] = max_predicate;
									value[2] = total;
									map.put(method, value);
									predicateMap.put(method, predicate);
								} else {
									value[0] = value[0] >= original ? value[0] : original;
									value[1] = value[1] >= max_predicate ? value[1] : max_predicate;
									if(value[2] < total) {
										value[2] = total;
										predicateMap.put(method, predicate);
									}
								}
							}
							
							for(Entry<String, double[]> entry2 : map.entrySet()) {
								String method = entry2.getKey();
								double[] value = entry2.getValue();
								stringBuffer.append(method + "\t" + value[0] + "\t" + value[1] + "\t" + value[2] + "\t" + predicateMap.get(method));
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
			
			String tarPath = System.getProperty("user.dir") + "/trans/simple/method";
			SimpleTransformation simpleTransformation = new SimpleTransformation();
			
			simpleTransformation.transform(tarPath);
			simpleTransformation.statistic(tarPath);
			
		}
	
}
