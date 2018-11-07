package cn.edu.pku;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.edu.pku.util.JavaFile;
import cn.edu.pku.util.Pair;
import cn.edu.pku.util.Utils;

/**
 * Combine predicates from predefined and predict results.
 * METHOD: select the maximum score (predicate) for each line 
 * @author Jiajun
 *
 */
public class CombineSd_Pred {

	private double epsilon = 1e-8;
	private String[] single = new String[]{ "Barinel_coverage", "DStar_coverage", "Ochiai_coverage", "Op2_coverage",
	"Tarantula_coverage"};
	
	private void combine(String predbase, String sdbase, String targetbase) {
		for(Entry<String, Integer> entry : Utils.PROJECTS.entrySet()) {
			String name = entry.getKey();
			int bound = entry.getValue();
			System.out.println("PROJECT : " + name);
			for(int id = 1; id <= bound; id++) {
				System.out.println("BUG : " + id);
				String preddatapath = predbase + "/" + name + "/" + name + "_" + id;
				String sddatapath = sdbase + "/" + name + "/" + name + "_" + id;
				if(checkFile(preddatapath, sddatapath)) {
					for(String formula : single) {
						Map<String, Pair<Double, Double>> lineMap = new HashMap<>();
						Map<String, String> predicatesMap = new HashMap<>();
						List<String> content_pred = JavaFile.readFileToStringList(preddatapath + "/" + formula + ".csv");
						for(int i = 1; i < content_pred.size(); i++) {
							String[] str = content_pred.get(i).split("\t");
							if(str.length < 4) {
								System.err.println("Pred Content error : " + content_pred.get(i));
								System.exit(0);
							}
							String key = str[0];
							Double ori = Double.parseDouble(str[1]);
							Double pred = Double.parseDouble(str[2]);
							String predicate = "";
							if(str.length >= 5) predicate = str[4];
							lineMap.put(key, new Pair<Double, Double>(ori, pred));
							predicatesMap.put(key, predicate);
						}
							
						List<String> content_sd = JavaFile.readFileToStringList(sddatapath + "/" + formula + "_sd.csv");
						for(int i = 1; i < content_sd.size(); i ++) {
							String[] str = content_sd.get(i).split("\t");
							if(str.length < 4) {
								System.err.println("SD Content error : " + content_pred.get(i));
								System.exit(0);
							}
							String key = str[0];
							Double ori = Double.parseDouble(str[1]);
							Double pred = Double.parseDouble(str[2]);
							String predicate = "";
							if(str.length >= 5) predicate = str[4];
							Pair<Double, Double> pair = lineMap.get(key);
							if(pair == null) {
								pair = new Pair<Double, Double>(ori, -1000.0);
							}
							if(Math.abs(pair.getFirst() - ori) > epsilon) {
								System.err.println("Original scores are inconsistent for : " + key);
//								System.exit(0);
							}
							if(pair.getSecond() < pred) {
								pair.setSecond(pred);
								predicatesMap.put(key, predicate);
							}
							lineMap.put(key, pair);
						}
						
						List<Entry<String, Pair<Double, Double>>> combine = new LinkedList<>(lineMap.entrySet());
						Collections.sort(combine, new Comparator<Entry<String, Pair<Double, Double>>>() {
							@Override
							public int compare(Entry<String, Pair<Double, Double>> o1,
									Entry<String, Pair<Double, Double>> o2) {
								Double d1 = o1.getValue().getFirst() + o1.getValue().getSecond();
								Double d2 = o2.getValue().getFirst() + o2.getValue().getSecond();
								return d2.compareTo(d1);
							}
						});

						StringBuffer stringBuffer = new StringBuffer();
						stringBuffer.append(content_pred.get(0));
						for(int i = 0; i < combine.size(); i++) {
							stringBuffer.append("\n");
							Entry<String, Pair<Double, Double>> line = combine.get(i);
							String key = line.getKey();
							double ori = line.getValue().getFirst();
							double combine_max = line.getValue().getSecond();
							double total = ori + combine_max;
							
							stringBuffer.append(key);
							stringBuffer.append("\t");
							stringBuffer.append(ori);
							stringBuffer.append("\t");
							stringBuffer.append(combine_max);
							stringBuffer.append("\t");
							stringBuffer.append(total);
							stringBuffer.append("\t");
							stringBuffer.append(predicatesMap.get(key));
						}
						JavaFile.writeStringToFile(targetbase + "/" + name + "/" + name + "_" + id + "/" + formula + "_combine.csv", stringBuffer.toString());
					}
				}
			}
		}
	}
	
	private boolean checkFile(String pred, String sd) {
		File file1 = null;
		File file2 = null;
		for(String string : single) {
			file1 = new File(pred + "/" + string + ".csv");
			file2 = new File(sd + "/" + string + "_sd.csv");
			if(!file1.exists() || !file2.exists()) {
				return false;
			}
		}
		return true;
	}
	
	
	public static void main(String[] args) {
		String predbase = System.getProperty("user.dir") + "/info/pred_data";
		String sdbase = System.getProperty("user.dir") + "/info/sd_data";
		String targetbase = System.getProperty("user.dir") + "/info/combine";
		
		CombineSd_Pred combineSd_Pred = new CombineSd_Pred();
		combineSd_Pred.combine(predbase, sdbase, targetbase);
	}
}
