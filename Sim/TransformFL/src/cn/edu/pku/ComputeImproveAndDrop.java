package cn.edu.pku;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.edu.pku.util.JavaFile;
import cn.edu.pku.util.Utils;

public class ComputeImproveAndDrop {

	private Map<ALG, Integer> alg2IndexMap = new HashMap<>();
	private Map<STATUS, Integer> top2IndexMap = new HashMap<>();
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
	
	public static enum STATUS {
		IMPROVE("Improve"),
		DROP("Drop");
		private String value;
		private STATUS(String val) {value = val;}
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static enum COMETHOD {
		PredSBFL("PredSBFL"),
		CombSD("CombSD");
		private String value;
		private COMETHOD(String val) { value = val; }
		@Override
		public String toString() {
			return value;
		}
	}
	
	public ComputeImproveAndDrop() {
		alg2IndexMap.put(ALG.Barinel, 2);
		alg2IndexMap.put(ALG.DStar, 6);
		alg2IndexMap.put(ALG.Ochiai, 10);
		alg2IndexMap.put(ALG.Op2, 14);
		alg2IndexMap.put(ALG.Tarantula, 18);
		
		top2IndexMap.put(STATUS.IMPROVE, 0);
		top2IndexMap.put(STATUS.DROP, 1);
		
		combMethod2IndexMap.put(COMETHOD.PredSBFL, 0);
		combMethod2IndexMap.put(COMETHOD.CombSD, 1);
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
				if(line.startsWith("improve_drop")) {
					String[] data = line.split("\t");
					for(Entry<ALG, Integer> algIdx : alg2IndexMap.entrySet()) {
						String[] predTopData = data[algIdx.getValue()].split("_");
						String[] combinedTopData = data[algIdx.getValue() + step].split("_");
						Integer[][] top = new Integer[2][4];
						
						top[combMethod2IndexMap.get(COMETHOD.PredSBFL)][top2IndexMap.get(STATUS.IMPROVE)] = new Integer(predTopData[0]);
						top[combMethod2IndexMap.get(COMETHOD.PredSBFL)][top2IndexMap.get(STATUS.DROP)] = new Integer(predTopData[1]);
						
						top[combMethod2IndexMap.get(COMETHOD.CombSD)][top2IndexMap.get(STATUS.IMPROVE)] = new Integer(combinedTopData[0]);
						top[combMethod2IndexMap.get(COMETHOD.CombSD)][top2IndexMap.get(STATUS.DROP)] = new Integer(combinedTopData[1]);
						
						alg2comb2topkMap.put(algIdx.getKey(), top);								
					}
					break;
				}
			}
			proj2alg2comb2topkMap.put(name, alg2comb2topkMap);
		}
		return proj2alg2comb2topkMap;
	}
	
	public Map<STATUS, Integer> computeNumberForEachStatusWhenGivenAlgAndMethod(Map<String, Map<ALG, Integer[][]>> proj2alg2comb2topkMap, ALG alg, COMETHOD comethod) {
		Map<STATUS, Integer> map = new HashMap<>();
		int methodIdx = combMethod2IndexMap.get(comethod);
		for(Entry<String, Map<ALG, Integer[][]>> entry : proj2alg2comb2topkMap.entrySet()) {
			Integer[][] values = entry.getValue().get(alg);
			for(Entry<STATUS, Integer> rank : top2IndexMap.entrySet()) {
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
	
	public Map<STATUS, Integer> computeNumberForEachStatusWhenGivenAlgAndMethodAndProject(Map<String, Map<ALG, Integer[][]>> proj2alg2comb2topkMap, ALG alg, COMETHOD comethod, String projName) {
		Map<STATUS, Integer> map = new HashMap<>();
		int methodIdx = combMethod2IndexMap.get(comethod);
		Integer[][] values = proj2alg2comb2topkMap.get(projName).get(alg);
		for(Entry<STATUS, Integer> rank : top2IndexMap.entrySet()) {
			Integer count = map.get(rank.getKey());
			if (count == null) {
				count = new Integer(0);
			}
			count += values[methodIdx][top2IndexMap.get(rank.getKey())];
			map.put(rank.getKey(), count);
		}
		return map;
	}
	
	public Map<ALG, Integer> computeNumberForEachAlgWhenGivenStatusAndMethod(Map<String, Map<ALG, Integer[][]>> proj2alg2comb2topkMap, COMETHOD comethod, STATUS topk) {
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
		String base = System.getProperty("user.dir") + "/trans/statistic/method";
		ComputeImproveAndDrop computeImproveAndDrop = new ComputeImproveAndDrop();
		
		Map<STATUS, Integer> map2 = computeImproveAndDrop.computeNumberForEachStatusWhenGivenAlgAndMethod(computeImproveAndDrop.parse(base), ALG.Tarantula, COMETHOD.CombSD);
		for(Entry<STATUS, Integer> entry : map2.entrySet()) {
			System.out.println(entry.getKey().toString() + " : " + entry.getValue());
		}
//		String[] projects = new String[] {"chart", "lang", "math", "time", "closure"};
//		for(String projName : projects) {
//			Map<STATUS, Integer> map3 = computeImproveAndDrop.computeNumberForEachStatusWhenGivenAlgAndMethodAndProject(computeImproveAndDrop.parse(base), ALG.Barinel, COMETHOD.CombSD, projName);
//			System.out.println(projName);
//			for(Entry<STATUS, Integer> entry : map3.entrySet()) {
//				System.out.println(entry.getKey().toString() + " : " + entry.getValue());
//			}
//		}
	}
}
