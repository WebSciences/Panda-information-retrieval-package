package uk.ac.ucl.panda.reranking;

/**
 * @author Yiwei Chen
 */

import uk.ac.ucl.panda.retrieval.models.ModelParser;

public class MMRReranker implements Reranker {

	double lambda = 0.5d;
	
	@Override
	public void Rerank(String index, String topics, String qrels, String var,
			Class modelType) {
		
		// get the underlying scoring model instance
		ModelParser model = null;
		try{
			model = (ModelParser)modelType.newInstance();
		} catch(Exception e) { }
	}

	@Override
	public void Rerank(String index, String topics, String qrels, String var,
			Class modelType, double a) {
		// TODO Auto-generated method stub
		lambda = a;
		Rerank(index, topics, qrels, var, modelType);
	}
	

}
