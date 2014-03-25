package uk.ac.ucl.panda.reranking;

/**
 * @author Yifei Rong
 * @author Yiwei Chen
 */

public class PortfolioReranker implements Reranker {
	
	double b = 0.5d;
	
	@Override
	public void Rerank(String index, String topics, String qrels, String var,
			Class modelType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void Rerank(String index, String topics, String qrels, String var,
			Class modelType, double a) {
		b = a;
		Rerank(index, topics, qrels, var, modelType);
	}
	
}
