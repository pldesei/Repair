package cn.edu.pku;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.edu.pku.util.JavaFile;
import net.sf.json.JSONObject;

/**
 * This class is used for output line scores for each algorithm,
 * precondition: already compute line coverage
 * output: a json file
 * @author Jiajun
 *
 */
public class Learning2Rank {
	// line original_score max_predicate_score total predicate
	private String[] single = null;
	private String base = null;

	private Map<String, Integer> projects;

	private void init_predict() {
		base = System.getProperty("user.dir") + "/trans/predict/line";
		single = new String[] { "Barinel_coverage.csv", "DStar_coverage.csv", "Ochiai_coverage.csv", "Op2_coverage.csv",
				"Tarantula_coverage.csv" };
	}

	private void init_statistic() {
		base = System.getProperty("user.dir") + "/trans/statistic/line";
		single = new String[] { "Barinel_coverage_sd.csv", "DStar_coverage_sd.csv", "Ochiai_coverage_sd.csv",
				"Op2_coverage_sd.csv", "Tarantula_coverage_sd.csv" };
	}

	public static enum KIND {
		PREDICT, STATISTIC
	}

	public Learning2Rank(KIND kind) {
		projects = new HashMap<>();
		projects.put("chart", 26);
		projects.put("time", 27);
		projects.put("lang", 65);
		projects.put("math", 106);
//		projects.put("closure", 133);
		// projects.put("mockito", 38);
		switch (kind) {
		case PREDICT:
			init_predict();
			break;
		case STATISTIC:
			init_statistic();
			break;
		default:
			System.err.println("Unkown category! :" + kind);
		}
	}

	private Map<String, Map<String, Map<String, Double>>> organize() {
		Map<String, Map<String, Map<String, Double>>> allBugs = new HashMap<>();
		for(Entry<String, Integer> entry : projects.entrySet()) {
			String name = entry.getKey();
			int bound = entry.getValue();
			for(int id = 1; id <= bound; id++) {
				Map<String, Map<String, Double>> methods = new HashMap<>();
				for(String alg : single) {
					String file = base + "/" + name + "/" + name + "_" + id + "/" + alg;
					String method = alg.split("_")[0] + "Pred";
					List<String> contents = JavaFile.readFileToStringList(file);
					for(int i = 1; i < contents.size(); i ++) {
						String[] str = contents.get(i).split("\t");
						if(str.length < 4) {
							System.err.println("Content error : " + contents.get(i));
							continue;
						}
						String line = str[0];
						Double predScore = Double.parseDouble(str[2]);
						if(Double.isInfinite(predScore)) predScore = 1.0;
						if(Double.isNaN(predScore)) predScore = 0.0;
						String[] details = line.split("#");
						if(details.length < 5) {
							System.err.println("Line error : " + line);
							continue;
						}
						String clazz = details[0];
						int index = clazz.indexOf("$");
						if(index > 0) {
							clazz = clazz.substring(0, index);
						}
						String key = clazz + ":" + details[4];
						Map<String, Double> singleMeth = methods.get(key);
						if(singleMeth == null) {
							singleMeth = new HashMap<>();
						}
						singleMeth.put(method, predScore);
						methods.put(key, singleMeth);
					}
				}
				allBugs.put(name + id, methods	);
			}
		}
		return allBugs;
	}
	
	private void outAsJson(Map<String, Map<String, Map<String, Double>>> allBugs, String path) {
		JSONObject all = new JSONObject();
		for(Entry<String, Map<String, Map<String, Double>>> entry : allBugs.entrySet()) {
			JSONObject oneBug = new JSONObject();
			String bugid = entry.getKey();
			for(Entry<String, Map<String, Double>> lineInfo : entry.getValue().entrySet()) {
				String line = lineInfo.getKey();
				JSONObject oneLine = new JSONObject();
				for(Entry<String, Double> methodInfo : lineInfo.getValue().entrySet()) {
					String method = methodInfo.getKey();
					Double score = methodInfo.getValue();
					if(score == null) score = 0.0;
					System.out.println(bugid + " : " + method + " : " + score);
					oneLine.accumulate(method, score.doubleValue());
				}
				oneBug.accumulate(line, oneLine);
			}
			all.accumulate(bugid, oneBug);
		}
		String filePath = path + "/add_in.json";
		File file = new File(filePath);
		JavaFile.writeStringToFile(file, all.toString());
	}

	public static void main(String[] args) {
		Learning2Rank learning2Rank = new Learning2Rank(KIND.PREDICT	);
		learning2Rank.outAsJson(learning2Rank.organize(), "/Users/Jiajun/Desktop");
		System.out.println("Finish!");
	}
}
