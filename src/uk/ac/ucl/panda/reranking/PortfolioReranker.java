package uk.ac.ucl.panda.reranking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import uk.ac.ucl.panda.BatchGetDocTermStats;
import uk.ac.ucl.panda.GetDocTermStats;
import uk.ac.ucl.panda.map.ResultsList;
import uk.ac.ucl.panda.map.ResultsList.Result;
import uk.ac.ucl.panda.retrieval.TrecRetrieval;
import uk.ac.ucl.panda.retrieval.models.BM25;
import uk.ac.ucl.panda.retrieval.models.Language_lambda;
import uk.ac.ucl.panda.retrieval.models.Language_u;

/**
 * @author Yifei Rong
 * @author Yiwei Chen
 */

public class PortfolioReranker implements Reranker {
	
	double b = 1.0d;
	GetDocTermStats gtdcs = null;
	BatchGetDocTermStats bgtdcs = null;
	String inputFile = null;
	String outputFile = null;
	ResultsList results = null;

	
	@Override
	public void Rerank(String index, String topics, String qrels, String var,
			Class modelType) throws Exception{
		
		if (gtdcs == null) 
			gtdcs = new GetDocTermStats(index);
		if (bgtdcs == null) 
			bgtdcs = new BatchGetDocTermStats(index);
		
		inputFile = var + "/results";
		outputFile = var + "/results-reranked";
	
		results = TrecRetrieval.getResultsFromFile(inputFile);
		
        File oldFile = new File(outputFile);
        if(oldFile.exists()){
            oldFile.delete();
        }            
		File newFile = new File(outputFile);		
		FileOutputStream outputfile = new FileOutputStream(newFile);
        PrintWriter scorelogger = new PrintWriter(outputfile, true);

        Integer[] topicIds = results.getTopics();
 
        for (int i = 0; i < topicIds.length; i++) {
            System.out.println("Reranking query " + String.valueOf(i + 1));
            ArrayList<Result> topicResults = results.getTopicResults(topicIds[i]);
            applyReranking(topicResults, calCovarianceMatrix(topicResults, modelType), modelType);
        }
        outputResults(results, scorelogger); 
        scorelogger.close();
        outputfile.close();
	}

	private void applyReranking(ArrayList<Result> topicResults, HashMap<String, Double> covarMatrics, Class modelType) {
		ArrayList<Result> newResults = new ArrayList<Result>();
		ArrayList<Result> tempResults = new ArrayList<Result>();
		
		for(int i=0; i<topicResults.size(); i++){
			tempResults.add(topicResults.get(i));
		}
		
		newResults.add(topicResults.get(0));
		topicResults.remove(0);
	    
	    for(int k=1; k<tempResults.size(); k++){   	
	    	double[] increase = new double[topicResults.size()];
	    	
	    	for(int j=0; j<topicResults.size(); j++){
	    		double totalSecMoment = 0.d;
	    		
				for(int i=0; i<k; i++){						
					if(i == 0 || i == 1)
						totalSecMoment += covarMatrics.get(newResults.get(i).docID+topicResults.get(j).docID);
					else{
						double weight = Math.log(2) / (Math.log10(i+1) * Math.log(10)) / 124;
						totalSecMoment += covarMatrics.get(newResults.get(i).docID+topicResults.get(j).docID)*weight;
					}					 
				}	
	    		
				if(k == 1)
					increase[j] = topicResults.get(j).score - b*Math.abs(covarMatrics.get(topicResults.get(j).docID+topicResults.get(j).docID)) - 2*b*Math.abs(totalSecMoment);
				else{
					double weight = Math.log(2) / (Math.log10(k+1) * Math.log(10)) / 124;
					increase[j] = topicResults.get(j).score - b*Math.abs(covarMatrics.get(topicResults.get(j).docID+topicResults.get(j).docID))*weight - 2*b*Math.abs(totalSecMoment);
				}   		
	    	}
	    	
			double max = increase[0];
			int index = 0;
            for(int i=0; i<increase.length; i++){
            	if(increase[i] > max){
            		max = increase[i];
            		index = i;
            	}
            }
            
            topicResults.get(index).score = increase[index];
            topicResults.get(index).rank = k;
            newResults.add(topicResults.get(index));
            topicResults.remove(index);	    		
	    }      
	    
    	if(topicResults.size() == 0){
    		topicResults.addAll(newResults);
    	}
	}
	
