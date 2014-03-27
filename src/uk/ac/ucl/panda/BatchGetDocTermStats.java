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
		setTotalWords(0);
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
							setTotalWords(getTotalWords() + AtFreq[i]);
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
								setTotalWords(getTotalWords()
										+ AbFreq[i]);
							} else {
								// eprop.put(Abterms[i], AbFreq[i]);
								termstats.put(id, AbFreq[i]);
								setTotalWords(getTotalWords()
										+ AbFreq[i]);
							}
						}
					}
				}
				results.add(termstats);
			}
		}
		return results;
	}
	
}
