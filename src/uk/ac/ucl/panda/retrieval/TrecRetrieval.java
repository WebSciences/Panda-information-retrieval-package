package uk.ac.ucl.panda.retrieval;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import uk.ac.ucl.panda.applications.evaluation.trec.Judge;
import uk.ac.ucl.panda.applications.evaluation.trec.QualityBenchmark;
import uk.ac.ucl.panda.applications.evaluation.trec.QualityStats;
import uk.ac.ucl.panda.applications.evaluation.trec.TrecJudge;
import uk.ac.ucl.panda.indexing.ExtraInformation;
import uk.ac.ucl.panda.indexing.io.TrecTopicsReader;
import uk.ac.ucl.panda.map.ResultsList;
import uk.ac.ucl.panda.map.ResultsList.Result;
import uk.ac.ucl.panda.reranking.MMRReranker;
import uk.ac.ucl.panda.reranking.PortfolioReranker;
import uk.ac.ucl.panda.reranking.Reranker;
import uk.ac.ucl.panda.retrieval.models.RawMaterial;
import uk.ac.ucl.panda.retrieval.query.QualityQuery;
import uk.ac.ucl.panda.utility.io.FileReader;
import uk.ac.ucl.panda.utility.io.SubmissionReport;
import uk.ac.ucl.panda.utility.parser.QualityQueryParser;
import uk.ac.ucl.panda.utility.parser.SimpleQQParser;


public class TrecRetrieval {

        protected String newline = System.getProperty("line.separator");

        protected String fileseparator = System.getProperty("file.separator");

    public void process_reranking(String index, String topics, String qrels, String var,
    		String reranking_method, int modelNumber) throws Exception {
    	// do the underlying scoring first.
    	System.err.println("Processing initial scores...");
    	process(index, topics, qrels, var, modelNumber);
    	
    	// rerank
    	System.err.println("Reranking...");
    	// hack, get the model type
    	Class modelType = (new RawMaterial(index, modelNumber)).getModelType();
    	Reranker reranker = null;
    	if (reranking_method.equals("mmr")) {
    		reranker = new MMRReranker();
    	} else if (reranking_method.equals("portfolio")) {
    		reranker = new PortfolioReranker();
    	} else {
    		throw new Exception("Unsupported reranking method.");
    	}
    	reranker.Rerank(index, topics, qrels, var, modelType);
    	
    	// do evaluation after rerank
    	System.err.println("Evaluating...");
    	(new File(var + fileseparator + "results")).renameTo(new File(var + fileseparator + "results-original"));
    	(new File(var + fileseparator + "results-reranked")).renameTo(new File(var + fileseparator + "results"));
    	ResultsList results = getResultsFromFile(var + fileseparator + "results");
    	TrecTopicsReader qReader = new TrecTopicsReader();
	    QualityQuery qqs[] = qReader.readQueries(FileReader.openFileReader(topics));
	    FileOutputStream evals =new FileOutputStream(new File(var+fileseparator+"evals-reranked"));
	    PrintWriter logger = new PrintWriter(evals,true);

	    Judge judge = new TrecJudge(FileReader.openFileReader(qrels));
	    judge.validateData(qqs, logger);
	    QualityStats stats[] = new QualityStats[qqs.length];
	    QualityQueryParser qqParser = new SimpleQQParser("title", "body");
	    
	    for (int i = 0; i < qqs.length; i++) {
	    	int topicNum = Integer.parseInt(qqs[i].getQueryID());
	    	ArrayList<Result> queryResults = results.getTopicResults(topicNum);
	    	
	    	stats[i] = new QualityStats(judge.maxRecall(qqs[i]), 0);
	    	for (Result r : queryResults) {
	    		stats[i].addResult(r.rank + 1,
	    				judge.isRelevant(r.docID, qqs[i]),
	    				0);
	    	}
	    	logger.println(qqs[i].getQueryID() + "  -  " + qqParser.parse(qqs[i]));
	    	stats[i].log(qqs[i].getQueryID() + " Stats:", 1, logger, "  ");
	    }
	    // print an avarage sum of the results
	    QualityStats avg = QualityStats.average(stats);
	    avg.log("SUMMARY", 2, logger, "  ");
	    (new File(var + fileseparator + "results")).renameTo(new File(var + fileseparator + "results-reranked"));
	    (new File(var + fileseparator + "results-original")).renameTo(new File(var + fileseparator + "results"));
    }
    