	private HashMap<String, Double> calCovarianceMatrix(ArrayList<Result> topicResults, Class modelType) throws ClassNotFoundException, IOException{
//		double[][] covarianceMatrix = new double[topicResults.size()][topicResults.size()];
		HashMap<String, Double> covarMatrics = new HashMap<String, Double>();
		double covaiance = 0.0d;
		
		if(modelType.equals(Language_u.class) || modelType.equals(Language_lambda.class)){
			double scoreSum = 0.d;
			
			for(int i=0; i<topicResults.size(); i++)
				scoreSum += topicResults.get(i).score;
			
			for(int i=0; i<topicResults.size(); i++){
				for(int j=0; j<topicResults.size(); j++){
					if(i == j)
						covaiance = 100000000d* (-1) * topicResults.get(i).score * (scoreSum - topicResults.get(i).score) / ((scoreSum * scoreSum)*(scoreSum + 1));				    																
					else
						covaiance = 100000000d* (-1) * (topicResults.get(i).score * topicResults.get(j).score) / ((scoreSum * scoreSum)*(scoreSum + 1));
					
					covarMatrics.put(topicResults.get(i).docID+topicResults.get(j).docID, covaiance);
				}
			}
		}
		
		else if(modelType.equals(BM25.class)){
			double[] meanFreqs = new double[topicResults.size()];
			ArrayList<Integer> totalWords = new ArrayList<Integer>();
			ArrayList<HashMap<String, Integer>> resultStatistics = new ArrayList<HashMap<String, Integer>>();
			HashMap<String, Integer> totalTermVector = new HashMap<String, Integer>();
			
			HashMap<String, Integer> docIDs = new HashMap<String, Integer>();
			for (Result r : topicResults) {
				docIDs.put(r.docID, r.rank);
			}
			resultStatistics = bgtdcs.GetDocLevelStats(docIDs);
			totalWords = bgtdcs.getTotalDocWords();
			
			for(int i=0; i<resultStatistics.size(); i++){
				HashMap<String, Integer> tempVector = resultStatistics.get(i);
				meanFreqs[i] = 1.0d * totalWords.get(i)/tempVector.size();
				
				Iterator iterator = tempVector.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next().toString();
					totalTermVector.put(key, 1);	
				}
			}

			double[][] rawMatirx = new double[topicResults.size()][totalTermVector.size()];
			for(int i=0; i<topicResults.size(); i++){
				Iterator iterator = totalTermVector.keySet().iterator();
				int count = 0;
				while (iterator.hasNext()) {
					String key = iterator.next().toString();
					int termFreq;
					try{
						termFreq = resultStatistics.get(i).get(key);
					} catch (Exception e){
						termFreq = 0;
					}
					rawMatirx[i][count] = termFreq;
					count++;
				}		
			}
			
			for(int i=0; i<topicResults.size(); i++){
				for(int j=0; j<topicResults.size(); j++){
					double totalVariance = 0.d;
					for(int k=0; k<totalTermVector.size(); k++)
						totalVariance += (rawMatirx[i][k] - meanFreqs[i])*(rawMatirx[j][k] - meanFreqs[j]);
					
					covaiance = totalVariance/totalTermVector.size();
					covarMatrics.put(topicResults.get(i).docID+topicResults.get(j).docID, covaiance);
				}
			}			
		}
		
		else
			System.err.println("Unexpected model number specified!");
		
		System.err.println("covariance calculated!");
		return covarMatrics;
	}

	private void outputResults(ResultsList results, PrintWriter scorelogger) {
		Integer[] topics = results.getTopics();		 
		Arrays.sort(topics);
		for (int topicNum : topics) {
		    ArrayList<Result> topicResults = results.getTopicResults(topicNum);
		    for (Result r : topicResults) {
		        scorelogger.println(String.valueOf(topicNum) +'\t' + "Q0" + '\t'
		                  + r.docID + '\t' + r.rank + '\t' + r.score + '\t' + "test");
		    }
		}	
	}

	@Override
	public void Rerank(String index, String topics, String qrels, String var,
			Class modelType, double a) throws Exception{
		b = a;
		Rerank(index, topics, qrels, var, modelType);
	}
	
}
