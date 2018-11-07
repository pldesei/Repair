package cn.edu.pku;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.edu.pku.util.JavaFile;
import cn.edu.pku.util.Utils;

public class TopkOnAllProjects {

	private Map<ALG, Integer> alg2IndexMap = new HashMap<>();
	private Map<RANK, Integer> top2IndexMap = new HashMap<>();
	private Map<COMETHOD, Integer> combMethod2IndexMap = new HashMap<>();
	private int step = 2;
	
	public static enum ALG {
		Barinel("Barinel"),
		DStar("DStar"),
		Ochiai("Ochiai"),
		Op2("Op2"),
		Tarantula("Tarantula");
		private String value;
		private ALG(String val) {value = val;}
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static enum RANK {
		top1("top1"),
		top3("top3"),
		top5("top5"),
		top10("top10");
		private String value;
		private RANK(String val) {value = val;}
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static enum COMETHOD {
		Original("Original"),
		PredSBFL("PredSBFL"),
		CombSD("CombSD");
		private String value;
		private COMETHOD(String val) { value = val; }
		@Override
		public String toString() {
			return value;
		}
	}
	
	public TopkOnAllProjects() {
		alg2IndexMap.put(ALG.Barinel, 2);
		alg2IndexMap.put(ALG.DStar, 6);
		alg2IndexMap.put(ALG.Ochiai, 10);
		alg2IndexMap.put(ALG.Op2, 14);
		alg2IndexMap.put(ALG.Tarantula, 18);
		
		top2IndexMap.put(RANK.top1, 0);
		top2IndexMap.put(RANK.top3, 1);
		top2IndexMap.put(RANK.top5, 2);
		top2IndexMap.put(RANK.top10, 3);
		
		combMethod2IndexMap.put(COMETHOD.Original, 0);
		combMethod2IndexMap.put(COMETHOD.PredSBFL, 1);
		combMethod2IndexMap.put(COMETHOD.CombSD, 2);
	}
	
	/**
	 * record the number of "each project" using "each formula" for "each combined method" locating "top 1 3 5 10"  
	 * @param base
	 * @return
	 */
	public Map<String, Map<ALG, Integer[][]>> parse(String base) {
		Map<String, Map<ALG, Integer[][]>> proj2alg2comb2topkMap = new HashMap<>(); 
		for(Entry<String, Integer> entry : Utils.PROJECTS.entrySet()) {
			String name = entry.getKey();
			Map<ALG, Integer[][]> alg2comb2topkMap = new HashMap<>();
			String fileName = base + "/" + name + "/" + name + "_statistic.csv";
			List<String> contents = JavaFile.readFileToStringList(fileName);
			if(contents == null || contents.size() <= 0 ) {
				System.err.println("Empty data! : " + fileName);
				System.exit(0);
			}
			for(int i = 0; i < contents.size(); i ++) {
				String line = contents.get(i);
				if(line.startsWith("top_1_3_5_10")) {
					String[] data = line.split("\t");
					for(Entry<ALG, Integer> algIdx : alg2IndexMap.entrySet()) {
						String[] originalTopData = data[algIdx.getValue()-1].split("_");
						String[] predTopData = data[algIdx.getValue()].split("_");
						String[] combinedTopData = data[algIdx.getValue() + step].split("_");
						Integer[][] top = new Integer[3][4];
						top[combMethod2IndexMap.get(COMETHOD.Original)][top2IndexMap.get(RANK.top1)] = new Integer(originalTopData[0]);
						top[combMethod2IndexMap.get(COMETHOD.Original)][top2IndexMap.get(RANK.top3)] = new Integer(originalTopData[1]);
						top[combMethod2IndexMap.get(COMETHOD.Original)][top2IndexMap.get(RANK.top5)] = new Integer(originalTopData[2]);
						top[combMethod2IndexMap.get(COMETHOD.Original)][top2IndexMap.get(RANK.top10)] = new Integer(originalTopData[3]);
						
						top[combMethod2IndexMap.get(COMETHOD.PredSBFL)][top2IndexMap.get(RANK.top1)] = new Integer(predTopData[0]);
						top[combMethod2IndexMap.get(COMETHOD.PredSBFL)][top2IndexMap.get(RANK.top3)] = new Integer(predTopData[1]);
						top[combMethod2IndexMap.get(COMETHOD.PredSBFL)][top2IndexMap.get(RANK.top5)] = new Integer(predTopData[2]);
						top[combMethod2IndexMap.get(COMETHOD.PredSBFL)][top2IndexMap.get(RANK.top10)] = new Integer(predTopData[3]);
						
						top[combMethod2IndexMap.get(COMETHOD.CombSD)][top2IndexMap.get(RANK.top1)] = new Integer(combinedTopData[0]);
						top[combMethod2IndexMap.get(COMETHOD.CombSD)][top2IndexMap.get(RANK.top3)] = new Integer(combinedTopData[1]);
						top[combMethod2IndexMap.get(COMETHOD.CombSD)][top2IndexMap.get(RANK.top5)] = new Integer(combinedTopData[2]);
						top[combMethod2IndexMap.get(COMETHOD.CombSD)][top2IndexMap.get(RANK.top10)] = new Integer(combinedTopData[3]);
						
						alg2comb2topkMap.put(algIdx.getKey(), top);								
					}
					break;
				}
			}
			proj2alg2comb2topkMap.put(name, alg2comb2topkMap);
		}
		return proj2alg2comb2topkMap;
	}
	
	public Map<RANK, Integer> computeNumberForEachRankWhenGivenAlgAndMethod(Map<String, Map<ALG, Integer[][]>> proj2alg2comb2topkMap, ALG alg, COMETHOD comethod) {
		Map<RANK, Integer> map = new HashMap<>();
		int methodIdx = combMethod2IndexMap.get(comethod);
		for(Entry<String, Map<ALG, Integer[][]>> entry : proj2alg2comb2topkMap.entrySet()) {
			Integer[][] values = entry.getValue().get(alg);
			for(Entry<RANK, Integer> rank : top2IndexMap.entrySet()) {
				Integer count = map.get(rank.getKey());
				if (count == null) {
					count = new Integer(0);
				}
				count += values[methodIdx][top2IndexMap.get(rank.getKey())];
				map.put(rank.getKey(), count);
			}
		}
		return map;
	}
	
	public Map<ALG, Integer> computeNumberForEachAlgWhenGivenRankAndMethod(Map<String, Map<ALG, Integer[][]>> proj2alg2comb2topkMap, COMETHOD comethod, RANK topk) {
		Map<ALG, Integer	> map = new HashMap<>();
		int methodIdx = combMethod2IndexMap.get(comethod);
		int topkIdx = top2IndexMap.get(topk);
		for(Entry<String, Map<ALG, Integer[][]>> entry : proj2alg2comb2topkMap.entrySet()) {
			for(Entry<ALG, Integer> alg : alg2IndexMap.entrySet()) {
				Integer[][] values = entry.getValue().get(alg.getKey());
				Integer count = map.get(alg.getKey());
				 if(count == null) {
					 count = new Integer(0);
				 }
				 count += values[methodIdx][topkIdx];
				 map.put(alg.getKey(), count);
			}
		}
		return map;
	}
	
	public static void main(String[] args) {
		String base = System.getProperty("user.dir") + "/trans/linear_sd-0.5/line";
		TopkOnAllProjects topkOnAllProjects = new TopkOnAllProjects();
//		Map<ALG, Integer> map = topkOnAllProjects.computeTopkForEachMethodOnAllProjectAndAllAlgs(topkOnAllProjects.parse(base), COMETHOD.CombSD, RANK.top1);
//		for(Entry<ALG, Integer> entry : map.entrySet()) {
//			System.out.println(entry.getKey().toString() + " : " + entry.getValue());
//		}
		
		Map<RANK, Integer> map2 = topkOnAllProjects.computeNumberForEachRankWhenGivenAlgAndMethod(topkOnAllProjects.parse(base), ALG.Tarantula, COMETHOD.Original);
		for(Entry<RANK, Integer> entry : map2.entrySet()) {
			System.out.println(entry.getKey().toString() + " : " + entry.getValue());
		}
	}
	
}
