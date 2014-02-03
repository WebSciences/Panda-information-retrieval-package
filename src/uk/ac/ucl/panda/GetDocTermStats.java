package uk.ac.ucl.panda;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import uk.ac.ucl.panda.indexing.io.IndexReader;
import uk.ac.ucl.panda.retrieval.Searcher;
import uk.ac.ucl.panda.utility.io.DocNameExtractor;
import uk.ac.ucl.panda.utility.io.FileReader;
import uk.ac.ucl.panda.utility.structure.TermFreqVector;

/**
 * This class is used to access the term frequencies found in specific documents
 */
public class GetDocTermStats {

	protected String docDataField1 = "title";
	protected String docDataField2 = "body";
	boolean type = true;
	int totalWords = 0;
	public String cindex;
	public IndexReader rdr;
	protected String fileseparator = System.getProperty("file.separator");
	private final static String PANDA_ETC = System.getProperty("panda.etc",
			"./etc/");

	/**
	 * Initialize the class, locates the document index automatically from the
	 * IndexDir.config file
	 * 
	 * @param index
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public GetDocTermStats() throws IOException, ClassNotFoundException {
		BufferedReader buf = FileReader.openFileReader(PANDA_ETC
				+ fileseparator + "IndexDir.config");
		String index = buf.readLine();

		buf.close();
		cindex = index;
		rdr = IndexReader.open(cindex);
	}

	/**
	 * Initialize the class using the given index location
	 * 
	 * @param index
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public GetDocTermStats(String index) throws IOException,
			ClassNotFoundException {
		cindex = index;
		rdr = IndexReader.open(cindex);
	}

	/**
	 * Locates the index of the docID and returns a HashMap containing each of
	 * the terms in the document with their frequencies.
	 * 
	 * @param docID
	 *            document ID
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public HashMap<String, Integer> GetDocLevelStats(String docID)
			throws IOException, ClassNotFoundException {
		IndexReader rdr = IndexReader.open(cindex);
		Searcher search = new Searcher(cindex);
		DocNameExtractor xt = new DocNameExtractor("docname");
		HashMap<String, Integer> termstats = new HashMap<String, Integer>();
		totalWords = 0;
		for (int j = 0; j < rdr.maxDoc(); j++) {
			String docName = xt.docName(search, j);
			// if (ent.containsKey(docName1) == true)
			if (docName.equals(docID) == true) {
				termstats.clear();
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
			}
		}
		return termstats;
	}

	public HashMap<String, Integer> GetDocLevelStats(int docid)
			throws IOException, ClassNotFoundException {
		// IndexReader rdr = IndexReader.open(cindex);
		HashMap<String, Integer> termstats = new HashMap<String, Integer>();
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
								.put(id,
										(Integer) ((Integer) termstats.get(id) + AbFreq[i]));
						totalWords += AbFreq[i];
					} else {
						// eprop.put(Abterms[i], AbFreq[i]);
						termstats.put(id, AbFreq[i]);
						totalWords += AbFreq[i];
					}
				}
			}
		}
		return termstats;
	}

}
