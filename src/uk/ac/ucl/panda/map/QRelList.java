package uk.ac.ucl.panda.map;

import java.util.HashMap;

/**
 * This class represents the qrels file, containing a list of each topic mapped
 * to another list containing the documents and their judgements.
 * 
 * @author Marc Sloan
 * 
 */
public class QRelList {

	/**
	 * This HashMap maps topics to another HashMap which maps document IDs to
	 * their judgement values
	 */
	private HashMap<Integer, HashMap<String, Boolean>> qrels;

	/**
	 * Initializes the list
	 */
	public QRelList() {
		qrels = new HashMap<Integer, HashMap<String, Boolean>>();
	}

	/**
	 * Add a new qrel to the list
	 * 
	 * @param topicNum
	 *            Topic number of this qrel
	 * @param docID
	 *            Document ID for this qrel
	 * @param relevant
	 *            true for relevant
	 */
	public void addQRel(int topicNum, String docID, boolean relevant) {
		// check if the topic number already exists in the list
		HashMap<String, Boolean> qrelList = (qrels.containsKey(topicNum)) ? qrels
				.get(topicNum) : new HashMap<String, Boolean>();
		// add the new qrel
		qrelList.put(docID, relevant);
		qrels.put(topicNum, qrelList);
	}

	/**
	 * Returns a HashMap of the documents that have been judged for the given
	 * topic number.
	 * 
	 * <b>Note:</b> If this map doesn't contain the documentID, then by default
	 * the relevance is false
	 * 
	 * @param topicNum
	 *            The topic number of the judged documents
	 * @return
	 */
	public HashMap<String, Boolean> getTopicQRels(int topicNum) {
		return qrels.get(topicNum);
	}

	/**
	 * Returns an array of topic numbers stored in this list
	 * 
	 * @return Integer array of topic numbers
	 */
	public Integer[] getTopics() {
		Integer[] topics = new Integer[qrels.keySet().size()];
		qrels.keySet().toArray(topics);
		return topics;
	}
}
