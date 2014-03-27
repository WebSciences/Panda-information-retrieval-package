package uk.ac.ucl.panda.reranking;

/**
 * @author Yiwei Chen
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import uk.ac.ucl.panda.BatchGetDocTermStats;
import uk.ac.ucl.panda.map.ResultsList;
import uk.ac.ucl.panda.map.ResultsList.Result;
import uk.ac.ucl.panda.retrieval.TrecRetrieval;
import uk.ac.ucl.panda.retrieval.models.Model;

public class MMRReranker implements Reranker {

	double lambda = 0.5d;
	Model model = null;
	int maxRerank = 10;
	
	ArrayList<Result> new_results = null;
	ArrayList<HashMap<String, Integer>> tmsts = null;
	ArrayList<HashMap<String, Integer>> new_tmsts = null;
	BatchGetDocTermStats bgtdcs = null;
	ArrayList<Double> DLs = null;
	double totalDL = 0.0;
	
	@Override
	public void Rerank(String index, String topics, String qrels, String var,
			Class modelType) throws Exception {
		if (new_results == null) new_results = new ArrayList<Result>();
		if (new_tmsts == null) new_tmsts = new ArrayList<HashMap<String, Integer>>();
		if (bgtdcs == null) bgtdcs = new BatchGetDocTermStats(index);
		if (DLs == null) DLs = new ArrayList<Double>();
		// get the underlying scoring model instance
		try {
			model = (Model)modelType.newInstance();
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
		scorelogger.close();
		outputfile.close();
	}
	
	private double getDL(HashMap<String, Integer> tmsts) {
		double DL = 0.0;
		for (Entry<String, Integer> entry : tmsts.entrySet()) {
			DL += entry.getValue();
		}
		return DL;
	}
	
	private void applyReranking(ArrayList<Result> results)
			throws Exception {
		new_results.clear();
		new_tmsts.clear();
		DLs.clear();
		totalDL = 0.0;
		HashSet<String> docIDs = new HashSet<String>();
		for (Result r : results) {
			docIDs.add(r.docID);
		}
		tmsts = bgtdcs.GetDocLevelStats(docIDs);
		while (results.size() > 0) { // re-select results
			if (new_results.size() == 0) { // the first one
				new_results.add(results.get(0));
				new_tmsts.add(tmsts.get(0));
				DLs.add(getDL(tmsts.get(0)));
				totalDL += DLs.get(DLs.size() - 1);
				tmsts.remove(0);
				results.remove(0);
				continue;
			}
			if (new_results.size() >= maxRerank) { // limited to maxRerank
				results.get(0).rank = new_results.size();
				new_results.add(results.get(0));
				results.remove(0);
				continue;
			}
			double max_score = Integer.MIN_VALUE;
			int max_index = -1;
			for (int i = 0; i < results.size(); i++) {
				double max_similarity = 0.0;
				for (int j = 0; j < new_results.size(); j++) {
					double similarity = getDocSimilarity(i, j);
					if (similarity > max_similarity) max_similarity = similarity;
				}
				double score = lambda * results.get(i).score
						- (1 - lambda) * max_similarity;
				if (score > max_score) {
					max_score = score;
					max_index = i;
				}
			}
			results.get(max_index).rank = new_results.size();
			new_results.add(results.get(max_index));
			new_tmsts.add(tmsts.get(max_index));
			DLs.add(getDL(tmsts.get(max_index)));
			totalDL += DLs.get(DLs.size() - 1);
			tmsts.remove(max_index);
			results.remove(max_index);
		}
		results.addAll(new_results);	
	}
	
	private double getDocSimilarity(int indexA, int indexB) throws Exception {
		double similarity = 0.0;
		HashMap<String, Integer> tmstsA = tmsts.get(indexA);
		HashMap<String, Integer> tmstsB = new_tmsts.get(indexB);
		for (Entry<String, Integer> entry : tmstsA.entrySet()) {
			String term = entry.getKey();
			double log2toe = 1.0d / Math.log(2.0d);
			double tf = 0.0;
			if (tmstsB.containsKey(term)) tf = tmstsB.get(term);
			double df = 0.0;
			int CTF = 0;
			for (int i = 0; i < new_results.size(); i++) {
				if (new_tmsts.get(i).containsKey(term)) {
					df += 1.0;
					CTF += new_tmsts.get(i).get(term);
				}
			}
			int DocNum = new_results.size();
			double idf = Math.log(DocNum / df) * log2toe;
			double DL = DLs.get(indexB);
			double CL = new_tmsts.size();
			double avgDL = totalDL / CL;
			int qTF = tmstsA.get(term);
			if (tf == 0.0) continue; // does not appear in the document
			similarity += model.getscore(tf, df, idf, DL, avgDL, DocNum, CL, CTF, qTF);
		}
		return similarity;
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
