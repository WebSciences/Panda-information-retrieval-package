package uk.ac.ucl.panda.reranking;

/**
 * @author Yiwei Chen
 */

public interface Reranker {
	
	/**
	 * This function should read the original retrieval result from file named
	 * 'var/results', and produce a new reranked list using some diversity model.
	 * The reranked list should be stored in file named 'var/results-reranked'.
	 * 
	 * @param index        --The path of index files 
	 * @param topics       --The path of topic files
	 * @param qrels        --The path of qrel files
	 * @param var          --The path of 'var' directory
	 * @param modelType    --The type of underlying retrieval model
	 */
	public void Rerank(String index, String topics, String qrels, String var, Class modelType);
	
	/**
	 * Specifying a reranking parameter
	 * 
	 * @param a		the reranking parameter (e.g. lambda or b)
	 */
	public void Rerank(String index, String topics, String qrels, String var, Class modelType, double a);
}
