package cn.edu.pku;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.edu.pku.util.JavaFile;

public class AddWeightForPredicates {
	
	private String base = "";
	private String[] single = null;
	private String predicateFileName = ".csv";
	
	public static Map<String, Integer> projects;
	
	public AddWeightForPredicates(KIND kind) {
		switch(kind) {
		case PREDICT:
			init_predict();
			break;
		case STATISTIC:
			init_statistic();
			break;
		default:
			System.err.println("Unknown kind : " + kind);
		}
		projects = new HashMap<>();
		projects.put("chart", 26);
		projects.put("time", 27);
		projects.put("lang", 65);
		projects.put("math", 106);
//		projects.put("closure", 133);
//		projects.put("mockito", 38);
	}
	
	public static enum KIND{
		PREDICT,
		STATISTIC
	}
	
	private void init_predict() {
		predicateFileName = "pred_coverage.csv";
		base = System.getProperty("user.dir") + "/info/pred_data";
		single = new String[]{ "Barinel_coverage.csv", "DStar_coverage.csv", "Ochiai_coverage.csv", "Op2_coverage.csv",
		"Tarantula_coverage.csv"};
	}
	
	private void init_statistic() {
		predicateFileName = "pred_coverage_sd.csv";
		base = System.getProperty("user.dir") + "/info/sd_data";
		single = new String[]{ "Barinel_coverage_sd.csv", "DStar_coverage_sd.csv", "Ochiai_coverage_sd.csv", "Op2_coverage_sd.csv",
		"Tarantula_coverage_sd.csv"};
	}
	
	private Map<String, Double> readWeightForEachPredicates(String projName, int id) {
		String filePath = base + "/" + projName + "/" + projName + "_" + id + "/" + predicateFileName;
		File file = new File(filePath);
		if(!file.exists()) {
			System.err.println("Failed to read weight for predicates, file (" + filePath + ") does not exist.");
			return null;
		}
		Map<String, Double> predicate2Score = new HashMap<>();
		List<String> content = JavaFile.readFileToStringList(file);
		//org.xxx.clazz#type#name#?,type#1665#predicate#score	0	0	1	7
		for(int line = 1; line < content.size(); line ++) {
			String string = content.get(line); 
			String[] data = string.split("\t");
			if(data.length != 5) {
				System.err.println("Data format error : " + filePath + "\n>>" + string);
				continue;
			}
			String key = data[0];
			if(key.split("#").length < 7) {
				System.out.println("Ignore : " + key);
				continue;
			}
			int index = key.lastIndexOf("#");
			String linePredicate = key.substring(0, index);
			Double score = new Double(key.substring(index + 1));
			predicate2Score.put(linePredicate, score);
		}
		return predicate2Score;
	}
	
	static enum CombineType{
		GEOMETRY_LINEAR,
		MAX,
		WEIGHT_PREDICT
	}
	
	public void transform(String tarPath, CombineType type) {
		for(Entry<String, Integer> proj : projects.entrySet()) {
			String name = proj.getKey();
			Integer bound = proj.getValue();
			for(int id = 1; id <= bound; id ++) {
				Map<String, Double> predScoreMap = readWeightForEachPredicates(name, id);
				if(predScoreMap == null) continue;
				String tarFile = tarPath + "/" + name + "/" + name + "_" + id + "/";
				for(String alg : single) {
					String srcFile = base + "/" + name + "/" + name + "_" + id + "/" + alg;
					List<String> content = JavaFile.readFileToStringList(srcFile);
					StringBuffer newBuff = new StringBuffer(content.get(0) + "\n");
					for(int line = 1; line < content.size(); line ++) {
						String lineStr = content.get(line);
						//org.xxx.classr#type#name#type#line	  ori_score	 pred_score	 tot_score	predicate
						String[] data = lineStr.split("\t");
						boolean added = false;
						if(data.length == 5 && data[4].length() > 0) {
							String linePred = data[0] + "#" + data[4];
							Double score = predScoreMap.get(linePred);
							if(score != null) {
								added = true;
								// re-calculate the total score with considering weights of predicates
								newBuff.append(data[0] + "\t");
								newBuff.append(data[1] + "\t");
								newBuff.append(data[2] + "\t");
								double original = Double.parseDouble(data[1]);
								double pred = Double.parseDouble(data[2]);
								double weighted =0;
//								weighted = pred + original * 2.0;
								switch (type) {
								case GEOMETRY_LINEAR:
									weighted = pred * score.doubleValue() + original * (1 - score.doubleValue());
									break;
								case MAX:
									weighted = pred > original ? pred : original;
									break;
								case WEIGHT_PREDICT:
									weighted = pred * score.doubleValue() + original;
									break;
								default:
									break;
								}
								newBuff.append(weighted + "\t");
								newBuff.append(data[4] + "#" + score + "\n");
							} else {
								System.err.println("Not find predicate : " + linePred);
							}
							
						} 
						if(!added) {
							newBuff.append(lineStr + "\n");
						}
					}
					JavaFile.writeStringToFile(tarFile + alg, newBuff.toString());
				}
			}
		}
	}
	
	public static void main(String[] args) {
		//comment out the following code snippet if not transforming predict method
		String target = System.getProperty("user.dir") + "/info/score_pred";
		AddWeightForPredicates addWFP = new AddWeightForPredicates(KIND.PREDICT);
		
		//comment out the following code snippet if not transforming statistical debugging method
//		String target = System.getProperty("user.dir") + "/info/score_sd";
//		AddWeightForPredicates addWFP = new AddWeightForPredicates(KIND.STATISTIC);
		
		// true: original_score * (1 - probability) + predict_score * probability
		// false: original_score + predict_score * probability
		CombineType type = CombineType.MAX;
		
		addWFP.transform(target, type);
		System.out.println("Finished!");
	}
}
