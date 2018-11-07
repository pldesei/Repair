/**
 * use word2vector and generate candidates
 * @author: GZ
 * date: 2018/11/11
 */
package cofix.core.parser.search;

import japa.parser.ASTParserTokenManager;
import japa.parser.JavaCharStream;
import japa.parser.Token;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import cofix.common.util.Pair;


public class W2VFilter {
	
	String filePath;
	int line;
	String projectName;
	String vectorBasePath = "/root/word2vec/defects4jData/";
	String vectorFilePath;
	ArrayList<String> tokens = new ArrayList<String>();
	
	int MAX_RESULT = 40;
	double MIN_THRESHOLD = 0.5;
    HashMap<String, Vector<Double>> words = new HashMap<String, Vector<Double>>();
    
    public static void main(String[] args) throws IOException {
    	W2VFilter w2v = new W2VFilter("/root/BugRepair/defects4j/buggyVersions/math/math_22_buggy/src/main/java/org/apache/commons/math3/distribution/FDistribution.java", 275, "math");
    	w2v.generateCandidates();
    }
	
	public W2VFilter(String filePath, int line, String projectName) throws IOException{
		System.out.println("FilePath:" + filePath + " Line:" + line + " Project:" + projectName );
		this.filePath = filePath;
		this.line = line;
		this.projectName = projectName;
		this.vectorFilePath = this.vectorBasePath + projectName.toLowerCase() + "Vectors.txt";
		generateTokens();
		readVectors();
	}
	
	private void readVectors() throws IOException{
		BufferedReader r = new BufferedReader(new FileReader(new File(vectorFilePath)));
        //int vocabularySize = Integer.parseInt(r.readLine());
        //int vectorSize = Integer.parseInt(r.readLine());
        String tmp = "";
        
        while ((tmp = r.readLine()) != null) {
            String[] opt = tmp.split(" ");
            if (opt == null || opt.length == 0)
                continue;
            String word = opt[0];
            double len = 0;
            Vector<Double> vec = new Vector<Double>();
            for (int i = 1; i < opt.length; i++) {
                vec.add(Double.parseDouble(opt[i]));
                len += vec.get(i - 1) * vec.get(i - 1);
            }
            len = Math.sqrt(len);
            for (int i = 0; i < vec.size(); i++)
                vec.set(i, vec.get(i) / len);
            words.put(word, vec);
        }
        r.close();
	}
	
	private void generateTokens() {
		File file = new File(filePath);
		try {
			FileInputStream in = new FileInputStream(file);
			ASTParserTokenManager tokenmgr = new ASTParserTokenManager(new JavaCharStream(in));
			Token token = tokenmgr.getNextToken(); 
			while (token != null && !token.image.equals("")) {
				if (token.beginLine == line) {
					//System.out.println(token.beginLine + ":" + token.image);
					tokens.add(token.image);
				}
				token = tokenmgr.getNextToken();
			}
			System.out.println(tokens);
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		catch (Error e) {
			System.out.println(file.getAbsolutePath());
			System.out.println(e.getMessage());
		}
	}
	
	private Vector<Pair<String, Double>> getSimilarWords(String word) {
        Vector<Double> vec = words.get(word);
        if (vec == null) {
            System.out.println(word + ": Out of Vocabulary!");
            return null;
        }
        double len = 0;
        for (int i = 0; i < vec.size(); i++)
            len += vec.get(i) * vec.get(i);
        len = Math.sqrt(len);
        for (int i = 0; i < vec.size(); i++)
            vec.set(i, vec.get(i) / len);
        Vector<Pair<String, Double>> result = new Vector<Pair<String, Double>>();
        
        for (String key : words.keySet()) {
            if (!key.equals(word)) {
                Vector<Double> tmp = words.get(key);
                double cosine = 0;
                for (int i = 0; i < tmp.size(); i++) {
                    cosine += tmp.get(i) * vec.get(i);
                }
                if (cosine < MIN_THRESHOLD)
                	continue;
                Pair<String, Double> ret = new Pair<String, Double>(key, cosine);
                int cur = result.size() - 1;
                while (cur >= 0 && result.get(cur).getSecond() < cosine) cur --;
                result.add(cur + 1, ret);
                if (result.size() > MAX_RESULT) result.remove(result.size() - 1);
            }
        }
        //for (int i = 0; i < result.size(); i++)
        //    System.out.printf("%s\t\t%.6f\n", result.get(i).getFirst(), result.get(i).getSecond());
        return result;
    }
	
	public List<String> generateCandidates() {
		List<String> ret = new ArrayList<>();
		for (int index = 0; index < tokens.size(); index++) {
			String token = tokens.get(index);
			Vector<Pair<String, Double>> similarWords = getSimilarWords(token);
			if (similarWords == null)
				continue;
			for(int i = 0; i < similarWords.size(); i++) {
				String similarWord = similarWords.get(i).getFirst();
				ret.add(generatePatch(index, similarWord));
				//System.out.println(ret.get(ret.size()-1));
			}
		}
		
		return ret;
	}
	
	private String generatePatch(int which, String replace) {
		String ret = "";
		for (int i = 0; i < tokens.size(); i++) {
			if (i == which) {
				ret += replace + " ";
			}
			else {
				ret += tokens.get(i) + " ";
			}
		}
		return ret;
	}

}
