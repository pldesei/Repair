package cn.edu.pku;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.edu.pku.util.JavaFile;

public class LinearSbflAndSdScore {

	private String base = "";
	private String[] single = null;
	
	public static Map<String, Integer> projects;
	
	public LinearSbflAndSdScore(KIND kind) {
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
		projects.put("closure", 133);
//		projects.put("mockito", 38);
	}
	
	public static enum KIND{
		PREDICT,
		STATISTIC
	}
	
	private void init_predict() {
		base = System.getProperty("user.dir") + "/info/pred_data";
		single = new String[]{ "Barinel_coverage.csv", "DStar_coverage.csv", "Ochiai_coverage.csv", "Op2_coverage.csv",
		"Tarantula_coverage.csv"};
	}
	
	private void init_statistic() {
		base = System.getProperty("user.dir") + "/info/sd_data";
		single = new String[]{ "Barinel_coverage_sd.csv", "DStar_coverage_sd.csv", "Ochiai_coverage_sd.csv", "Op2_coverage_sd.csv",
		"Tarantula_coverage_sd.csv"};
	}
	
	static enum CombineType{
		GEOMETRY_LINEAR,
		MAX,
		WEIGHT_PREDICT
	}
	
	public void transform(String tarPath, double weightpred) {
		for(Entry<String, Integer> proj : projects.entrySet()) {
			String name = proj.getKey();
			Integer bound = proj.getValue();
			for(int id = 1; id <= bound; id ++) {
				String tarFile = tarPath + "/" + name + "/" + name + "_" + id + "/";
				for(String alg : single) {
					String srcFile = base + "/" + name + "/" + name + "_" + id + "/" + alg;
					File file = new File(srcFile);
					if (!file.exists()) {
						continue;
					}
					List<String> content = JavaFile.readFileToStringList(srcFile);
					if(content == null || content.size() == 0) continue;
					StringBuffer newBuff = new StringBuffer(content.get(0) + "\n");
					for(int line = 1; line < content.size(); line ++) {
						String lineStr = content.get(line);
						//org.xxx.classr#type#name#type#line	  ori_score	 pred_score	 tot_score	predicate
						String[] data = lineStr.split("\t");
						if(data.length >= 4) {
							// re-calculate the total score with considering weights of predicates
							newBuff.append(data[0] + "\t");
							newBuff.append(data[1] + "\t");
							newBuff.append(data[2] + "\t");
							double original = Double.parseDouble(data[1]);
							double pred = Double.parseDouble(data[2]);
							double weighted =original * (1 - weightpred) + pred * weightpred;
							newBuff.append(weighted + "\t");
							if(data.length == 5) {
								newBuff.append(data[4]);
							}
							newBuff.append("\n");
						} else {
							System.out.println("Data format error : " + lineStr);
						}
					}
					JavaFile.writeStringToFile(tarFile + alg, newBuff.toString());
				}
			}
		}
	}
	
	public static void main(String[] args) {
		//comment out the following code snippet if not transforming predict method
//		String target = System.getProperty("user.dir") + "/info/linear_pred";
//		LinearSbflAndSdScore addWFP = new LinearSbflAndSdScore(KIND.PREDICT);
		
		//comment out the following code snippet if not transforming statistical debugging method
		String target = System.getProperty("user.dir") + "/info/linear_sd-0.5";
		LinearSbflAndSdScore addWFP = new LinearSbflAndSdScore(KIND.STATISTIC);
		
		addWFP.transform(target, 0.5);
		System.out.println("Finished!");
	}
}
