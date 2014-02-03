package uk.ac.ucl.panda.retrieval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import uk.ac.ucl.panda.applications.evaluation.trec.Judge;
import uk.ac.ucl.panda.applications.evaluation.trec.QualityBenchmark;
import uk.ac.ucl.panda.applications.evaluation.trec.QualityStats;
import uk.ac.ucl.panda.applications.evaluation.trec.TrecJudge;
import uk.ac.ucl.panda.indexing.ExtraInformation;
import uk.ac.ucl.panda.indexing.io.TrecTopicsReader;
import uk.ac.ucl.panda.retrieval.query.QualityQuery;
import uk.ac.ucl.panda.utility.io.FileReader;
import uk.ac.ucl.panda.utility.io.SubmissionReport;
import uk.ac.ucl.panda.utility.parser.QualityQueryParser;
import uk.ac.ucl.panda.utility.parser.SimpleQQParser;


public class TrecRetrieval {

        protected String newline = System.getProperty("line.separator");

        protected String fileseparator = System.getProperty("file.separator");

	
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
