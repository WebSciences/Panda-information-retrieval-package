package uk.ac.ucl.panda.map;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents the results file, containing a list of each topic
 * mapped to another list containing the ranked documents for that topic.
 * 
 * @author Marc Sloan
 * 
 */
public class ResultsList {
	/**
	 * HashMap mapping each topic to an ArrayList of documents that are the
	 * ranking for that topic
	 */
	private HashMap<Integer, ArrayList<Result>> results;

	/**
	 * Initializes the list
	 */
	public ResultsList() {
		results = new HashMap<Integer, ArrayList<Result>>();
	}

	/**
	 * Add a new result to the list
	 * 
	 * @param topicNum
	 *            Topic number of this ranked document
	 * @param docID
	 *            Document ID for this ranked document
	 * @param rank
	 *            the rank of this ranked document
	 * @param score
	 *            the score of this ranked document
	 */
	public void addResult(int topicNum, String docID, int rank, double score) {
		// check if the topic number already exists in the list
		ArrayList<Result> resultsList = (results.containsKey(topicNum)) ? results
				.get(topicNum) : new ArrayList<Result>();
		// add the new result
		resultsList.add(new Result(docID, rank, score));
		results.put(topicNum, resultsList);
	}

	/**
	 * Returns a ArrayList of the ranked documents for the given topic number.
	 * 
	 * @param topicNum
	 *            The topic number of the judged documents
	 * @return
	 * 
	 * @see Result
	 */
	public ArrayList<Result> getTopicResults(int topicNum) {
		return results.get(topicNum);
	}

	/**
	 * Returns an array of topic numbers stored in this list
	 * 
	 * @return Integer array of topic numbers
	 */
	public Integer[] getTopics() {
		Integer[] topics = new Integer[results.keySet().size()];
		results.keySet().toArray(topics);
		return topics;
	}

	/**
	 * This class is used to store the details of an individual ranked document
	 * 
	 * @author Marc Sloan
	 * 
	 */
	public class Result {
		/**
		 * Document ID of this ranked document
		 */
		public String docID;
		/**
		 * Search ranking of this ranked document
		 */
		public int rank;
		/**
		 * IR score of this ranked document
		 */
		public double score;

		/**
		 * Initialize the search result
		 * 
		 * @param docID
		 * @param rank
		 * @param score
		 */
		public Result(String docID, int rank, double score) {
			this.docID = docID;
			this.rank = rank;
			this.score = score;
		}
	}
}