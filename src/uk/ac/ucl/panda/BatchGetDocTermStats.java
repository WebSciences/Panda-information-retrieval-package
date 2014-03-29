package uk.ac.ucl.panda;

/**
 * @author Yiwei Chen
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import uk.ac.ucl.panda.indexing.io.IndexReader;
import uk.ac.ucl.panda.retrieval.Searcher;
import uk.ac.ucl.panda.utility.io.DocNameExtractor;
import uk.ac.ucl.panda.utility.structure.TermFreqVector;

public class BatchGetDocTermStats extends GetDocTermStats {
	
	private ArrayList<Integer> totalDocWords;

	public BatchGetDocTermStats(String index) throws IOException,
			ClassNotFoundException {
		super(index);
	}
	public BatchGetDocTermStats() throws ClassNotFoundException, IOException {
		super();
	}
	
	public ArrayList<HashMap<String, Integer>> GetDocLevelStats(HashSet<String> docIDs)
			throws IOException, ClassNotFoundException {
		IndexReader rdr = IndexReader.open(cindex);
		Searcher search = new Searcher(cindex);
		DocNameExtractor xt = new DocNameExtractor("docname");
		ArrayList<HashMap<String, Integer>> results = new ArrayList<HashMap<String, Integer>>();
		for (int j = 0; j < rdr.maxDoc(); j++) {
			String docName = xt.docName(search, j);
			if (docIDs.contains(docName)) {	
				HashMap<String, Integer> termstats = new HashMap<String, Integer>();
				int docid = j;
				if (rdr.isDeleted(docid)) {
					return null;
				}
				TermFreqVector tTerms = null;
				TermFreqVector bTerms = null;
				tTerms = rdr.getTermFreqVector(docid, docDataField1);
				bTerms = rdr.getTermFreqVector(docid, docDataField2);
				if (tTerms != null) {
					if (type == true) {
						String Atterms[] = tTerms.getTerms();
						int AtFreq[] = tTerms.getTermFrequencies();
						for (int i = 0; i < Atterms.length; i++) {
							String id = Atterms[i];
							termstats.put(id, AtFreq[i]);
						}
					}
				}
				if (bTerms != null) {
					if (type == true) {
						String Abterms[] = bTerms.getTerms();
						int AbFreq[] = bTerms.getTermFrequencies();
						for (int i = 0; i < Abterms.length; i++) {
							String id = Abterms[i];
							if (termstats.containsKey(id)) {
								// int updateScore = (Integer) ( (Integer)
								// termstats.get(id) + AbFreq[i]);
								termstats
										.put(id, (Integer) ((Integer) termstats
												.get(id) + AbFreq[i]));
							} else {
								// eprop.put(Abterms[i], AbFreq[i]);
								termstats.put(id, AbFreq[i]);
							}
						}
					}
				}
				results.add(termstats);
			}
		}
		return results;
	}
	
	public ArrayList<HashMap<String, Integer>> GetDocLevelStats(HashMap<String, Integer> docIDs)
			throws IOException, ClassNotFoundException {
		IndexReader rdr = IndexReader.open(cindex);
		Searcher search = new Searcher(cindex);
		DocNameExtractor xt = new DocNameExtractor("docname");
		totalDocWords = new ArrayList<Integer>();
		ArrayList<HashMap<String, Integer>> results = new ArrayList<HashMap<String, Integer>>();
		ArrayList<Integer> index = new ArrayList<Integer>();
		for (int j = 0; j < rdr.maxDoc(); j++) {
			String docName = xt.docName(search, j);
			int totalWords = 0;
			if (docIDs.containsKey(docName)) {	
				HashMap<String, Integer> termstats = new HashMap<String, Integer>();
				int docid = j;
				if (rdr.isDeleted(docid)) {
					return null;
				}
				TermFreqVector tTerms = null;
				TermFreqVector bTerms = null;
				tTerms = rdr.getTermFreqVector(docid, docDataField1);
				bTerms = rdr.getTermFreqVector(docid, docDataField2);
				if (tTerms != null) {
					if (type == true) {
						String Atterms[] = tTerms.getTerms();
						int AtFreq[] = tTerms.getTermFrequencies();
						for (int i = 0; i < Atterms.length; i++) {
							String id = Atterms[i];
							termstats.put(id, AtFreq[i]);
							totalWords += AtFreq[i];
						}
					}
				}
				if (bTerms != null) {
					if (type == true) {
						String Abterms[] = bTerms.getTerms();
						int AbFreq[] = bTerms.getTermFrequencies();
						for (int i = 0; i < Abterms.length; i++) {
							String id = Abterms[i];
							if (termstats.containsKey(id)) {
								// int updateScore = (Integer) ( (Integer)
								// termstats.get(id) + AbFreq[i]);
								termstats
										.put(id, (Integer) ((Integer) termstats
												.get(id) + AbFreq[i]));
								totalWords += AbFreq[i];
							} else {
								// eprop.put(Abterms[i], AbFreq[i]);
								termstats.put(id, AbFreq[i]);
								totalWords += AbFreq[i];
							}
						}
					}
				}
				results.add(termstats);
				index.add(docIDs.get(docName));
				totalDocWords.add(totalWords);
			}
		}
		ArrayList<HashMap<String, Integer>> tempVector = new ArrayList<HashMap<String, Integer>>();
		ArrayList<Integer> tempWords = new ArrayList<Integer>();
		tempVector = results;
		tempWords = totalDocWords;

		for(int i=0; i<results.size(); i++){
			results.add(index.get(i), tempVector.get(i));
			results.remove(index.get(i)+1);
			totalDocWords.add(index.get(i), tempWords.get(i));
			totalDocWords.remove(index.get(i)+1);			
		}
		
		return results;
	}
	
	public ArrayList<Integer> getTotalDocWords() {
		return totalDocWords;
	}
	public void setTotalDocWords(ArrayList<Integer> totalDocWords) {
		this.totalDocWords = totalDocWords;
	}
	
}
