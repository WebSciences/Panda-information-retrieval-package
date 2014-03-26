package uk.ac.ucl.panda.reranking;

/**
 * @author Yiwei Chen
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import uk.ac.ucl.panda.GetDocTermStats;
import uk.ac.ucl.panda.map.ResultsList;
import uk.ac.ucl.panda.map.ResultsList.Result;
import uk.ac.ucl.panda.retrieval.TrecRetrieval;
import uk.ac.ucl.panda.retrieval.models.ModelParser;

public class MMRReranker implements Reranker {

	double lambda = 0.5d;
	ModelParser model = null;
	GetDocTermStats gtdcs = null;
	int maxRerank = Integer.MAX_VALUE;
	
	@Override
	public void Rerank(String index, String topics, String qrels, String var,
			Class modelType) throws Exception {
		if (gtdcs == null) gtdcs = new GetDocTermStats(index);
		// get the underlying scoring model instance
		try {
			model = (ModelParser)modelType.newInstance();
		} catch(Exception e) { }
		ResultsList results = TrecRetrieval.getResultsFromFile(var + "/results");
		FileOutputStream outputfile = new FileOutputStream(
				new File(var + "/results-reranked"));
	    PrintWriter scorelogger = new PrintWriter(outputfile, true);
	    
	    Integer[] topicIds = results.getTopics();
		for (int i = 0; i < topicIds.length; i++) {
			System.out.println("Reranking query " + String.valueOf(i + 1));
			ArrayList<Result> topicResults = results.getTopicResults(topicIds[i]);
			applyReranking(topicResults);
		}
		outputResults(results, scorelogger);
	}
	
	private double getDocSimilarity(String DocA, String DocB)
			throws ClassNotFoundException, IOException {
		double similarity = Math.random();
		// TODO: compute similarity
		//HashMap<String, Integer> tmsts = gtdcs.GetDocLevelStats(DocA);
		return similarity;
	}
	
	private void applyReranking(ArrayList<Result> results)
			throws ClassNotFoundException, IOException {
		ArrayList<Result> new_results = new ArrayList<Result>();
		while (results.size() > 0) { // re-select results
			if (new_results.size() == 0) {
				results.get(0).score *= lambda;
				new_results.add(results.get(0));
				results.remove(0);
				continue;
			}
			if (new_results.size() >= maxRerank) {
				results.get(0).score *= lambda;
				results.get(0).rank = new_results.size();
				new_results.add(results.get(0));
				results.remove(0);
				continue;
			}
			double max_score = -1.0;
			int max_index = -1;
			for (int i = 0; i < results.size(); i++) {
				String candidateDocId = results.get(i).docID;
				double max_similarity = 0;
				for (int j = 0; j < new_results.size(); j++) {
					String selectedDocId = new_results.get(j).docID;
					double similarity = getDocSimilarity(candidateDocId, selectedDocId);
					if (similarity > max_similarity) max_similarity = similarity;
				}
				double score = lambda * results.get(i).score
						- (1 - lambda) * max_similarity;
				if (score > max_score) {
					max_score = score;
					max_index = i;
				}
			}
			results.get(max_index).score = max_score;
			results.get(max_index).rank = new_results.size();
			new_results.add(results.get(max_index));
			results.remove(max_index);
		}
		results.addAll(new_results);	
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
			Class modelType, double a) throws Exception {
		lambda = a;
		Rerank(index, topics, qrels, var, modelType);
	}
	

}