    public void batch_reranking(String index, String topics, String qrels, String var,
    		String reranking_method, int modelNumber,
    		double batchA, double batchB, double batchIncrement) throws Exception {
    	// do the underlying scoring first.
    	System.err.println("Processing initial scores...");
    	process(index, topics, qrels, var, modelNumber);
    	
    	// hack, get the model type
    	Class modelType = (new RawMaterial(index, modelNumber)).getModelType();
    	// rerank
    	Reranker reranker = null;
    	if (reranking_method.equals("mmr")) {
    		reranker = new MMRReranker();
    	} else if (reranking_method.equals("portfolio")) {
    		reranker = new PortfolioReranker();
    	} else {
    		throw new Exception("Unsupported reranking method.");
    	}
    	
    	// batch reranking and evaluation
    	TrecTopicsReader qReader = new TrecTopicsReader();
    	QualityQuery qqs[] = qReader.readQueries(FileReader.openFileReader(topics));
	    FileOutputStream evals =new FileOutputStream(new File(var+fileseparator+"evals-reranked"));
	    PrintWriter logger = new PrintWriter(evals,true);
	    Judge judge = new TrecJudge(FileReader.openFileReader(qrels));
	    judge.validateData(qqs, logger);
	    
		//batch
		logger.print("MRR"+'\t'+"Recall"+'\t'+"MAP"+'\t'+"1-call"+'\t'+"2-call"+'\t'+"3-call"+'\t'+"4-call"+'\t'+"5-call"+'\t'+"6-call"+'\t'+"7-call"+'\t'+"8-call"+'\t'+"9-call"+'\t'+"10-call"+'\t'+"NDCG@1"+'\t'+"NDCG@5"+'\t'+"NDCG@10"+'\t'+"NDCG@15"+'\t'+"NDCG@20"+'\t'+"NDCG@35"+'\t'+"NDCG@50"+'\t'+"NDCG@70"+'\t'+"NDCG@100"+'\t'+"NDCG@200"+'\t'+"NDCG@250"+'\t'+"NDCG@400"+'\t'+"NDCG@500"+'\t'+"NDCG@600"+'\t'+"NDCG@700"+'\t'+"NDCG@1000");
		for(int i =1; i<100 ;i++){ logger.print('\t'+"Precision@"+i); }
		logger.println();
    	for (double a1 = batchA; a1 <= batchB; a1 += batchIncrement) {
    		// rerank
    		System.err.println("Reranking...(a = " + String.valueOf(a1) + ")");
    		reranker.Rerank(index, topics, qrels, var, modelType, a1);
    		
    		// evaluation
    		System.err.println("Evaluating...(a = " + String.valueOf(a1) + ")");
    		(new File(var + fileseparator + "results")).renameTo(new File(var + fileseparator + "results-original"));
        	(new File(var + fileseparator + "results-reranked")).renameTo(new File(var + fileseparator + "results"));
    		ResultsList results = getResultsFromFile(var + fileseparator + "results");
    		QualityStats stats[] = new QualityStats[qqs.length];
    	    
    	    for (int i = 0; i < qqs.length; i++) {
    	    	int topicNum = Integer.parseInt(qqs[i].getQueryID());
    	    	ArrayList<Result> queryResults = results.getTopicResults(topicNum);
    	    	
    	    	stats[i] = new QualityStats(judge.maxRecall(qqs[i]), 0);
    	    	for (Result r : queryResults) {
    	    		stats[i].addResult(r.rank + 1,
    	    				judge.isRelevant(r.docID, qqs[i]),
    	    				0);
    	    	}
    	    }
    	    // print an avarage sum of the results
    	    QualityStats avg = QualityStats.average(stats);
    	    avg.batch_log(Double.toString(a1) , 2, logger, "  ", true); // batch, with MAP
    	    (new File(var + fileseparator + "results")).renameTo(new File(var + fileseparator + "results-reranked"));
    	    (new File(var + fileseparator + "results-original")).renameTo(new File(var + fileseparator + "results"));
    	}
    }
    
