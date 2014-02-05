import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;


public class NBTrain {
	
	public static int MAX_WORDS_IN_MEMORY = 40000;
	public static int MAX_DOCS_IN_MEMORY = 1500;
	public static int MIN_COUNT_TO_REMAIN = 4;
	
	
	static ArrayList<String> tokenizeDoc(String cur_doc) {
        String[] words = cur_doc.split("\\s+");
        ArrayList<String> tokens = new ArrayList<String>();
        for (int i = 0; i < words.length; i++) {
        	words[i] = words[i].replaceAll("\\W", "");
        	if (words[i].length() > 0) {
        		tokens.add(words[i]);
        	}
        }
        return tokens;
	}
	
	
	public static void main(String[] args) {
		
		String[] existingLabels = {"ca","de","el","es","fr","ga","hr","hu","nl","pl","pt","ru","sl","tr"};
		
		//hashmap that links ClassName to array number of vectorOfDics
		HashMap<String,Integer> classPosition = new HashMap<String,Integer>();
		
		//total documents
		int totalDocCount = 0;
		int[] totalLabelDocs = new int[14];
		
		//total words
		int[] totalLabelWords = new int[14];
		
		//populate structures
		int assignedPos=0;
		for(String l : existingLabels){
			classPosition.put(l, assignedPos);
			assignedPos++;
		}
		
		// +--------------------------------------------------------
		// |     READING AND COUNTING (TRAINING)
		// |--------------------------------------------------------
		// |  To increase performance, we won't keep track
		// |  of Y=*  or any Y=y|W=* 
		// | we will only count them once data is consolidated
		// +--------------------------------------------------------
		
		//long startTime = System.currentTimeMillis(); 
		try {
			
			//String inputPath = args[0];
	        //BufferedReader br = new BufferedReader(new FileReader(inputPath));
	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        
	        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	        
	        String line = br.readLine();
	        
			while (line != null) {
				
				//read labels and words
				String[] labelsAndTokens = line.split("\\t",2);
				String[] labels = labelsAndTokens[0].split(",");
				ArrayList<String> tokens = tokenizeDoc(labelsAndTokens[1]);				
				
				//for each label
				for(String label : labels){
					
					int labelPos = classPosition.get(label);
					
					//count of docs per label
					totalLabelDocs[labelPos]++;
					//count of words per label
					totalLabelWords[labelPos]++;
					
					for(String token : tokens){
						bw.append("Y="+label+",W="+token+"\t1\n");
					}
					
				}//end for label
				
				line = br.readLine();	
			}
			
			bw.flush();
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//print label counters
		for(Entry<String,Integer> e : classPosition.entrySet()){
			String label = e.getKey();
			int labelPos = e.getValue();
			
			//Y=y, W=*
			int totalWordsInLabel = totalLabelWords[labelPos];
			System.out.println("Y="+label+",W=*\t"+totalWordsInLabel);
			//Y=y
			int totalDocsInLabel = totalLabelDocs[labelPos];
			System.out.println("Y="+label+"\t"+totalDocsInLabel);
			totalDocCount += totalDocsInLabel;
		}

		//total doc counter Y=*
		System.out.print("Y=*\t"+totalDocCount);
		
		//long estimatedTime = System.currentTimeMillis() - startTime;
		//System.out.println("Training Time: "+estimatedTime);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main3(String[] args) {
		
		String[] existingLabels = {"ca","de","el","es","fr","ga","hr","hu","nl","pl","pt","ru","sl","tr"};
		
		//vector of dictionaries: one hashmap per class
		ArrayList<HashMap<String,Integer>> vectorOfDics= new ArrayList<HashMap<String,Integer>>(); 
		//hashmap that links ClassName to array number of vectorOfDics
		HashMap<String,Integer> classPosition = new HashMap<String,Integer>();
		
		//dic for current label
		HashMap<String,Integer> currentDic;
		
		//total documents
		int totalDocCount = 0;
		ArrayList<Integer> totalLabelDocs = new ArrayList<Integer>();
		
		//total words
		ArrayList<Integer> totalLabelWords = new ArrayList<Integer>();
		
		//populate structures
		int assignedPos=0;
		for(String l : existingLabels){
			classPosition.put(l, assignedPos);
			vectorOfDics.add(new HashMap<String,Integer>());
			totalLabelDocs.add(assignedPos,0);
			totalLabelWords.add(assignedPos,0);
			assignedPos++;
		}
		
		//variables for limiting memory usage
		int wordsInMemory = 0;
		int docsInMemory = 0;
		
		// +--------------------------------------------------------
		// |     READING AND COUNTING (TRAINING)
		// |--------------------------------------------------------
		// |  To increase performance, we won't keep track
		// |  of Y=*  or any Y=y|W=* 
		// | we will only count them once data is consolidated
		// +--------------------------------------------------------
		
		//long startTime = System.currentTimeMillis(); 
		try {
			
			//String inputPath = args[0];
	        //BufferedReader br = new BufferedReader(new FileReader(inputPath));
	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        String line = br.readLine();
	        
			while (line != null) {
				
				//read labels and words
				String[] labelsAndTokens = line.split("\\t",2);
				String[] labels = labelsAndTokens[0].split(",");
				ArrayList<String> tokens = tokenizeDoc(labelsAndTokens[1]);				
				
				//for each label
				for(String label : labels){
					
					//count of docs per label
					int labelPos = classPosition.get(label);
					totalLabelDocs.set(labelPos, totalLabelDocs.get(labelPos) + 1);
					
					//get dic for current label
					currentDic = vectorOfDics.get(classPosition.get(label));
					
					//for each token, count
					for(String token : tokens){
						if(!currentDic.containsKey(token)){
							currentDic.put(token, 1);
							//wordsInMemory++;
						}
						else{
							currentDic.put(token, currentDic.get(token)+1);
						}
					}
					
					docsInMemory++;
					
				}//end for label
				
				
				//Dump to disk if memory usage is too large
				//if(wordsInMemory > MAX_WORDS_IN_MEMORY){
				if(docsInMemory > MAX_DOCS_IN_MEMORY){
					
					//for each dictionary
					for(Entry<String, Integer> dic_e : classPosition.entrySet()){
						
						String label = dic_e.getKey();
						int pos = dic_e.getValue();
						currentDic = vectorOfDics.get(pos);
						
						//for each label
						for(Entry<String,Integer> e : currentDic.entrySet()){
							
							String token = e.getKey();
							int tokenCount = e.getValue();
							
							//output to disk
							System.out.println("Y="+label+",W="+token+"\t"+tokenCount);
							//update word counter
							totalLabelWords.set(pos, totalLabelWords.get(pos) + tokenCount);
							
						}
						vectorOfDics.get(pos).clear();
					}	
					
					//wordsInMemory = 0;
					docsInMemory = 0;
					
				}//end of dumping
				
				
				
				
				
				/*
				//Dump to disk if memory usage is too large
				while(wordsInMemory > MAX_WORDS_IN_MEMORY){
					
					//System.out.println("words in memory:"+wordsInMemory);
					
					//for each dictionary
					for(Entry<String, Integer> dic_e : classPosition.entrySet()){
						
						String label = dic_e.getKey();
						int pos = dic_e.getValue();
						currentDic = vectorOfDics.elementAt(pos);
						HashMap<String,Integer> remainingDic = new HashMap<String,Integer>();  
						
						//for each label
						for(Entry<String,Integer> e : currentDic.entrySet()){
							
							String token = e.getKey();
							int tokenCount = e.getValue();
							if(tokenCount < MIN_COUNT_TO_REMAIN){
								
								//output to disk
								System.out.println("Y="+label+",W="+token+"\t"+tokenCount);
								//update word counter
								totalLabelWords.set(pos, totalLabelWords.get(pos) + tokenCount);

								wordsInMemory--;
							}
							else{
								remainingDic.put(token, tokenCount);
							}
							
						}
						vectorOfDics.remove(pos);
						vectorOfDics.add(pos, remainingDic);
					}	
					
					//System.out.println("words in memory:"+wordsInMemory+"\n----------------");
					
					if(wordsInMemory > MAX_WORDS_IN_MEMORY){
						MIN_COUNT_TO_REMAIN+=1;
					}
					
				}//end of dumping
				*/
				
				
				
				line = br.readLine();	
			}
			
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//Dump remaining words
		for(Entry<String, Integer> dic_e : classPosition.entrySet()){
			
			String label = dic_e.getKey();
			int pos = dic_e.getValue();
			currentDic = vectorOfDics.get(classPosition.get(label));
			
			//for each label
			Set<Entry<String,Integer>> allEntries = currentDic.entrySet(); 
			for(Entry<String,Integer> e : allEntries){
			    int tokenCount = e.getValue();
				String token = e.getKey();
				//output to disk
				System.out.println("Y="+label+",W="+token+"\t"+tokenCount);
				//update word counter
				totalLabelWords.set(pos, totalLabelWords.get(pos) + tokenCount);
			}
			
			vectorOfDics.get(pos).clear();
		}	
		
		
		//print label counters
		for(Entry<String,Integer> e : classPosition.entrySet()){
			String label = e.getKey();
			int labelPos = e.getValue();
			
			//Y=y, W=*
			int totalWordsInLabel = totalLabelWords.get(labelPos);
			System.out.println("Y="+label+",W=*\t"+totalWordsInLabel);
			//Y=y
			int totalDocsInLabel = totalLabelDocs.get(labelPos);
			System.out.println("Y="+label+"\t"+totalDocsInLabel);
			totalDocCount += totalDocsInLabel;
		}

		//total doc counter Y=*
		System.out.print("Y=*\t"+totalDocCount);
		
		
		//System.out.println("MIN_COUNT_TO_REMAIN: "+MIN_COUNT_TO_REMAIN);
		
		//long estimatedTime = System.currentTimeMillis() - startTime;
		//System.out.println("Training Time: "+estimatedTime);
		
	}
	




	//--------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	public static void main2(String[] args) {
		
		//vector of dictionaries: one hashmap per class
		Vector<HashMap<String,Integer>> vectorOfDics= new Vector<HashMap<String,Integer>>(); 
		//hashmap that links ClassName to array number of vectorOfDics
		HashMap<String,Integer> classPosition = new HashMap<String,Integer>();
		
		//dic for current label
		HashMap<String,Integer> currentDic;
		int totalSeenClasses = 0;
		
		//total documents
		int totalDocCount = 0;
		ArrayList<Integer> totalLabelDocs = new ArrayList<Integer>();
		
		//total words
		ArrayList<Integer> totalLabelWords = new ArrayList<Integer>();
		
		//variables for limiting memory usage
		int wordsInMemory = 0;
		
		// +--------------------------------------------------------
		// |     READING AND COUNTING (TRAINING)
		// |--------------------------------------------------------
		// |  To increase performance, we won't keep track
		// |  of Y=*  or any Y=y|W=* 
		// | we will only count them once data is consolidated
		// +--------------------------------------------------------
		
		//long startTime = System.currentTimeMillis(); 
		try {
			
			//String inputPath = args[0];
	        //BufferedReader br = new BufferedReader(new FileReader(inputPath));
	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        String line = br.readLine();
	        
			while (line != null) {
				
				//read labels and words
				String[] labelsAndTokens = line.split("\\t",2);
				String[] labels = labelsAndTokens[0].split(",");
				ArrayList<String> tokens = tokenizeDoc(labelsAndTokens[1]);				
				
				
				
				//for each label
				for(String label : labels){
					
					//if label was previously unseen
					if(!classPosition.containsKey(label)){
						int assignedPos = totalSeenClasses;
						totalSeenClasses++;
						classPosition.put(label, assignedPos);
						vectorOfDics.add(new HashMap<String,Integer>());
						totalLabelDocs.add(assignedPos,0);
						totalLabelWords.add(assignedPos,0);
					}
					
					//count of docs per label
					int labelPos = classPosition.get(label);
					totalLabelDocs.set(labelPos, totalLabelDocs.get(labelPos) + 1);
					
					//get dic for current label
					currentDic = vectorOfDics.elementAt(classPosition.get(label));
					
					//for each token, count
					for(String token : tokens){
						if(!currentDic.containsKey(token)){
							currentDic.put(token, 1);
							wordsInMemory++;
						}
						else{
							currentDic.put(token, currentDic.get(token)+1);
						}
					}
					
				}//end for label
				
				
				
				
				
				//Dump to disk if memory usage is too large
				if(wordsInMemory > MAX_WORDS_IN_MEMORY){
					
					
					//for each dictionary
					for(Entry<String, Integer> dic_e : classPosition.entrySet()){
						
						String label = dic_e.getKey();
						int pos = dic_e.getValue();
						currentDic = vectorOfDics.elementAt(pos);
						
						//for each label
						for(Entry<String,Integer> e : currentDic.entrySet()){
							
							String token = e.getKey();
							int tokenCount = e.getValue();
							
							//output to disk
							System.out.println("Y="+label+",W="+token+"\t"+tokenCount);
							//update word counter
							totalLabelWords.set(pos, totalLabelWords.get(pos) + tokenCount);
							
						}
						vectorOfDics.get(pos).clear();
					}	
					
					wordsInMemory = 0;
					
				}//end of dumping
				
				
				
				
				
				/*
				//Dump to disk if memory usage is too large
				while(wordsInMemory > MAX_WORDS_IN_MEMORY){
					
					//System.out.println("words in memory:"+wordsInMemory);
					
					//for each dictionary
					for(Entry<String, Integer> dic_e : classPosition.entrySet()){
						
						String label = dic_e.getKey();
						int pos = dic_e.getValue();
						currentDic = vectorOfDics.elementAt(pos);
						HashMap<String,Integer> remainingDic = new HashMap<String,Integer>();  
						
						//for each label
						for(Entry<String,Integer> e : currentDic.entrySet()){
							
							String token = e.getKey();
							int tokenCount = e.getValue();
							if(tokenCount < MIN_COUNT_TO_REMAIN){
								
								//output to disk
								System.out.println("Y="+label+",W="+token+"\t"+tokenCount);
								//update word counter
								totalLabelWords.set(pos, totalLabelWords.get(pos) + tokenCount);

								wordsInMemory--;
							}
							else{
								remainingDic.put(token, tokenCount);
							}
							
						}
						vectorOfDics.remove(pos);
						vectorOfDics.add(pos, remainingDic);
					}	
					
					//System.out.println("words in memory:"+wordsInMemory+"\n----------------");
					
					if(wordsInMemory > MAX_WORDS_IN_MEMORY){
						MIN_COUNT_TO_REMAIN+=1;
					}
					
				}//end of dumping
				*/
				
				
				
				line = br.readLine();	
			}
			
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//Dump remaining words
		for(Entry<String, Integer> dic_e : classPosition.entrySet()){
			
			String label = dic_e.getKey();
			int pos = dic_e.getValue();
			currentDic = vectorOfDics.elementAt(classPosition.get(label));
			
			//for each label
			Set<Entry<String,Integer>> allEntries = currentDic.entrySet(); 
			for(Entry<String,Integer> e : allEntries){
			    int tokenCount = e.getValue();
				String token = e.getKey();
				//output to disk
				System.out.println("Y="+label+",W="+token+"\t"+tokenCount);
				//update word counter
				totalLabelWords.set(pos, totalLabelWords.get(pos) + tokenCount);
			}
			
			vectorOfDics.elementAt(pos).clear();
		}	
		
		
		//print label counters
		for(Entry<String,Integer> e : classPosition.entrySet()){
			String label = e.getKey();
			int labelPos = e.getValue();
			
			//Y=y, W=*
			int totalWordsInLabel = totalLabelWords.get(labelPos);
			System.out.println("Y="+label+",W=*\t"+totalWordsInLabel);
			//Y=y
			int totalDocsInLabel = totalLabelDocs.get(labelPos);
			System.out.println("Y="+label+"\t"+totalDocsInLabel);
			totalDocCount += totalDocsInLabel;
		}

		//total doc counter Y=*
		System.out.print("Y=*\t"+totalDocCount);
		
		
		//System.out.println("MIN_COUNT_TO_REMAIN: "+MIN_COUNT_TO_REMAIN);
		
		//long estimatedTime = System.currentTimeMillis() - startTime;
		//System.out.println("Training Time: "+estimatedTime);
		
	}

}
