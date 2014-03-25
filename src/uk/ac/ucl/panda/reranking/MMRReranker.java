package uk.ac.ucl.panda.reranking;

import uk.ac.ucl.panda.retrieval.models.ModelParser;

public class MMRReranker implements Reranker {

	@Override
	public void Rerank(String index, String topics, String qrels, String var,
			Class modelType) {
		
		// get the underlying scoring model instance
		ModelParser model = null;
		try{
			model = (ModelParser)modelType.newInstance();
		} catch(Exception e) { }
		
		
		
		
	}

}
