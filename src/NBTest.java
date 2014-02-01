import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.Map.Entry;


public class NBTest {
	
	static Vector<String> tokenizeDoc(String cur_doc) {
        String[] words = cur_doc.split("\\s+");
        Vector<String> tokens = new Vector<String>();
        for (int i = 0; i < words.length; i++) {
        	words[i] = words[i].replaceAll("\\W", "");
        	if (words[i].length() > 0) {
        		tokens.add(words[i]);
        	}
        }
        return tokens;
	}

	public static void main(String[] args) {

		String testPath = args[0];
		
		//vector of dictionaries: one hashmap per class
		Vector<HashMap<String,Integer>> vectorOfDics= new Vector<HashMap<String,Integer>>(); 
		//hashmap that links ClassName to array number of vectorOfDics
		HashMap<String,Integer> classPosition = new HashMap<String,Integer>();
		//hashmap with label count
		HashMap<String,Integer> classCount = new HashMap<String,Integer>();
		//total doc count #(Y=*)
		int totalDocCount = 0;
		//doc count per label
		HashMap<String, Integer> labelDocCount = new HashMap<String, Integer>(); 
		//vocabulary
		HashSet<String> vocabulary = new HashSet<String>(); 
		
		
		//labels of interest
		String[] sL = new String[]{"CCAT","ECAT","GCAT","MCAT"};
		HashSet<String> selectedLabels = new HashSet<String>(Arrays.asList(sL));
		
		// +--------------------------------------------------------
		// |     READING THE COUNTS AND REBUILDING MAPS
		// +--------------------------------------------------------

		try {
	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			//BufferedReader br = new BufferedReader(new FileReader(args[1]));
	        String line = br.readLine(); 
			while (line != null) {
				
				String[] featureAndCount = line.split("\\t");
				int count = Integer.parseInt(featureAndCount[1]);
				
				//parse feature
				String[] yAndW = featureAndCount[0].split("[=,]");
				String label = yAndW[1];
				//Y=*
				if(label.equals("*")){ 
					totalDocCount = count;
				}
				//Y=y
				else if(yAndW.length==2){
					labelDocCount.put(label, count);
				}
				//Y=y,W=?
				else{
					String token = yAndW[3];
					vocabulary.add(token);
					if(!classPosition.containsKey(label)){
						int assignedPos = vectorOfDics.size();
						classPosition.put(label, assignedPos);
						vectorOfDics.add(new HashMap<String,Integer>());
					}
					//Y=y,W=*
					if(token.equals("*")){
						classCount.put(label, count);
					}
					//Y=y,W=w
					else{
						vectorOfDics.elementAt(classPosition.get(label)).put(token, count);
					}	
				}				
				line = br.readLine();	
			}
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		// +--------------------------------------------------------
		// |     NAIVE BAYES CLASSIFICATION
		// +--------------------------------------------------------

		//feedback parameters
		int totalPredictions = 0;
		int totalCorrectPredictions = 0;
		
		try {
	        BufferedReader br = new BufferedReader(new FileReader(testPath));
	        String line = br.readLine(); 
			while (line != null) {
			
				//read labels and words
				String[] labelsAndTokens = line.split("\\t",2);
				HashSet<String> trueLabels = new HashSet<String>(Arrays.asList(labelsAndTokens[0].split(",")));
				Vector<String> tokens = tokenizeDoc(labelsAndTokens[1]);
				
				
				//Smoothing parameters  Theta_i = (x_i + a) / (N + ad)
				double d = vocabulary.size();
				double a = 1;
				
				//best label data
				String bestLabel = "N/A";
				double bestLogLikelihood = Double.NEGATIVE_INFINITY;
				
				//compute likelihood for each label
				for (Entry<String, Integer> entry : classPosition.entrySet()) {
					
					String currentLabel = entry.getKey();
					int currentPos = entry.getValue();
					
					int classTotalWords = classCount.get(currentLabel);
					int classTotalDocs = labelDocCount.get(currentLabel);
					HashMap<String,Integer> currentDic = vectorOfDics.get(currentPos);
					
					//initialize with logP(Y=y)  or actually log( #(Y=y) )
					//OBS: no need to divide by #(Y=*) since it is constant for all labels
					//the correction will be done later, to optimize processing time
					double probability = - Math.log(classTotalDocs);
					
					for(String token : tokens){
						int tokenCount = currentDic.containsKey(token) ? currentDic.get(token) : 0;
						probability += Math.log((double)tokenCount + a) - Math.log((double)classTotalWords + a*d);
					}
					
					if(probability > bestLogLikelihood){
						bestLogLikelihood = probability;
						bestLabel = currentLabel;
					}
					
				}
				
				//adjust probability:  make  log( #(Y=y,W=*) ) become logP(Y=y,W=*)
				bestLogLikelihood += Math.log(totalDocCount);
				
				//output the best label
				System.out.println(bestLabel+"\t"+bestLogLikelihood);
				
				//feedback results
				totalPredictions ++;
				if(trueLabels.contains(bestLabel))
					totalCorrectPredictions++;
				
				//read next document
				line = br.readLine();
			}
			br.close();
			
			//print results
			//System.out.println("\nResults: "+totalCorrectPredictions+"/"+totalPredictions+" = "+((double)totalCorrectPredictions/(double)totalPredictions));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
