package cofix.gz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.localization.ManualLocator;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.parser.ProjectInfo;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.search.BuggyCode;
import cofix.core.parser.search.SimpleFilter;

public class GetPatchCandidate {
	String basePath = "/Users/Sonia/defects4j/";
	String projectName = "Chart";
	int bugID = 2;
	String versionID;
	
	String patchPath;
	String resultPath;
	String buggyVersionPath;
	String fixedVersionPath;
	
	String projectFixedRepo;
	String projectBuggyRepo;
	
	String patchSuffix = ".src.patch";
	String buggyResultName = "/buggySim.txt";
	String fixedResultName = "/fixedSim.txt";
	
	List<CodeBlock> fixedBlockList = new LinkedList<>();
	List<CodeBlock> buggyBlockList = new LinkedList<>();
	
	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out.println("Arguments Wrong!");
		}
		else {
			GetPatchCandidate gpc = new GetPatchCandidate();
			args[0] = "/Users/Sonia/defects4j";
			args[1] = "lang";
			args[2] = "65";
			args[3] = "1";
			
			int[] a = {1, 2, 3, 4, 5, 6,  7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 24, 25, 26, 29, 30, 31, 32, 34, 35, 36, 37, 38, 40, 44, 45, 46, 47, 48, 49, 51, 52, 53, 54, 55, 57, 59, 61, 62, 63, 64, 65};
			
			for (int i = 0; i < a.length; i++) {
				args[2] = a[i] + "";
			gpc.fixedBlockList.clear();
			gpc.buggyBlockList.clear();
			gpc.basePath = args[0];
			gpc.projectName = args[1];
			gpc.bugID = Integer.parseInt(args[2]);
			gpc.versionID = args[3];
			
			//gpc.projectRepo = args[0] + "/repos/" + args[1];
			
			gpc.patchPath = args[0] + "/framework/projects/" + args[1] + "/patches/" + args[2] + gpc.patchSuffix;
			gpc.resultPath = args[0] + "/results/" + args[1] + "/" + args[2] + "/" + args[3];
			File file = new File(gpc.resultPath);
			if (!file.exists())
				file.mkdirs();
			gpc.buggyVersionPath = args[0] + "/buggyVersions/" + args[1].toLowerCase() + "/" + args[1].toLowerCase() + "_" + args[2] + "_buggy";
			gpc.fixedVersionPath = args[0] + "/fixedVersions/" + args[1].toLowerCase() + "/" + args[1].toLowerCase() + "_" + args[2] + "_fixed";
			
			gpc.projectBuggyRepo = gpc.buggyVersionPath;
			gpc.projectFixedRepo = gpc.fixedVersionPath;
			
			gpc.getBuggyBlock();
			gpc.getFixedBlock();
			gpc.getBuggyCandidates();
			gpc.getFixedCandidates();
			System.out.println("Finish!");
			}
		}
	}
	
	public void getBuggyBlock() throws Exception {
		Constant.PROJECT_HOME = basePath + "/buggyVersions";
		String projName = projectName.toLowerCase();
		
		Configure.configEnvironment();
		System.out.println(Constant.PROJECT_HOME);
		
		Subject subject = Configure.getSubject(projName, bugID);
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("=================================================\n");
		stringBuffer.append("Project : " + subject.getName() + "_" + subject.getId() + "\t");
		SimpleDateFormat simpleFormat=new SimpleDateFormat("yy/MM/dd HH:mm"); 
		stringBuffer.append("start : " + simpleFormat.format(new Date()) + "\n");
		System.out.println(stringBuffer.toString());
		
		ProjectInfo.init(subject);
		AbstractFaultlocalization fLocalization = new ManualLocator(subject);
		List<Pair<String, Integer>> locations = fLocalization.getLocations(200);
		for(Pair<String, Integer> loc : locations){
			FileUtils.deleteDirectory(new File(subject.getHome() + subject.getSbin()));
			FileUtils.deleteDirectory(new File(subject.getHome() + subject.getTbin()));
			System.out.println(loc.getFirst() + "," + loc.getSecond());
				
			String file = subject.getHome() + subject.getSsrc() + "/" + loc.getFirst().replace(".", "/") + ".java";
			CodeBlock buggyblock = BuggyCode.getBuggyCodeBlock(file, loc.getSecond());
				
			buggyBlockList.addAll(buggyblock.reduce());
			buggyBlockList.add(buggyblock);
		}		
	}
	
	public void getBuggyCandidates() throws Exception {
		BufferedWriter w = new BufferedWriter(new FileWriter(resultPath + buggyResultName));
		Set<String> haveTryBuggySourceCode = new HashSet<>();
		for(CodeBlock oneBuggyBlock : buggyBlockList){
			String currentBlockString = oneBuggyBlock.toSrcString().toString();
			if(currentBlockString == null || currentBlockString.length() <= 0){
				continue;
			}
			if(haveTryBuggySourceCode.contains(currentBlockString)){
				continue;
			}
			haveTryBuggySourceCode.add(currentBlockString);
					
			SimpleFilter simpleFilter = new SimpleFilter(oneBuggyBlock);	
			List<Pair<CodeBlock, Double>> candidates = simpleFilter.filter(projectBuggyRepo, 0.3);
					
			int cnt = 1;
			if (candidates == null || candidates.size() == 0)
				continue;
			//System.out.println("fixed block:");
			//System.out.println(oneFixedBlock.toSrcString().toString());
			w.write("=================buggy block=================\n");
			w.write(oneBuggyBlock.toSrcString().toString() + "\n");
			for(Pair<CodeBlock, Double> similar : candidates){
				// try top 50 candidates
				if(cnt > 50){
					break;
				}
				//System.out.println("No." + cnt + " candidate:");
				//System.out.println(similar.getFirst().toSrcString().toString());
				CodeBlock simBlock = similar.getFirst();
				w.write("=================" + cnt + "=====Sim:" + similar.getSecond() + "=================\n");
				w.write("FileName:" + simBlock.getFileName() + "   LineRange:" + simBlock.getLineRangeInSource() + "\n");
				w.write(simBlock.toSrcString().toString() + "\n");
				cnt ++;
			}
		}
		
		w.close();
	}
	
	public void getFixedBlock() throws Exception {
		HashMap<String, HashMap<Integer, Integer>> lines = analyzeFile(patchPath);
		for (String str : lines.keySet()) {
			System.out.println(str);
			System.out.println(lines.get(str));
			HashMap<Integer, Integer> lineRange = lines.get(str);
			if (lineRange.size() > 0) {
				String filePath = fixedVersionPath + "/" + str;
				for (int start : lineRange.keySet()) {
					int end = lineRange.get(start);
					if (end - start <= 1) {
						CodeBlock fixedBlock = BuggyCode.getBuggyCodeBlock(filePath, start);
						fixedBlockList.addAll(fixedBlock.reduce());
						fixedBlockList.add(fixedBlock);
					}
					else {
						CodeBlock fixedBlock = BuggyCode.getFixedCodeBlock(filePath, start, end);
						fixedBlockList.addAll(fixedBlock.reduce());
						fixedBlockList.add(fixedBlock);
					}
				}
			}
		}
	}
	
	public void getFixedCandidates() throws Exception {
		BufferedWriter w = new BufferedWriter(new FileWriter(resultPath + fixedResultName));
		Set<String> haveTryBuggySourceCode = new HashSet<>();
		for(CodeBlock oneFixedBlock : fixedBlockList){
			String currentBlockString = oneFixedBlock.toSrcString().toString();
			if(currentBlockString == null || currentBlockString.length() <= 0){
				continue;
			}
			if(haveTryBuggySourceCode.contains(currentBlockString)){
				continue;
			}
			haveTryBuggySourceCode.add(currentBlockString);
					
			SimpleFilter simpleFilter = new SimpleFilter(oneFixedBlock);	
			List<Pair<CodeBlock, Double>> candidates = simpleFilter.filter(projectFixedRepo, 0.3);
					
			int cnt = 1;
			if (candidates == null || candidates.size() == 0)
				continue;
			//System.out.println("fixed block:");
			//System.out.println(oneFixedBlock.toSrcString().toString());
			w.write("=================fixed block=================\n");
			w.write(oneFixedBlock.toSrcString().toString() + "\n");
			for(Pair<CodeBlock, Double> similar : candidates){
				// try top 50 candidates
				if(cnt > 50){
					break;
				}
				//System.out.println("No." + cnt + " candidate:");
				//System.out.println(similar.getFirst().toSrcString().toString());
				CodeBlock simBlock = similar.getFirst();
				w.write("=================" + cnt + "=====Sim:" + similar.getSecond() + "=================\n");
				w.write("FileName:" + simBlock.getFileName() + "   LineRange:" + simBlock.getLineRangeInSource() + "\n");
				w.write(simBlock.toSrcString().toString() + "\n");
				cnt ++;
			}
		}
		w.close();
	}
	
	private HashMap<String, HashMap<Integer, Integer>> analyzeFile(String filePath) throws Exception {
		HashMap<String, HashMap<Integer, Integer>> ret = new HashMap<>();
		BufferedReader r = new BufferedReader(new FileReader(new File(filePath)));
		String tmp = r.readLine();
		int startLine = 0;
		while (tmp != null) {
			if (tmp.startsWith("---")) {
				HashMap<Integer, Integer> lines = new HashMap<>();
				if (tmp.startsWith("--- a/")) {
					String path = tmp.substring(tmp.indexOf("/") + 1, tmp.indexOf(".java") + 5);
					ret.put(path, lines);
				}
				else {
					String path = tmp.substring(tmp.indexOf(" ") + 1, tmp.indexOf(".java") + 5);
					ret.put(path, lines);
				}
				
				tmp = r.readLine();
				while (tmp != null && !tmp.startsWith("---")) {
					while (tmp != null && tmp.startsWith("@@")) {
						String start = tmp.substring(tmp.indexOf("-") + 1, tmp.indexOf(","));
						startLine = Integer.parseInt(start);
						tmp = r.readLine();
						ArrayList<Integer> lineNums = new ArrayList<>();
						boolean flag1 = tmp.startsWith("@@");
						boolean flag2 = tmp.startsWith("diff --git");
						while (!(flag1) && !(flag2)) {
							if (tmp.startsWith("-")) {
								lineNums.add(startLine);
							}
							if (!tmp.startsWith("+")) {
								startLine ++;
							}
							tmp = r.readLine();
							if (tmp == null)
								break;
							flag1 = tmp.startsWith("@@");
							flag2 = tmp.startsWith("diff --git");
						}
						if (lineNums.size() == 0) {
							System.out.println(filePath);
							continue;
						}
						int head = 1;
						int s = lineNums.get(0);
						while (head < lineNums.size()) {
							if (lineNums.get(head) != lineNums.get(head-1) + 1) {
								lines.put(s, lineNums.get(head-1));
								s = lineNums.get(head);
							}
							head ++;
						}
						lines.put(s, lineNums.get(head-1));
					}
					tmp = r.readLine();
				}
			}
			else {
				tmp = r.readLine();
			}
		}
		r.close();
		return ret;
	}

}