	/**
	 * This function is the same as that in the MAP package, but with a path parameter
	 * 
	 * @param path the path of result file
	 * 
	 * @return ResultsList representing the results file
	 * 
	 * @see uk.ac.ucl.panda.map.ResultsList
	 */
	public static ResultsList getResultsFromFile(String path) {
		// Opens the results file in the Panda/var/ folder
		FileInputStream resultsFile = null;
		ResultsList resultsArray = new ResultsList();
		try {
			resultsFile = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (resultsFile == null)
			return resultsArray;

		// Read in the file
		DataInputStream resultsStream = new DataInputStream(resultsFile);
		BufferedReader results = new BufferedReader(new InputStreamReader(
				resultsStream));

		StringTokenizer rToken;
		String rLine;
		String topic;
		String docID;
		String rank;
		String score;

		try {
			// iterate through every line in the file
			while ((rLine = results.readLine()) != null) {
				rToken = new StringTokenizer(rLine);
				// extract the meaningful information
				topic = rToken.nextToken();
				rToken.nextToken();
				docID = rToken.nextToken();
				rank = rToken.nextToken();
				score = rToken.nextToken();

				// add this result to our ResultsList
				resultsArray.addResult(Integer.parseInt(topic), docID,
						Integer.parseInt(rank), Double.parseDouble(score));

			}
			if (resultsFile != null)
				resultsFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resultsArray;
	}
	
	public void process (String index, String topics, String qrels, String var, int modelNumber) throws Exception{
		
		//Searcher searcher = new Searcher(index);
               

	    int maxResults = 1000;
	    String docNameField = "docname";
	    FileOutputStream evals =new FileOutputStream(new File(var+fileseparator+"evals-hard-bm25"));
	    PrintWriter logger = new PrintWriter(evals,true);

             FileOutputStream results =new FileOutputStream(new File(var+fileseparator+"results"));
            PrintWriter scorelogger = new PrintWriter(results,true);
	    // use trec utilities to read trec topics into quality queries
	    TrecTopicsReader qReader = new TrecTopicsReader();
	    QualityQuery qqs[] = qReader.readQueries(FileReader.openFileReader(topics));
            ////////////
      //       System.out.println("Number of queries: "+qqs.length);
            ///////////
            
	    // prepare judge, with trec utilities that read from a QRels file
	    Judge judge = new TrecJudge(FileReader.openFileReader(qrels));

	    // validate topics & judgments match each other
	    judge.validateData(qqs, logger);

	    // set the parsing of quality queries into Lucene queries.
	    QualityQueryParser qqParser = new SimpleQQParser("title", "body");

	    // run the benchmark
	    QualityBenchmark qrun;
	    if(modelNumber > -1)
	    	qrun = new QualityBenchmark(qqs, qqParser, index, docNameField, modelNumber);
	    else
	    	qrun = new QualityBenchmark(qqs, qqParser, index, docNameField);
	    
	    qrun.setMaxResults(maxResults);
	    SubmissionReport submitLog = null;
	    
	    
	    
	    QualityStats stats[] = qrun.execute(judge, submitLog, logger, scorelogger);

	    // print an avarage sum of the results
	    QualityStats avg = QualityStats.average(stats);
	    avg.log("SUMMARY", 2, logger, "  ");
	
	    scorelogger.close();
	    results.close();
		
	}
	
	
        public void batch (String index, String topics, String qrels, String var, int modelNumber, double batchA, double batchB, double batchIncrement) throws Exception{
		
		//Searcher searcher = new Searcher(index);
               

	    int maxResults = 1000;
	    String docNameField = "docname";
	    FileOutputStream evals =new FileOutputStream(new File(var+fileseparator+"evals-hard-bm25"));
	    PrintWriter logger = new PrintWriter(evals,true);

             FileOutputStream results =new FileOutputStream(new File(var+fileseparator+"results"));
            PrintWriter scorelogger = new PrintWriter(results,true);
	    // use trec utilities to read trec topics into quality queries
	    TrecTopicsReader qReader = new TrecTopicsReader();
	    QualityQuery qqs[] = qReader.readQueries(FileReader.openFileReader(topics));

            ////////////
      //       System.out.println("Number of queries: "+qqs.length);
            ///////////
            
	    // prepare judge, with trec utilities that read from a QRels file
	    Judge judge = new TrecJudge(FileReader.openFileReader(qrels));

	    // validate topics & judgments match each other
	    judge.validateData(qqs, logger);

	    // set the parsing of quality queries into Lucene queries.
	    QualityQueryParser qqParser = new SimpleQQParser("title", "body");

	    // run the benchmark
	    QualityBenchmark qrun;
	    if(modelNumber > -1)
	    	qrun = new QualityBenchmark(qqs, qqParser, index, docNameField, modelNumber);
	    else
	    	qrun = new QualityBenchmark(qqs, qqParser, index, docNameField);
	    qrun.setMaxResults(maxResults);
	    SubmissionReport submitLog = null;
	    
            
            //batch
             logger.print("MRR"+'\t'+"Recall"+'\t'+"1-call"+'\t'+"2-call"+'\t'+"3-call"+'\t'+"4-call"+'\t'+"5-call"+'\t'+"6-call"+'\t'+"7-call"+'\t'+"8-call"+'\t'+"9-call"+'\t'+"10-call"+'\t'+"NDCG@1"+'\t'+"NDCG@5"+'\t'+"NDCG@10"+'\t'+"NDCG@15"+'\t'+"NDCG@20"+'\t'+"NDCG@35"+'\t'+"NDCG@50"+'\t'+"NDCG@70"+'\t'+"NDCG@100"+'\t'+"NDCG@200"+'\t'+"NDCG@250"+'\t'+"NDCG@400"+'\t'+"NDCG@500"+'\t'+"NDCG@600"+'\t'+"NDCG@700"+'\t'+"NDCG@1000");
            for(int i =1; i<100 ;i++){
                logger.print('\t'+"Precision@"+i);
            }
            
            logger.println();
      /**
       * Var adjust
       */

         
	    for(double a1=batchA; a1<=batchB; a1+=batchIncrement){
	    
	    QualityStats stats[] = qrun.execute(judge, submitLog, null, scorelogger, a1, 0);

	    // print an avarage sum of the results
	    QualityStats avg = QualityStats.average(stats);
	    avg.batch_log(Double.toString(a1) , 2, logger, "  ");
	
           }
	
             /**
       * Doc correlation adjust
       */
       /*
               for(double a2=-2.0d; a2<=1.0d; a2+=0.1d){

	    QualityStats stats[] = qrun.execute(judge, submitLog, null, scorelogger, 0, a2);

	    // print an avarage sum of the results
	    QualityStats avg = QualityStats.average(stats);
	    avg.batch_log(Double.toString(a2) , 2, logger, "  ");

           }
	*/


	}

          public void process_var (String index, String topics, String qrels, String var) throws Exception{

		//Searcher searcher = new Searcher(index);


	    int maxResults = 1000;
	    String docNameField = "docname";
	    FileOutputStream evals =new FileOutputStream(new File(var+fileseparator+"test-ScoreCombine"));
	    PrintWriter logger = new PrintWriter(evals,true);

             FileOutputStream results =new FileOutputStream(new File(var+fileseparator+"results"));
            PrintWriter scorelogger = new PrintWriter(results,true);
	    // use trec utilities to read trec topics into quality queries
	    TrecTopicsReader qReader = new TrecTopicsReader();
	    QualityQuery qqs[] = qReader.readQueries(FileReader.openFileReader(topics));

            ////////////
      //       System.out.println("Number of queries: "+qqs.length);
            ///////////

	    // prepare judge, with trec utilities that read from a QRels file
	    Judge judge = new TrecJudge(FileReader.openFileReader(qrels));

	    // validate topics & judgments match each other
	    judge.validateData(qqs, logger);

	    // set the parsing of quality queries into Lucene queries.
	    QualityQueryParser qqParser = new SimpleQQParser("title", "body");

	    // run the benchmark
	    QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, index, docNameField);
	    qrun.setMaxResults(maxResults);
	    SubmissionReport submitLog = null;


            //batch
             logger.print("MAP"+'\t'+"MRR"+'\t'+"Recall"+'\t'+"1-call"+'\t'+"2-call"+'\t'+"3-call"+'\t'+"4-call"+'\t'+"5-call"+'\t'+"6-call"+'\t'+"7-call"+'\t'+"8-call"+'\t'+"9-call"+'\t'+"10-call"+'\t'+"NDCG@1"+'\t'+"NDCG@5"+'\t'+"NDCG@10"+'\t'+"NDCG@15"+'\t'+"NDCG@20"+"NDCG@35"+'\t'+"NDCG@50"+'\t'+"NDCG@70"+'\t'+"NDCG@100"+'\t'+"NDCG@200"+'\t'+"NDCG@250"+'\t'+"NDCG@400"+'\t'+"NDCG@500"+'\t'+"NDCG@600"+'\t'+"NDCG@700"+'\t');
            for(int i =1; i<=70 ;i++){
                logger.print('\t'+"Precision@"+i);
            }

            logger.println();
      /**
       * Var adjust
       */


	    for(double a1=0.000001d; a1<=1.0d; a1+=1.0d){

            for(double a2=10.0d; a2<=15.0d; a2+=10.0d){
	    QualityStats stats[] = qrun.execute_var(judge, submitLog, null, scorelogger, a1, a2);
	    // print an avarage sum of the results
	    QualityStats avg = QualityStats.average(stats);
	    avg.batch_log(Double.toString(a1) , 2, logger, "  ");
        }

           }

             /**
       * Doc correlation adjust
       */
       /*
               for(double a2=-2.0d; a2<=1.0d; a2+=0.1d){

	    QualityStats stats[] = qrun.execute(judge, submitLog, null, scorelogger, 0, a2);

	    // print an avarage sum of the results
	    QualityStats avg = QualityStats.average(stats);
	    avg.batch_log(Double.toString(a2) , 2, logger, "  ");

           }
	*/


	}

          public void process_plot (String index, String topics, String qrels, String var) throws Exception{

		//Searcher searcher = new Searcher(index);


	    int maxResults = 1000;
	    String docNameField = "docname";
	    FileOutputStream evals =new FileOutputStream(new File(var+fileseparator+"test-plot"));
	    PrintWriter logger = new PrintWriter(evals,true);

        FileOutputStream relScorePair =new FileOutputStream(new File(var+fileseparator+"rel-score-pair"));
	    PrintWriter relScoreLogger = new PrintWriter(relScorePair,true);

             FileOutputStream results =new FileOutputStream(new File(var+fileseparator+"result-plot"));
            PrintWriter scorelogger = new PrintWriter(results,true);
	    // use trec utilities to read trec topics into quality queries
	    TrecTopicsReader qReader = new TrecTopicsReader();
	    QualityQuery qqs[] = qReader.readQueries(FileReader.openFileReader(topics));

            ////////////
      //       System.out.println("Number of queries: "+qqs.length);
            ///////////

	    // prepare judge, with trec utilities that read from a QRels file
	    Judge judge = new TrecJudge(FileReader.openFileReader(qrels));

	    // validate topics & judgments match each other
	    judge.validateData(qqs, logger);

	    // set the parsing of quality queries into Lucene queries.
	    QualityQueryParser qqParser = new SimpleQQParser("title", "body");

	    // run the benchmark
	    QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, index, docNameField);
	    qrun.setMaxResults(maxResults);
	    SubmissionReport submitLog = null;


            //batch
             logger.print("MAP"+'\t'+"MRR"+'\t'+"Recall"+'\t'+"1-call"+'\t'+"2-call"+'\t'+"3-call"+'\t'+"4-call"+'\t'+"5-call"+'\t'+"6-call"+'\t'+"7-call"+'\t'+"8-call"+'\t'+"9-call"+'\t'+"10-call"+'\t'+"NDCG@1"+'\t'+"NDCG@5"+'\t'+"NDCG@10"+'\t'+"NDCG@15"+'\t'+"NDCG@20"+"NDCG@35"+'\t'+"NDCG@50"+'\t'+"NDCG@70"+'\t'+"NDCG@100"+'\t'+"NDCG@200"+'\t'+"NDCG@250"+'\t'+"NDCG@400"+'\t'+"NDCG@500"+'\t'+"NDCG@600"+'\t'+"NDCG@700"+'\t');
            for(int i =1; i<=70 ;i++){
                logger.print('\t'+"Precision@"+i);
            }

            logger.println();
      /**
       * Var adjust
       */


	    for(double a1=0.0d; a1<=0.0d; a1+=1.0d){

	    QualityStats stats[] = qrun.execute_plot(judge, submitLog, null, scorelogger, a1, 0, relScoreLogger);

	    // print an avarage sum of the results
	    QualityStats avg = QualityStats.average(stats);
	    avg.batch_log(Double.toString(a1) , 2, logger, "  ");

           }

             /**
       * Doc correlation adjust
       */
       /*
               for(double a2=-2.0d; a2<=1.0d; a2+=0.1d){

	    QualityStats stats[] = qrun.execute(judge, submitLog, null, scorelogger, 0, a2);

	    // print an avarage sum of the results
	    QualityStats avg = QualityStats.average(stats);
	    avg.batch_log(Double.toString(a2) , 2, logger, "  ");

           }
	*/


	}

	
}
